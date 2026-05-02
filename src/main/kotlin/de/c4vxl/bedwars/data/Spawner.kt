package de.c4vxl.bedwars.data

import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

/**
 * Represents an item spawner
 */
data class Spawner(
    val location: Location,
    val material: Material,
    val interval: Int,
    val displayDelay: Boolean,
    val name: String?,
    var timeWaited: Int = 0
) {
    /**
     * Builds the item
     */
    val item: ItemStack = ItemBuilder(material)
            // Add translation key
            .apply { this@Spawner.name?.let { translatable(it) } }
            .build()

    /**
     * Drops the spawning item in a world
     * @param world The world
     */
    fun drop(world: World) {
        // Spawn item
        world.dropItem(location, item)

            // Remove random velocity from item
            .velocity = Vector(0, 0, 0)
    }
}