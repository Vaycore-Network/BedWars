package de.c4vxl.bedwars.item

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.data.TeamData
import de.c4vxl.bedwars.data.TeamData.getBlockVariant
import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.bedwars.handler.MapHandler
import de.c4vxl.bedwars.item.InstantBridge.calculateDirection
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

/**
 * A custom bridge item
 * When placed it automatically constructs an 8 block long stair-bridge in front of the player
 */
object InstantStairs {
    private var eventsInitialized = false

    fun item(language: Language) =
        ItemBuilder(
            Material.SANDSTONE_STAIRS,
            language.getCmp("item.custom.instant_stairs.name"),
            lore = listOf(language.getCmp("item.custom.instant_stairs.desc.1")),
            key = "bw_instant_stairs"
        )
            .translatable("item.custom.instant_stairs.name")
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
     * Constructs the instant bridge
     * @param player The player who constructed it
     * @param length The length of the bridge
     * @param material The material of the bridge
     */
    private fun constructBridge(player: Player, length: Int = 8, material: Material) {
        val game = player.gma.game ?: return
        val face = calculateDirection(player)
        var target = player.location.clone().subtract(0.0, 1.0, 0.0).block

        for (i in 1..length + 3) {
            Bukkit.getScheduler().runTaskLater(Main.instance, Runnable {
                target = if (i % 4 == 0) target.getRelative(BlockFace.UP) else target.getRelative(face)

                target
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