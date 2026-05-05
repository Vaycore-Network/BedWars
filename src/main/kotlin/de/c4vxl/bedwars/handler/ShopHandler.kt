package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.ui.ShopUI
import de.c4vxl.bedwars.utils.NPCUtils
import de.c4vxl.bedwars.utils.NPCUtils.injectPacketHandler
import de.c4vxl.bedwars.utils.NPCUtils.renderNPC
import de.c4vxl.bedwars.utils.NPCUtils.sendNPCRotation
import de.c4vxl.bedwars.utils.NPCUtils.skin
import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.event.game.GameStartedEvent
import de.c4vxl.gamemanager.gma.event.game.GameWorldLoadedEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerSpectateStartEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import net.minecraft.network.protocol.game.ServerboundInteractPacket
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Handles the shops:
 * - At the beginning of the game this handler will spawn the shops
 * - Handles shop interactions
 */
class ShopHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)

        // Make NPCs always face player
        Bukkit.getScheduler().runTaskTimer(Main.instance, Runnable {
            GMA.registeredGames.forEach { game ->
                val shops: Map<Player, List<Pair<Location, Int>>> = game.gameData["shopEntities"] ?: mutableMapOf()

                shops.forEach { (player, playerShops) ->
                    playerShops.forEach { (loc, npc) ->
                        if (loc.world != player.location.world)
                            return@forEach

                        if (loc.distance(player.location) <= 15)
                            player.sendNPCRotation(npc, NPCUtils.calculateYaw(player.location, loc))
                    }
                }
            }
        }, 0, 5)
    }

    /**
     * Renders all shops to a player
     * @param player The player to send the shops to
     */
    private fun renderShops(player: Player) {
        val game = player.gma.game ?: return
        val shops: List<Location> = game.gameData["shops"] ?: return
        val lang = player.language.child("bedwars")

        val shopEntities: MutableMap<Player, List<Pair<Location, Int>>> = game.gameData["shopEntities"] ?: mutableMapOf()

        shopEntities[player] = shops.map {
            it to player.renderNPC(it, player.skin, lang.getCmp("game.shop.name"))
        }

        game.gameData["shopEntities"] = shopEntities
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.injectPacketHandler("npc_interact", ServerboundInteractPacket::class.java) { packet ->
            val game = event.player.gma.game ?: return@injectPacketHandler
            val id = packet.entityId
            val shops: Map<Player, List<Pair<Location, Int>>> = game.gameData["shopEntities"] ?: mutableMapOf()

            // NPC is not a shop
            if (shops[event.player]?.find { it.second == id } == null)
                return@injectPacketHandler

            // Open shop ui
            Bukkit.getScheduler().callSyncMethod(Main.instance) {
                ShopUI(event.player).open()
            }
        }
    }

    @EventHandler
    fun onStarted(event: GameStartedEvent) {
        val world = event.game.worldManager.map?.world ?: return

        // Send shops
        event.game.players.forEach { renderShops(it.bukkitPlayer) }
    }

    @EventHandler
    fun onSpectate(event: GamePlayerSpectateStartEvent) {
        if (!event.game.isRunning) return

        renderShops(event.player.bukkitPlayer)
    }

    @EventHandler
    fun onWorldLoaded(event: GameWorldLoadedEvent) {
        val world = event.map.world ?: return

        // Load shop locations
        val config = event.map.getMetadata("bedwars.shops") ?: return
        event.game.gameData["shops"] = config.getKeys(false).map { key ->
            val (x, y, z) = config.getIntegerList(key)
            Location(world, x + 0.5, y.toDouble(), z + 0.5)
        }
    }
}