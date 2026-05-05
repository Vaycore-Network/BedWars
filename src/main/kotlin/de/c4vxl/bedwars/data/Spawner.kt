package de.c4vxl.bedwars.data

import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.bedwars.utils.HologramUtils.createHologram
import de.c4vxl.bedwars.utils.HologramUtils.setHologramItem
import de.c4vxl.bedwars.utils.HologramUtils.setHologramText
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import net.minecraft.world.entity.decoration.ArmorStand
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

/**
 * Represents an item spawner
 */
data class Spawner(
    val location: Location,
    val material: Material,
    val interval: Int,
    val maximum: Int,
    val displayDelay: Boolean,
    val displayName: String?,
    val displayItem: Material?
) {
    /**
     * Builds the item
     */
    val item: ItemStack = ItemBuilder(material, key = "bw_spawner_item")
            // Add translation key
            .apply { translatable("item.material.${material.name.lowercase()}.name") }
            .build()

    /**
     * Drops the spawning item in a world
     * @param world The world
     */
    fun drop(world: World) {
        val nearby = location.getNearbyEntitiesByType(Item::class.java, 3.0)
            .sumOf {
                it.itemStack
                    .takeIf { i -> i.type == material }
                    ?.amount
                    ?: 0
            }

        if (nearby >= maximum)
            return

        // Spawn item
        world.dropItem(location, item)

            // Remove random velocity from item
            .velocity = Vector(0, 0, 0)
    }

    /**
     * Sends the spawner hologram to a player
     * @param player The player to send the hologram to
     */
    fun sendHologram(player: Player): ArmorStand? {
        if (!displayDelay)
            return null

        val loc = location.clone().add(0.0, 2.1, 0.0)

        // Send spawner display item
        displayItem?.let {
            player.setHologramItem(
                player.createHologram(loc.clone().add(0.0, -0.5, 0.0)) ?: return@let,
                it
            )
        }

        // Send spawner name
        displayName?.let {
            player.setHologramText(
                player.createHologram(loc.clone().add(0.0, 1.45, 0.0)) ?: return@let,
                player.language.child("bedwars").getCmp(it)
            )
        }

        return player.createHologram(loc)
    }

    /**
     * Renders the spawner to all players of a game
     * @param game The game to render the spawner to
     */
    fun render(game: Game) =
        game.players.mapNotNull { player ->
            player.bukkitPlayer to (sendHologram(player.bukkitPlayer) ?: return@mapNotNull null)
        }
}