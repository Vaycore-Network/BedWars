package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.gamemanager.gma.event.game.GameStartedEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Handles general game events
 */
class GameHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onGameStarted(event: GameStartedEvent) {
        event.game.broadcastMessage(
            "game.started.broadcast",
            event.game.worldManager.map?.name ?: "",
            child = "bedwars"
        )
    }
}