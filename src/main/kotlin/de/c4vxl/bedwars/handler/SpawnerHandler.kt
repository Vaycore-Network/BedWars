package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.data.Spawner
import de.c4vxl.bedwars.utils.HologramUtils.setHologramText
import de.c4vxl.gamemanager.gma.event.game.GameStartedEvent
import de.c4vxl.gamemanager.gma.event.game.GameWorldLoadedEvent
import de.c4vxl.gamemanager.gma.event.game.GameWorldUnloadEvent
import de.c4vxl.gamemanager.language.Language.Companion.language
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
    fun onStarted(event: GameStartedEvent) {
        val world = event.game.worldManager.map?.world ?: return

        // Get spawners
        val spawners: List<Spawner> = event.game.gameData["spawners"] ?: run {
            Main.logger.warning("Failed to access spawners of game ${event.game.id}")
            return
        }

        // Create holograms
        val holograms = spawners.associateWith { it.render(event.game) }

        // Start spawners
        val timeWaited: MutableMap<Spawner, Int> = mutableMapOf()
        event.game.gameData["spawnerTask"] = Bukkit.getScheduler().runTaskTimer(Main.instance, Runnable {
            if (!event.game.isRunning)
                return@Runnable

            spawners.forEach { spawner ->
                val waited = timeWaited[spawner] ?: 0
                timeWaited[spawner] = waited + 1

                // Update holograms
                val dwait = spawner.interval - waited
                holograms[spawner]?.forEach { (player, entity) ->
                    player.setHologramText(entity, player.language.child("bedwars").getCmp("spawner.display.name",
                        "${dwait.takeIf { it != 0 } ?: spawner.interval}"))
                }

                // Spawn item
                if (waited >= spawner.interval) {
                    timeWaited[spawner] = 1
                    spawner.drop(world)
                }
            }
        }, 20, 20)
    }

    @EventHandler
    fun onWorldLoaded(event: GameWorldLoadedEvent) {
        val world = event.map.world ?: return

        // Read spawner data
        val config = event.map.getMetadata("bedwars.spawners") ?: return
        event.game.gameData["spawners"] = config.getKeys(false).mapNotNull { key ->
            val (x, y, z) = config.getIntegerList("$key.location")
            Spawner(
                Location(world, x + 0.5, y + 0.5, z + 0.5),
                config.getString("$key.material")?.let { Material.getMaterial(it) } ?: return@mapNotNull null,
                config.getInt("$key.interval"),
                config.getBoolean("$key.display.enabled"),
                config.getString("$key.display.name"),
                config.getString("$key.display.material")?.let { Material.getMaterial(it) }
            )
        }
    }

    @EventHandler
    fun onWorldUnload(event: GameWorldUnloadEvent) {
        // Stop spawner task when world gets unloaded
        val task = event.game.gameData.get<BukkitTask>("spawnerTask") ?: return
        task.cancel()
    }
}