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
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.abs

/**
 * A custom bridge item
 * When placed it automatically constructs an 8 block long bridge in front of the player
 */
object InstantBridge {
    private var eventsInitialized = false

    fun item(language: Language) =
        ItemBuilder(
            Material.SANDSTONE_SLAB,
            language.getCmp("item.custom.instant_bridge.name"),
            lore = listOf(language.getCmp("item.custom.instant_bridge.desc.1")),
            key = "bw_instant_bridge"
        )
            .translatable("item.custom.instant_bridge.name")
            .apply {
                if (!eventsInitialized)
                    onEvent(PlayerInteractEvent::class.java) { event ->
                        if (!event.action.isRightClick)
                            return@onEvent

                        event.isCancelled = true

                        val team = event.player.gma.team ?: return@onEvent

                        constructBridge(
                            event.player,
                            8,
                            team.getBlockVariant(TeamData.BlockVariant.PRIMARY)
                        )

                        event.item!!.amount -= 1
                    }

                eventsInitialized = true
            }

    /**
     * Calculates the direction a player is looking
     * @param player The player
     */
    fun calculateDirection(player: Player): BlockFace {
        val direction = player.eyeLocation.direction
        val x = direction.x
        val z = direction.z

        return when {
            x > 0 && abs(x) > abs(z) -> BlockFace.EAST
            x < 0 && abs(x) > abs(z) -> BlockFace.WEST
            z > 0 && abs(x) < abs(z) -> BlockFace.SOUTH
            else                     -> BlockFace.NORTH
        }
    }

    /**
     * Constructs the instant bridge
     * @param player The player who constructed it
     * @param length The length of the bridge
     * @param material The material of the bridge
     */
    private fun constructBridge(player: Player, length: Int, material: Material) {
        val game = player.gma.game ?: return
        val face = calculateDirection(player)
        val startBlock = player.location.clone().subtract(0.0, 1.0, 0.0).block

        for (i in 1..length) {
            Bukkit.getScheduler().runTaskLater(Main.instance, Runnable {
                startBlock.getRelative(face, i)
                    .takeIf { it.type.isAir || it.isLiquid || !it.type.isSolid }
                    ?.let {
                        // Track blocks as "placed by a player"
                        MapHandler.track(game, it)

                        it.type = material
                    }
            }, i * 2L)
        }
    }
}