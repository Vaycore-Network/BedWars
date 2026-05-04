package de.c4vxl.bedwars.item

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.bedwars.ui.PlayerTrackerUI
import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

/**
 * A custom compass
 * Revealing the location of another player
 */
object PlayerTracker {
    init {
        // Update trackers in 20 tick interval
        Bukkit.getScheduler().runTaskTimer(Main.instance, Runnable {
            GMA.registeredGames.forEach {
                it.trackers.forEach { (tracking, tracked) ->
                    if (tracking.gma.game != tracked.gma.game)
                        it.gameData["trackers"] = it.trackers.apply { remove(tracking) }
                    else
                        tracking.compassTarget = tracked.location
                }
            }
        }, 20, 20)
    }

    private var eventsInitialized = false
    private val Game.trackers get() = this.gameData.get<MutableMap<Player, Player>>("trackers") ?: mutableMapOf()

    fun item(language: Language) =
        ItemBuilder(
            Material.COMPASS,
            language.getCmp("item.custom.tracker.name"),
            lore = listOf(language.getCmp("item.custom.tracker.desc.1")),
            key = "bw_tracker"
        )
            .translatable("item.custom.tracker.name")
            .apply {
                if (!eventsInitialized)
                    onEvent(PlayerInteractEvent::class.java) { event ->
                        if (!event.action.isRightClick)
                            return@onEvent

                        val game = event.player.gma.game ?: return@onEvent

                        // Track player
                        PlayerTrackerUI(event.player, { tracked ->
                            game.gameData["trackers"] = game.trackers.apply {
                                put(event.player, tracked)
                            }

                            event.player.sendActionBar(event.player.language.child("bedwars").getCmp("game.tracker.notice.tracker", tracked.name))
                            tracked.sendActionBar(tracked.language.child("bedwars").getCmp("game.tracker.notice.tracked"))
                        }).open()

                        event.isCancelled = true
                    }

                eventsInitialized = true
            }
}