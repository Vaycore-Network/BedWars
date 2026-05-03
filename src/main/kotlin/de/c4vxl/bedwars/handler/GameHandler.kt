package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.gamemanager.gma.event.game.GameStartedEvent
import de.c4vxl.gamemanager.gma.event.game.GameWorldLoadedEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import org.bukkit.Bukkit
import org.bukkit.GameRules
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import kotlin.time.measureTime

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

    @EventHandler
    fun onWorldLoaded(event: GameWorldLoadedEvent) {
        val world = event.map.world ?: return

        world.setGameRule(GameRules.KEEP_INVENTORY, true) // Item dropping will be handled separately
        world.setGameRule(GameRules.RANDOM_TICK_SPEED, 0)
        world.time = 6000
    }

    @EventHandler
    fun onArmor(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val game = player.gma.game ?: return

        if (event.slot !in 36..39)
            return

        event.isCancelled = true
    }
}