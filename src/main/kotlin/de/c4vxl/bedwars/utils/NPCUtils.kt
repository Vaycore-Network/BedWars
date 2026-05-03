package de.c4vxl.bedwars.utils

import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import io.netty.buffer.Unpooled
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.atan2

/**
 * Utilities regarding fake players
 */
object NPCUtils {
    /**
     * Renders an npc to a player
     * @param location The location to spawn the npc at
     * @param skin The texture-signature pair
     */
    fun Player.renderNPC(location: Location, skin: Pair<String, String?>, displayName: Component): Int {
        // Wrong world
        if (this.world != location.world)
            return -1

        // Get player info
        val player = (this as CraftPlayer).handle
        val level = player.level()
        val connection = player.connection

        // Create profile
        val profile = GameProfile(UUID.randomUUID(), LegacyComponentSerializer.legacySection().serialize(displayName),
            PropertyMap(ImmutableMultimap.of(
                "textures", Property("textures", skin.first, skin.second)
            ))
        )

        // Create NPC information packet
        val entry = ClientboundPlayerInfoUpdatePacket.Entry(
            profile.id, profile, false, 0,
            GameType.CREATIVE, player.getDisplayName(),
            false, 0, null
        )
        
        // Send npc information
        connection.send(
            ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
                listOf(entry)
            )
        )

        // Update location
        val entityId = Entity.nextEntityId()
        connection.send(
            ClientboundAddEntityPacket(
                entityId, profile.id,
                location.x, location.y, location.z,
                location.yaw, location.pitch,
                EntityType.PLAYER, 0, Vec3.ZERO, 0.0
            )
        )

        connection.send(
            ClientboundSetEntityDataPacket(
                entityId,
                listOf(SynchedEntityData.DataValue(
                    net.minecraft.world.entity.player.Player.DATA_PLAYER_MODE_CUSTOMISATION.id,
                    EntityDataSerializers.BYTE,
                    (0x7F).toByte()
                ))
            )
        )

        return entityId
    }

    /**
     * Sets the head rotation of an npc
     * @param npc The id of the npc
     * @param yaw The head rotation of the npc
     */
    fun Player.sendNPCRotation(npc: Int, yaw: Float) {
        val player = (this as CraftPlayer).handle
        val level = player.level()
        val connection = player.connection

        // Update head rotation
        connection.send(ClientboundRotateHeadPacket(
            object : Entity(EntityType.PLAYER, level) {
                init {
                    this.id = npc
                }

                override fun defineSynchedData(p0: SynchedEntityData.Builder) {}
                override fun hurtServer(p0: ServerLevel, p1: DamageSource, p2: Float): Boolean = false
                override fun readAdditionalSaveData(p0: ValueInput) {}
                override fun addAdditionalSaveData(p0: ValueOutput) {}
            },
            (yaw * 256f / 360f).toInt().toByte()
        ))

        // Update body rotation
        connection.send(
            ClientboundMoveEntityPacket.Rot(
            npc, (yaw * 256f / 360f).toInt().toByte(), 0.toByte(), true
        ))
    }

    /**
     * Calculates the yaw for an npc to face a certain location
     */
    fun calculateYaw(from: Location, to: Location): Float {
        val dx = from.x - to.x
        val dz = from.z - to.z
        return Math.toDegrees(atan2(-dx, dz)).toFloat()
    }

    /**
     * Returns the skin texture-signature pair of a players skin
     */
    val Player.skin: Pair<String, String?>
        get() {
            val textures = (this as CraftPlayer).profile.properties.get("textures").first()
            return textures.value to textures.signature
        }
}