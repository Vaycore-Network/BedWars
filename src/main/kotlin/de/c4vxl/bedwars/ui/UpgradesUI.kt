package de.c4vxl.bedwars.ui

import de.c4vxl.bedwars.data.TeamData.upgrades
import de.c4vxl.bedwars.data.Upgrade
import de.c4vxl.bedwars.utils.InventoryUtils.countMaterial
import de.c4vxl.bedwars.utils.InventoryUtils.removeMaterial
import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * UI for the upgrades shop
 */
class UpgradesUI(
    private val shopUI: ShopUI,
) {
    val player = shopUI.player
    val language = shopUI.language
    val team = shopUI.team

    private fun buyUpgrade(upgrade: Upgrade): Boolean {
        // Upgrade already bought
        if (team.upgrades.contains(upgrade))
            return false

        // Add upgrade
        team.upgrades = team.upgrades.apply { add(upgrade) }

        // Notify team
        team.players.forEach { player ->
            val lang = player.language.child("bedwars")
            player.bukkitPlayer.sendMessage(
                lang.getCmp("game.upgrade.bought", this.player.name, lang.get("ui.upgrades.${upgrade.name.lowercase()}.name"))
            )
        }

        return true
    }

    fun open() {
        val inv = shopUI.baseInventory.apply {
            mapOf(
                20 to Upgrade.SHARPNESS,
                21 to Upgrade.PROTECTION,
                22 to Upgrade.TOOLS,
                29 to Upgrade.BASE_HEAL,
                30 to Upgrade.BLINDNESS_TRAP,
                31 to Upgrade.MINING_TRAP,
                32 to Upgrade.LIGHTNING_TRAP
            ).forEach { (slot, upgrade) ->
                setItem(slot, ItemBuilder(
                    upgrade.display,
                    language.getCmp("ui.upgrades.${upgrade.name.lowercase()}.name"),
                    lore = buildList {
                        for (i in 1..10) {
                            val key = "ui.upgrades.${upgrade.name.lowercase()}.lore.$i"

                            if (language.get(key) == key) {
                                add(Component.empty())
                                add(language.getCmp("ui.upgrades.cost", upgrade.cost.toString(), Material.DIAMOND.translationKey()))
                                break
                            } else add(language.getCmp(key))
                        }
                    }
                )
                    .onEvent(InventoryClickEvent::class.java) { event ->
                        event.isCancelled = true

                        // Only accept left clicks
                        if (!event.isLeftClick)
                            return@onEvent

                        // Player can't afford the item
                        if (player.inventory.countMaterial(Material.DIAMOND) < upgrade.cost) {
                            player.sendMessage(language.getCmp("game.shop.buy.fail"))
                            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 3f)
                            return@onEvent
                        }

                        // Try to buy the upgrade
                        if (!buyUpgrade(upgrade)) {
                            player.sendMessage(language.getCmp("game.upgrade.buy.fail.already"))
                            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 3f)
                            return@onEvent
                        }

                        // Remove currency
                        player.inventory.removeMaterial(Material.DIAMOND, upgrade.cost)

                        // Play sound
                        player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                    }
                    .build())
            }
        }

        println("dsf")

        // Open page
        player.openInventory(inv)

        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
    }
}