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
}