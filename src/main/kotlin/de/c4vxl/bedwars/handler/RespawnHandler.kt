package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.data.TeamData
import de.c4vxl.bedwars.data.TeamData.equipTeamArmor
import de.c4vxl.bedwars.data.TeamData.getBlockVariant
import de.c4vxl.gamemanager.gma.event.player.GamePlayerDeathEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerEquipEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerRespawnEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.gma.team.Team
import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import kotlin.math.abs

/**
 * Handles the games respawn mechanic:
 *
 * Players are able to respawn as long as their bed hasn't been destroyed
 */
class RespawnHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    companion object {
        /**
         * Returns {@code true} if the team can still respawn
         */
        val Team.canRespawn: Boolean get() =
            this.players.isNotEmpty() &&
            this.manager.game.gameData.get<Boolean>("destroyed.${this.id}") != true
    }

    @EventHandler
    fun onEquip(event: GamePlayerEquipEvent) {
        val armor = event.player.bukkitPlayer.inventory.armorContents

        // Reset player
        event.player.reset()
        event.player.bukkitPlayer.saturation = 10F

        // Equip armor
        if (event.reason == GamePlayerEquipEvent.Reason.RESPAWN)
            event.player.bukkitPlayer.inventory.armorContents = armor
        else if (event.reason == GamePlayerEquipEvent.Reason.GAME_START)
            event.player.equipTeamArmor()

        // Equip sword
        event.player.bukkitPlayer.inventory.setItem(0, ItemBuilder(Material.WOODEN_SWORD).build())

        // Apply upgrades
        UpgradesHandler.upgradeItem(event.player.bukkitPlayer, event.player.bukkitPlayer.inventory.chestplate!!)
        UpgradesHandler.upgradeItem(event.player.bukkitPlayer, event.player.bukkitPlayer.inventory.getItem(0)!!)
    }

    @EventHandler
    fun onDeath(event: GamePlayerDeathEvent) {
        val keep = listOf(
            Material.IRON_INGOT,
            Material.BRICK,
            Material.GOLD_INGOT,
            Material.FLINT_AND_STEEL,
            event.player.team?.getBlockVariant(TeamData.BlockVariant.PRIMARY)
        )

        event.deathEvent.keepInventory = true

        // Give specific item types to killer
        event.player.bukkitPlayer.killer?.let { killer ->
            event.player.bukkitPlayer.inventory.storageContents
                .filterNotNull()
                .filter { keep.contains(it.type) }
                .forEach {
                    killer.inventory.addItem(it)
                }
        }
    }

    @EventHandler
    fun onRespawn(event: GamePlayerRespawnEvent) {
        // Get team of player
        val team = event.player.team ?: return

        // Return if bed of the team hasn't been destroyed yet
        if (team.canRespawn) {
            event.game.broadcastMessage(
                "game.player.${if (event.killer == null) "death" else "killed"}",
                event.player.bukkitPlayer.name,
                event.killer?.bukkitPlayer?.name ?: "???",
                child = "bedwars"
            )
            return
        }

        // Eliminate player
        event.player.eliminate(event.killer)

        // Broadcast elimination message
        event.game.broadcastMessage(
            "game.player.${if (event.killer == null) "eliminated" else "eliminated_by"}",
            event.player.bukkitPlayer.name,
            event.killer?.bukkitPlayer?.name ?: "???",
            child = "bedwars"
        )
    }

    @EventHandler
    fun onBedDestroy(event: BlockBreakEvent) {
        // Only act if block broken was a bed
        if (!event.block.type.name.endsWith("_BED")) return

        // Get game
        val player = event.player.gma
        val lang = player.language.child("bedwars")
        val game = player.game?.takeIf { it.isRunning } ?: return

        // Get team of the bed
        val config = game.worldManager.map?.getMetadata("bedwars") ?: run {
            Main.logger.warning("Failed to access 'bedwars' metadata in map '${game.worldManager.map?.name}'")
            return
        }
        val teamId = (0 until game.size.teamAmount).find { i ->
            val (x, y, z) = config.getIntegerList("beds.$i")
            abs(x - event.block.x) <= 1 && abs(y - event.block.y) <= 1 && abs(z - event.block.z) <= 1
        }

        // Get team from id
        val team = game.teamManager.teams[teamId]?.takeIf { it.players.isNotEmpty() } ?: run {
            event.player.sendMessage(lang.getCmp("game.bed.destroy.fail.invalid_team"))
            event.isCancelled = true
            return
        }

        // Prevent player from destroying own bed
        if (team.players.contains(player)) {
            event.player.sendMessage(lang.getCmp("game.bed.destroy.fail.own_team"))
            event.isCancelled = true
            return
        }

        // Mark bed as destroyed
        game.gameData["destroyed.${teamId}"] = true
        event.isDropItems = false
        event.isCancelled = false

        // Broadcast to game
        game.players.forEach { audience ->
            // Send chat notification
            audience.bukkitPlayer.sendMessage(
                audience.language.child("bedwars")
                    .getCmp("game.bed.destroy.broadcast.global", team.labelStr(audience.language), event.player.name)
            )

            // Play sound
            audience.bukkitPlayer.playSound(audience.bukkitPlayer.location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f)
        }

        // Broadcast to players
        team.players.forEach { audience ->
            val language = audience.language.child("bedwars")
            audience.bukkitPlayer.sendTitlePart(TitlePart.TITLE, language.getCmp("game.bed.destroy.broadcast.team.title"))
            audience.bukkitPlayer.sendTitlePart(TitlePart.SUBTITLE, language.getCmp("game.bed.destroy.broadcast.team.subtitle"))
        }

        // Update scoreboard
        ScoreboardHandler.update(game)
    }
}