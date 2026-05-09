package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.data.TeamData.upgrades
import de.c4vxl.bedwars.data.Upgrade
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import net.minecraft.world.level.Level
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

/**
 * Handles upgrades
 */
class UpgradesHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    companion object {
        /**
         * Handles item upgrades
         * @param player The player to upgrade the item for
         * @param item The item to upgrade
         */
        fun upgradeItem(player: Player, item: ItemStack) {
            val allUpgrades = player.gma.team?.upgrades ?: return

            fun handle(upgrade: Upgrade, enchantment: Enchantment, level: Int) {
                if (allUpgrades.contains(upgrade))
                    item.addUnsafeEnchantments(mapOf(enchantment to level))
            }

            // Sword item
            if (item.type.name.endsWith("_SWORD"))
                handle(Upgrade.SHARPNESS, Enchantment.SHARPNESS, 2)

            else if (item.type.name.endsWith("CHESTPLATE"))
                handle(Upgrade.PROTECTION, Enchantment.PROTECTION, 2)

            // Tool item
            else if (item.type.name.endsWith("AXE") || item.type == Material.SHEARS)
                handle(Upgrade.TOOLS, Enchantment.EFFICIENCY, 2)
        }
    }
}