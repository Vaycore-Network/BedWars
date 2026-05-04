package de.c4vxl.bedwars.item

import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory

/**
 * A portable chest
 * A shared chest between a team
 */
object TeamChest {
    private var eventsInitialized = false

    fun item(language: Language) =
        ItemBuilder(
            Material.CHEST,
            language.getCmp("item.custom.team_chest.name"),
            lore = listOf(language.getCmp("item.custom.team_chest.desc.1")),
            key = "bw_team_chest"
        )
            .translatable("item.custom.team_chest.name")
            .apply {
                if (!eventsInitialized)
                    onEvent(PlayerInteractEvent::class.java) { event ->
                        if (!event.action.isRightClick)
                            return@onEvent

                        val team = event.player.gma.team ?: return@onEvent
                        val game = team.manager.game

                        // Get chest
                        val chests: MutableMap<Int, Inventory> = game.gameData["teamChests"] ?: mutableMapOf()
                        val inventory = chests.computeIfAbsent(team.id) { Bukkit.createInventory(null, InventoryType.CHEST) }
                        game.gameData["teamChests"] = chests

                        // Open chest
                        event.player.openInventory(inventory)
                        event.player.playSound(event.player.location, Sound.BLOCK_CHEST_OPEN, 2f, 1f)

                        event.isCancelled = true
                    }

                eventsInitialized = true
            }
}