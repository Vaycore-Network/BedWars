package de.c4vxl.bedwars.ui

import de.c4vxl.bedwars.ui.type.ScrollableUI
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.meta.SkullMeta

/**
 * The ui for the player tracker
 */
class PlayerTrackerUI(
    val player: Player,
    private val onChoose: (Player) -> Unit,
    private val language: Language = player.language.child("bedwars"),
    private val game: Game = player.gma.game!!
) {
    fun open() {
        val team = player.gma.team ?: return

        val items = game.playerManager.alivePlayers
            .filterNot { it.team?.id == team.id || it.team == null }
            .map { player ->
                ItemBuilder(
                    Material.PLAYER_HEAD,
                    lore = listOf(
                        language.getCmp("ui.player_tracker.item.desc", player.team?.labelStr(language.root) ?: "")
                    )
                )
                    .editMeta { meta ->
                        (meta as SkullMeta).setOwningPlayer(player.bukkitPlayer)
                        meta.displayName(language.getCmp("ui.player_tracker.item.name", player.bukkitPlayer.name))
                    }
                    .onEvent(InventoryClickEvent::class.java) { event ->
                        event.isCancelled = true
                        this.player.closeInventory()
                        onChoose(player.bukkitPlayer)
                    }
                    .build()
            }

        ScrollableUI(items, language.getCmp("ui.player_tracker.title"), player)
            .open(0)
    }
}