package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.gamemanager.gma.event.game.GameStartedEvent
import de.c4vxl.gamemanager.gma.event.game.GameWorldLoadedEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import org.bukkit.Bukkit
import org.bukkit.GameRules
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent

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
    fun onCraft(event: CraftItemEvent) {
        val player = event.whoClicked as? Player ?: return
        val game = player.gma.game ?: return

        event.isCancelled = true
        player.sendMessage(player.language.getCmp("game.crafting.disabled"))
    }

    @EventHandler
    fun onWorldLoaded(event: GameWorldLoadedEvent) {
        val world = event.map.world ?: return

        world.setGameRule(GameRules.KEEP_INVENTORY, true) // Item dropping will be handled separately
        world.setGameRule(GameRules.RANDOM_TICK_SPEED, 0)
        world.setGameRule(GameRules.ADVANCE_TIME, false)
        world.setGameRule(GameRules.ADVANCE_WEATHER, false)
        world.setGameRule(GameRules.SPAWN_MOBS, false)
        world.setGameRule(GameRules.SPAWN_MONSTERS, false)
        world.setGameRule(GameRules.SPAWN_WANDERING_TRADERS, false)
        world.setGameRule(GameRules.SPAWN_PHANTOMS, false)
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

    @EventHandler
    fun onHunger(event: FoodLevelChangeEvent) {
        val player = event.entity as? Player ?: return

        // Only in games
        if (!player.gma.isInGame)
            return

        event.isCancelled = true
    }

    @EventHandler
    fun onTnT(event: BlockPlaceEvent) {
        if (!event.player.gma.isInGame)
            return

        if (event.block.type != Material.TNT)
            return

        // Automatically prime the tnt
        event.block.type = Material.AIR
        event.block.world.spawnEntity(event.block.location.toCenterLocation(), EntityType.TNT)
    }
}