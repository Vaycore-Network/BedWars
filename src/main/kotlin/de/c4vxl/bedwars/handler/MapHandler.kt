package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent

/**
 * Handles map breaking:
 * Blocks of the map actual game map shouldn't be breakable.
 * This handler keeps track of blocks that were placed by a player during the game and
 * stops players from breaking blocks that weren't tracked as such
 */
class MapHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    companion object {
        /**
         * Holds a list of all blocks placed by a player
         */
        private val Game.blocksPlaced: MutableList<Block> get() =
            this.gameData["blocksPlaced"] ?: mutableListOf()

        /**
         * Tries to find the game a world belongs to
         */
        private val World.game: Game? get() =
            GMA.registeredGames.find { this.name.endsWith(it.id.asString) }

        /**
         * Adds a block to the tracked blocks of a game
         * @param game The game
         * @param block The block
         */
        fun track(game: Game, block: Block) {
            game.gameData["blocksPlaced"] = game.blocksPlaced.apply { add(block) }
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        // Get game
        val game = event.player.gma.game?.takeIf { it.isRunning } ?: return

        // Allow placing blocks
        event.isCancelled = false

        // Add block
        track(game, event.blockPlaced)
    }

    @EventHandler
    fun onExplode(event: BlockExplodeEvent) {
        // Remove all map blocks
        // Stops block explosions from destroying the actual map
        event.blockList().removeIf { block ->
            val game = block.world.game ?: return@removeIf false
            !game.blocksPlaced.contains(block)
        }
    }

    @EventHandler
    fun onExplode(event: EntityExplodeEvent) {
        // Remove all map blocks
        // Stops entity explosions from destroying the actual map
        event.blockList().removeIf { block ->
            val game = block.world.game ?: return@removeIf false
            !game.blocksPlaced.contains(block)
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        // Get game
        val game = event.player.gma.game?.takeIf { it.isRunning } ?: return

        // Let creative players break map blocks
        if (event.player.gameMode == GameMode.CREATIVE)
            return

        // Return if block was placed by a player
        if (game.blocksPlaced.contains(event.block))
            return

        // Allow breaking of beds
        if (event.block.type.name.endsWith("_BED"))
            return

        // Let player destroy non-solid blocks
        // Stuff like grass or flowers
        if (!event.block.type.isSolid && listOf("_CARPET", "STRING").none { event.block.type.name.contains(it) }) {
            event.isDropItems = false
            return
        }

        // Warn player
        event.player.sendMessage(event.player.language.child("bedwars").getCmp("game.map.break.warning"))

        // Cancel
        event.isCancelled = true
    }
}