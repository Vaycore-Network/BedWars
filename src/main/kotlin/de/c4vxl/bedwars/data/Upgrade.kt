package de.c4vxl.bedwars.data

import org.bukkit.Material

enum class Upgrade(val display: Material, val cost: Int) {
    SHARPNESS(Material.IRON_SWORD, 4),
    PROTECTION(Material.IRON_CHESTPLATE, 4),
    TOOLS(Material.IRON_PICKAXE, 2),
    BASE_HEAL(Material.GOLDEN_APPLE, 8),
    MINING_TRAP(Material.WOODEN_PICKAXE, 1),
    LIGHTNING_TRAP(Material.LIGHTNING_ROD, 2),
    BLINDNESS_TRAP(Material.SPIDER_EYE, 1)
}