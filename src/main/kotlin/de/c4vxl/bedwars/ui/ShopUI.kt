package de.c4vxl.bedwars.ui

import de.c4vxl.bedwars.data.TeamData
import de.c4vxl.bedwars.data.TeamData.getBlockVariant
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.gma.team.Team
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ShopUI(
    val player: Player,
    language: Language = player.language.child("bedwars"),
    team: Team = player.gma.team!!
) {
    enum class Page {
        BLOCKS,
        WEAPONS,
        ARMOR,
        CONSUMABLES,
        SPECIAL
    }

    private var marginItem = ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, Component.empty()).build()

    val baseInventory = Bukkit.createInventory(null, 5 * 9, language.getCmp("ui.shop.title"))
        .apply {
            // Add margin items
            for (range in listOf(
                0..16,
                36..44,
                0..36 step 9,
                8..44 step 9
            )) {
                for (i in range) { setItem(i, marginItem) }
            }

            // Add tab items
            listOf(
                Page.BLOCKS to team.getBlockVariant(TeamData.BlockVariant.PRIMARY),
                Page.WEAPONS to Material.IRON_SWORD,
                Page.ARMOR to Material.IRON_CHESTPLATE,
                Page.CONSUMABLES to Material.POTION,
                Page.SPECIAL to Material.NETHER_STAR
            ).forEachIndexed { i, (tab, display) ->
                setItem(2 + i, ItemBuilder(display)
                    .onEvent(InventoryClickEvent::class.java) { open(tab) }
                    .editMeta { it.customName(language.getCmp("ui.shop.tab.${tab.name.lowercase()}")) }
                    .build())
            }
        }

    fun open(page: Page = Page.BLOCKS) {
        player.openInventory(baseInventory)
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
    }
}