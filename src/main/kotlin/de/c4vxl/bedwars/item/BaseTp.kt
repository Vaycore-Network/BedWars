package de.c4vxl.bedwars.item

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

/**
 * A custom teleportation item
 * Teleports players to their base
 */
object BaseTp {
    private var eventsInitialized = false

    fun item(language: Language) =
        ItemBuilder(
            Material.GUNPOWDER,
            language.getCmp("item.custom.base_tp.name"),
            lore = listOf(language.getCmp("item.custom.base_tp.desc.1")),
            key = "bw_base_tp"
        )
            .translatable("item.custom.base_tp.name")
            .apply {
                if (!eventsInitialized)
                    onEvent(PlayerInteractEvent::class.java) { event ->
                        if (!event.action.isRightClick)
                            return@onEvent

                        // Handle cooldown
                        if (event.player.hasCooldown(event.item!!))
                            return@onEvent

                        event.player.setCooldown(event.item!!, 20 * 25)

                        // Teleport
                        val team = event.player.gma.team ?: return@onEvent
                        val spawn = team.manager.game.worldManager.map?.getSpawnLocation(team.id) ?: return@onEvent
                        teleport(event.player, spawn, 5, event.item?.clone())

                        event.item!!.amount -= 1
                        event.isCancelled = true
                    }

                eventsInitialized = true
            }

    /**
     * Starts the base teleport
     * @param player The player to teleport
     * @param location The location to teleport the player to
     * @param seconds The amount of seconds the teleport should take
     */
    private fun teleport(player: Player, location: Location, seconds: Int, item: ItemStack? = null) {
        val initialLocation = player.location.clone()
        val lang = player.language.child("bedwars")

        var remaining = seconds

        object : BukkitRunnable() {
            override fun run() {
                // Player has moved
                if (initialLocation.blockX != player.location.blockX ||
                    initialLocation.blockY != player.location.blockY ||
                    initialLocation.blockZ != player.location.blockZ) {
                    player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 2f, 1f)
                    item?.let { player.inventory.addItem(it) }
                    player.sendMessage(lang.getCmp("game.base_tp.cancelled"))
                    cancel()
                    return
                }

                // Wrong world
                if (player.world != location.world) {
                    cancel()
                    return
                }

                // Wait time over
                if (remaining <= 0) {
                    player.teleport(location)
                    player.world.players.forEach {
                        it.playSound(it.location, Sound.ENTITY_PLAYER_TELEPORT, 2f, 1f)
                    }
                    cancel()
                    return
                }

                // Send status
                player.sendActionBar(lang.getCmp("game.base_tp.notice", remaining.toString()))

                remaining -= 1
            }
        }.runTaskTimer(Main.instance, 0, 20)
    }
}