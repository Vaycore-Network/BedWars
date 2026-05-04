package de.c4vxl.bedwars.ui.type

import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class ScrollableUI(
    private val items: List<ItemStack>,
    private val title: Component,
    private val player: Player,
    private val numRows: Int = 1
) {
    private val itemsPerPage = numRows * 7
    private val totalPages: Int = (items.size + itemsPerPage - 1) / itemsPerPage

    private var marginItem = ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, Component.empty())
        .onEvent(InventoryClickEvent::class.java) { it.isCancelled = true }.build()

    private val baseInventory = Bukkit.createInventory(null, (numRows + 2) * 9, title)
        .apply {
            // Add margin items
            val lastRowFirstSlot = this.size - 9
            for (range in listOf(
                0..8,
                0..lastRowFirstSlot step 9,
                8..<this.size step 9,
                lastRowFirstSlot..<this.size
            )) {
                range.forEach { setItem(it, marginItem) }
            }
        }

    fun open(page: Int) {
        // Build page
        val inv = baseInventory.apply {
            // Add navigation items
            if (page > 0)
                setItem(0, ItemBuilder(Material.ARROW, player.language.child("bedwars").getCmp("ui.scrollable.previous"))
                    .onEvent(InventoryClickEvent::class.java) { open(page - 1) }
                    .build())

            if (page < totalPages - 1)
                setItem(8, ItemBuilder(Material.ARROW, player.language.child("bedwars").getCmp("ui.scrollable.next"))
                    .onEvent(InventoryClickEvent::class.java) { open(page + 1) }
                    .build())

            // Add items
            val start = page * itemsPerPage
            items.drop(start)
                .take(itemsPerPage)
                .forEach { addItem(it) }
        }

        // Open page
        player.openInventory(inv)
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f)
    }
}