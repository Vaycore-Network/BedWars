package de.c4vxl.bedwars.item

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent

/**
 * A custom ender pearl
 * Teleports players back after 5 seconds
 */
object WarpPearl {
    private var eventsInitialized = false

    fun item(language: Language) =
        ItemBuilder(
            Material.ENDER_PEARL,
            language.getCmp("item.custom.warp_pearl.name"),
            lore = listOf(language.getCmp("item.custom.warp_pearl.desc.1"), language.getCmp("item.custom.warp_pearl.desc.2")),
            key = "bw_warp_pearl"
        )
            .translatable("item.custom.warp_pearl.name")
            .apply {
                if (!eventsInitialized)
                    onEvent(PlayerInteractEvent::class.java) { event ->
                        if (!event.action.isRightClick)
                            return@onEvent

                        // Send info
                        event.player.sendActionBar(event.player.language.child("bedwars").getCmp("game.warp_pearl.notice"))

                        // Teleport back after 5 seconds
                        event.player.location.clone().let {
                            Bukkit.getScheduler().runTaskLater(Main.instance, Runnable {
                                event.player.teleport(it)
                            }, 20 * 6)
                        }
                    }

                eventsInitialized = true
            }
}