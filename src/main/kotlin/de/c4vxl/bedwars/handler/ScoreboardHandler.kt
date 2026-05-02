package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.handler.RespawnHandler.Companion.canRespawn
import de.c4vxl.gamemanager.gma.event.player.GamePlayerEquipEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerSpectateStartEvent
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot

/**
 * Takes care of the sidebar scoreboard
 */
class ScoreboardHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    companion object {
        /**
         * Renders a scoreboard to a player
         */
        private fun display(player: Player, vararg lines: Component) {
            // Create new scoreboard to prevent two players having the same one
            // Players should have their own scoreboard either way since GMA usually handles this
            if (player.scoreboard == Bukkit.getScoreboardManager().mainScoreboard)
                player.scoreboard = Bukkit.getScoreboardManager().newScoreboard

            // Create objective
            val objective = "bedwars_${player.uniqueId}".let {
                player.scoreboard.getObjective(it)?.unregister()
                player.scoreboard.registerNewObjective(it, Criteria.DUMMY, player.language.child("bedwars").getCmp("game.scoreboard.title"))
            }
            objective.displaySlot = DisplaySlot.SIDEBAR

            // Display lines
            lines.reversed().forEachIndexed { i, component ->
                objective.getScore(LegacyComponentSerializer.legacySection().serialize(component)).score = i
            }
        }

        /**
         * Renders the game scoreboard to a player
         * @param player The player to render the scoreboard to
         */
        fun render(player: Player) {
            val game = player.gma.game ?: return
            val language = player.language.child("bedwars")

            // Create team lines
            val lines = game.teamManager.teams.values.map { team ->
                val playersAlive = team.players.count { !it.isEliminated }

                language.getCmp(
                    "game.scoreboard.team.${
                        if (team.canRespawn) "bed"
                        else if (playersAlive >= 1) "remaining"
                        else "dead"
                    }",
                    team.labelStr(player.language),
                    playersAlive.toString()
                ).let {
                    if (team.players.contains(player.gma))
                        it.append(language.getCmp("game.scoreboard.team.self"))
                    else
                        it
                }
            }

            // Display lines
            display(
                player,
                Component.empty(),
                *lines.toTypedArray(),
                Component.text(" "),
                language.getCmp("game.scoreboard.map", game.worldManager.map?.name ?: "/")
            )
        }

        /**
         * Reloads the scoreboard for every player in a game
         * @param game The game to reload for
         */
        fun update(game: Game) {
            game.players.forEach { render(it.bukkitPlayer) }
        }
    }

    @EventHandler
    fun onSpectate(event: GamePlayerSpectateStartEvent) {
        render(event.player.bukkitPlayer)
    }

    @EventHandler
    fun onEquip(event: GamePlayerEquipEvent) {
        Bukkit.getScheduler().callSyncMethod(Main.instance) {
            render(event.player.bukkitPlayer)
        }
    }
}