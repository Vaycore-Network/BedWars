package de.c4vxl.bedwars.utils

import com.mojang.datafixers.util.Pair
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.decoration.ArmorStand
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Predicate

/**
 * Utilities regarding "holograms"
 */
object HologramUtils {
    /**
     * Creates a hologram entity and sends it to a player
     * @param location The location to place the hologram at
     */
    fun Player.createHologram(location: Location): ArmorStand? {
        // Wrong world
        if (this.world != location.world)
            return null

        // Get player info
        val player = (this as CraftPlayer).handle
        val level = player.level()
        val connection = player.connection

        // Create entity to display
        val entity = ArmorStand(EntityType.ARMOR_STAND, level)
        entity.isMarker = true
        entity.isNoGravity = true
        entity.isInvisible = true
        entity.setPos(location.x, location.y, location.z)

        // Send entity to player
        val synchronizer = object : ServerEntity.Synchronizer {
            override fun sendToTrackingPlayers(p0: Packet<in ClientGamePacketListener>) {}
            override fun sendToTrackingPlayersAndSelf(p0: Packet<in ClientGamePacketListener>) {}
            override fun sendToTrackingPlayersFiltered(p0: Packet<in ClientGamePacketListener>, p1: Predicate<ServerPlayer>) {}
        }
        connection.send(
            ClientboundAddEntityPacket(
                entity,
                ServerEntity(level, entity, 0, false, synchronizer, setOf())
            )
        )

        // Update entity data
        connection.send(ClientboundSetEntityDataPacket(entity.id, entity.entityData.packAll()))

        return entity
    }

    /**
     * Sets the item of a hologram
     * @param entity The hologram
     * @param item The item to create the hologram of
     */
    fun Player.setHologramItem(entity: ArmorStand, item: Material) {
        (this as CraftPlayer).handle.connection.send(
            ClientboundSetEquipmentPacket(
                entity.id,
                mutableListOf(
                    Pair(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(ItemStack(item)))
                )
            )
        )
    }

    /**
     * Updates a holograms text for a player
     * @param entity The hologram entity
     * @param text The updated text
     */
    fun Player.setHologramText(entity: ArmorStand, text: Component) {
        entity.isCustomNameVisible = true
        entity.customName = PaperAdventure.asVanilla(text)

        // Update entity data
        (this as CraftPlayer).handle.connection.send(ClientboundSetEntityDataPacket(entity.id, entity.entityData.packAll()))
    }
}