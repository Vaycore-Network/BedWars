package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.data.Spawner
import de.c4vxl.gamemanager.gma.event.game.GameWorldLoadedEvent
import de.c4vxl.gamemanager.gma.event.game.GameWorldUnloadEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitTask

/**
 * Handles the item spawners:
 * Materials will spawn in at specific locations of the map
 */
class SpawnerHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onWorldLoaded(event: GameWorldLoadedEvent) {
        val world = event.map.world ?: return

        // Read spawner data
        val config = event.map.getMetadata("bedwars.spawners") ?: return
        val spawners = config.getKeys(false).mapNotNull { key ->
            val (x, y, z) = config.getIntegerList("$key.location")
            Spawner(
                Location(world, x + 0.5, y + 0.5, z + 0.5),
                config.getString("$key.material")?.let { Material.getMaterial(it) } ?: return@mapNotNull null,
                config.getInt("$key.interval"),
                config.getBoolean("$key.display"),
                config.getString("$key.name"),
            )
        }

        // Start spawners
        val timeWaited: MutableMap<Spawner, Int> = mutableMapOf()
        event.game.gameData["spawnerTask"] = Bukkit.getScheduler().runTaskTimer(Main.instance, Runnable {
            if (!event.game.isRunning)
                return@Runnable

            spawners.forEach { spawner ->
                val waited = timeWaited[spawner] ?: 0
                timeWaited[spawner] = waited + 1

                // Spawn item
                if (waited >= spawner.interval) {
                    timeWaited[spawner] = 0
                    spawner.drop(world)
                }
            }
        }, 20, 20)
    }

    @EventHandler
    fun onWorldUnload(event: GameWorldUnloadEvent) {
        // Stop spawner task when world gets unloaded
        val task = event.game.gameData.get<BukkitTask>("spawnerTask") ?: return
        task.cancel()
    }
}