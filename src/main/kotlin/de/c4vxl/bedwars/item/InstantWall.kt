package de.c4vxl.bedwars.item

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.data.TeamData
import de.c4vxl.bedwars.data.TeamData.getBlockVariant
import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.bedwars.handler.MapHandler
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent

/**
 * A custom block
 * When placed it automatically constructs a 5x5 wall
 */
object InstantWall {
    private var eventsInitialized = false

    fun item(language: Language) =
        ItemBuilder(
            Material.SANDSTONE_WALL,
            language.getCmp("item.custom.instant_wall.name"),
            lore = listOf(language.getCmp("item.custom.instant_wall.desc.1")),
            key = "bw_instant_wall"
        )
            .translatable("item.custom.instant_wall.name")
            .apply {
                if (!eventsInitialized)
                    onEvent(BlockPlaceEvent::class.java) { event ->
                        val team = event.player.gma.team ?: return@onEvent

                        val block = team.getBlockVariant(TeamData.BlockVariant.PRIMARY)
                        constructWall(
                            event.player,
                            event.blockPlaced,
                            block
                        )
                        event.blockPlaced.type = block
                    }

                eventsInitialized = true
            }

    /**
     * Returns the perpendicular block face
     */
    val BlockFace.perpendicular get() =
        when (this) {
            BlockFace.NORTH -> BlockFace.EAST
            BlockFace.SOUTH -> BlockFace.WEST
            BlockFace.EAST  -> BlockFace.SOUTH
            else            -> BlockFace.NORTH
        }

    /**
     * Constructs the 5x5 wall
     * @param player The player who constructed it
     * @param startBlock The bottom center block
     * @param material The material of the bridge
     */
    private fun constructWall(player: Player, startBlock: Block, material: Material) {
        val game = player.gma.game ?: return
        val playerDirection = InstantBridge.calculateDirection(player)
        val right = playerDirection.perpendicular

        for (y in 0 until 4) {
            Bukkit.getScheduler().runTaskLater(Main.instance, Runnable {
                for (x in -2..2) {
                    startBlock
                        .getRelative(BlockFace.UP, y)
                        .getRelative(right, x)
                        .takeIf { it.type.isAir || it.isLiquid || !it.type.isSolid }
                        ?.let {
                            // Track blocks as "placed by a player"
                            MapHandler.track(game, it)
                            it.type = material
                        }
                }
            }, 3L * y)
        }
    }
}