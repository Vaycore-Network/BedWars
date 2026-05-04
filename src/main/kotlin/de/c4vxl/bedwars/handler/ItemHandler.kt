package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBucketEmptyEvent

/**
 * Handles custom items
 */
class ItemHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onWaterPlace(event: PlayerBucketEmptyEvent) {
        if (!event.player.gma.isInGame)
            return

        if (event.bucket != Material.WATER_BUCKET)
            return

        // Remove bucket
        event.itemStack?.let {
            it.amount = 0
        }
    }
}