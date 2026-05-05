package de.c4vxl.bedwars.utils

import org.bukkit.Material
import org.bukkit.inventory.Inventory

object InventoryUtils {
    /**
     * Returns the amount of a certain material in the inventory
     * @param material The material to count
     */
    fun Inventory.countMaterial(material: Material) =
        this.contents.sumOf {
            if (it?.type == material) it.amount
            else 0
        }

    /**
     * Removes a certain amount of a specific material
     * @param material The material to remove
     * @param amount The amount of the material to remove
     */
    fun Inventory.removeMaterial(material: Material, amount: Int) {
        var remaining = amount

        for (i in contents.indices) {
            val item = contents[i] ?: continue
            if (item.type != material) continue

            when {
                item.amount < remaining -> {
                    remaining -= item.amount
                    contents[i] = null
                }
                else -> {
                    item.amount -= remaining
                    contents[i] = item
                    return
                }
            }

            if (remaining <= 0)
                return
        }
    }
}