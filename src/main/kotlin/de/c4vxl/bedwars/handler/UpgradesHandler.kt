package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.data.TeamData.upgrades
import de.c4vxl.bedwars.data.Upgrade
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.gma.team.Team
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.LightningStrike
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * Handles item and base upgrades
 */
class UpgradesHandler {
    init {
        Bukkit.getScheduler().runTaskTimer(Main.instance, Runnable {
            Bukkit.getOnlinePlayers().forEach { handleBaseUpgrades(it) }
        }, 40, 40)
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

        /**
         * Returns the team owning the base a player is currently at
         * @param game The game
         * @param player The player to check
         */
        fun getBaseAt(game: Game, player: Player): Team? {
            val shops: Map<Int, Location> = game.gameData["shops"] ?: return null
            val distance = game.gameData["upgradesDistance"] ?: 30
            val loc = player.location
            val id = shops.toList().find {
                it.second.distance(loc) <= distance
            }?.first ?: return null
            return game.teamManager.teams[id]
        }
    }

    /**
     * Announces a trap to a team
     */
    private fun announceTrap(team: Team) {
        // Announce
        team.players.forEach {
            it.bukkitPlayer.sendTitlePart(TitlePart.TITLE, it.language.child("bedwars").getCmp("game.trap.triggered.title"))
            it.bukkitPlayer.playSound(it.bukkitPlayer.location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.5f)
        }
    }

    @EventHandler
    fun onInv(event: InventoryEvent) {
        event.viewers.forEach { viewer ->
            val player = viewer as? Player ?: return@forEach
            // Check for game
            player.gma.game.takeIf { it?.isRunning == true } ?: return

            // Upgrade all items
            player.inventory.contents
                .filterNotNull()
                .forEach { upgradeItem(player, it) }
        }
    }

    private fun handleBaseUpgrades(player: Player) {
        val game = player.gma.game?.takeIf { it.isRunning } ?: return
        val team = getBaseAt(game, player) ?: return
        val upgrades = team.upgrades
        val isOwnTeam = team.players.contains(player.gma)

        // Base heal potion effect
        if (upgrades.contains(Upgrade.BASE_HEAL) && isOwnTeam)
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 20 * 3, 1, true, false))

        // Blindness trap
        if (upgrades.contains(Upgrade.BLINDNESS_TRAP) && !isOwnTeam) {
            player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 20 * 10, 2, true, false))
            team.upgrades = upgrades.apply { remove(Upgrade.BLINDNESS_TRAP) }
            announceTrap(team)
        }

        // Lightning trap
        if (upgrades.contains(Upgrade.LIGHTNING_TRAP) && !isOwnTeam) {
            (player.world.spawnEntity(player.location.add(0.0, 3.0, 0.0), EntityType.LIGHTNING_BOLT) as LightningStrike)

            team.upgrades = upgrades.apply { remove(Upgrade.LIGHTNING_TRAP) }
            announceTrap(team)
        }

        // Mining trap
        if (upgrades.contains(Upgrade.MINING_TRAP) && !isOwnTeam) {
            player.addPotionEffect(PotionEffect(PotionEffectType.MINING_FATIGUE, 20 * 30, 1, true, false))
            team.upgrades = upgrades.apply { remove(Upgrade.MINING_TRAP) }
            announceTrap(team)
        }
    }
}