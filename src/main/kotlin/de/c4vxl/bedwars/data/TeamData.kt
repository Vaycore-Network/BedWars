package de.c4vxl.bedwars.data

import de.c4vxl.bedwars.Main
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamemanager.gma.team.Team
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta

/**
 * Data extensions for GMA teams
 */
object TeamData {
    private val config = YamlConfiguration.loadConfiguration(Main.instance.dataFolder.resolve("teams.yml"))

    /**
     * Returns the color of a team
     */
    val Team.color: Color
        get() =
            config.getColor("colors.${this.id}") ?: Color.FUCHSIA

    /**
     * Returns a colored leather armor piece based on the team color
     * @param piece The armor piece
     * @param player The player the armor is for
     */
    fun Team.getLeatherPiece(piece: EquipmentSlot, player: Player): ItemStack {
        val material = when (piece) {
            EquipmentSlot.HEAD -> Material.LEATHER_HELMET
            EquipmentSlot.CHEST -> Material.LEATHER_CHESTPLATE
            EquipmentSlot.LEGS -> Material.LEATHER_LEGGINGS
            EquipmentSlot.FEET -> Material.LEATHER_BOOTS
            else               -> throw IllegalArgumentException("Invalid equipment slot '${piece}'. Only HEAD, CHEST, LEGS, FEET!")
        }

        return ItemBuilder(
            material,
            player.language.child("bedwars").getCmp("item.armor.leather.name")
        )
            .editMeta { meta -> (meta as LeatherArmorMeta).setColor(this.color) }
            .build()
    }

    /**
     * Equips the player with the team colored leather armor
     */
    fun GMAPlayer.equipTeamArmor() {
        val team = this.team ?: return

        this.bukkitPlayer.inventory.armorContents =
            listOf(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD)
                .map { team.getLeatherPiece(it, this.bukkitPlayer) }
                .toTypedArray()
    }

    enum class BlockVariant {
        PRIMARY,
        GLASS,
        WOOL
    }

    /**
     * Returns the team colored version of a block variant
     * @param variant The type of block
     */
    fun Team.getBlockVariant(variant: BlockVariant) =
        config.getString("blocks.${this.id}.${variant.name.lowercase()}")
            ?.let { Material.getMaterial(it) }
            ?: run { throw RuntimeException("Couldn't find block variant '${variant.name}' for Team '${this.id}'") }
}