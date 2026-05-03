package de.c4vxl.bedwars.ui

import de.c4vxl.bedwars.data.shop.ShopData
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
    private val language: Language = player.language.child("bedwars"),
    private val team: Team = player.gma.team!!
) {
    enum class Page {
        BLOCKS,
        WEAPONS,
        ARMOR,
        CONSUMABLES,
        SPECIAL
    }

    private var marginItem = ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, Component.empty())
        .onEvent(InventoryClickEvent::class.java) { it.isCancelled = true }.build()

    val baseInventory get() = Bukkit.createInventory(null, 5 * 9, language.getCmp("ui.shop.title"))
        .apply {
            // Add margin items
            for (i in 0..44)
                setItem(i, marginItem)

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
        // Build page
        val inv = baseInventory.apply {
            ShopData.pages[page]?.forEach {
                try {
                    setItem(it.key, it.value.builder(player.gma)
                        .build())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Open page
        player.openInventory(inv)

        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
    }
}