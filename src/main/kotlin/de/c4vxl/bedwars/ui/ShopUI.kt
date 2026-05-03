package de.c4vxl.bedwars.ui

import de.c4vxl.bedwars.data.shop.ShopData
import de.c4vxl.bedwars.data.TeamData
import de.c4vxl.bedwars.data.TeamData.getBlockVariant
import de.c4vxl.bedwars.utils.InventoryUtils.countMaterial
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.gma.team.Team
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.floor
import kotlin.math.min

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
                        .apply {
                            lore = listOf(
                                language.getCmp("ui.shop.buyable.lore.1", it.value.cost.toString(), it.value.currency.translationKey()),
                                Component.empty(),
                                language.getCmp("ui.shop.buyable.lore.2"),
                                language.getCmp("ui.shop.buyable.lore.3")
                            )
                        }
                        .onEvent(InventoryClickEvent::class.java) { event ->
                            event.isCancelled = true

                            // Only accept left clicks
                            if (!event.isLeftClick && event.hotbarButton == -1)
                                return@onEvent

                            val currencyPresent = player.inventory.countMaterial(it.value.currency)
                            val buyAmount = when (event.action) {
                                InventoryAction.MOVE_TO_OTHER_INVENTORY -> min(
                                    floor(currencyPresent / it.value.cost.toFloat()).toInt(),                        // How often the item can be bought
                                    floor(event.currentItem!!.type.maxStackSize / it.value.amount.toFloat()).toInt() // Maximum amount needed to buy a full stack
                                )
                                else -> if (currencyPresent >= it.value.cost) 1 else 0
                            }

                            // Player can't afford the item
                            if (buyAmount == 0) {
                                player.sendMessage(language.getCmp("game.shop.buy.fail"))
                                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 3f)
                                return@onEvent
                            }

                            // Create item
                            val item = it.value.builder(player.gma).apply { amount = buyAmount * it.value.amount }
                                .editMeta { meta -> meta.persistentDataContainer.remove(NamespacedKey("gma", "itembuilder")) }
                                .build()

                            var slot = event.hotbarButton

                            // Slot will always be chest with armor
                            if (page == Page.ARMOR) {
                                slot = 38

                                // Return if downgrade
                                val tiers = listOf(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE)
                                if (tiers.indexOf(player.inventory.getItem(slot)?.type) >= tiers.indexOf(event.currentItem?.type)) {
                                    player.sendMessage(language.getCmp("game.shop.armor.buy.fail"))
                                    return@onEvent
                                }

                                player.inventory.setItem(slot, null)
                            }

                            // Give item
                            if (slot != -1) {
                                val previous = player.inventory.getItem(slot)
                                player.inventory.setItem(slot, item)
                                previous?.let { player.inventory.addItem(previous) }
                            } else
                                player.inventory.addItem(item)

                            // Remove currency
                            player.inventory.removeItemAnySlot(ItemStack(it.value.currency, it.value.cost * buyAmount))
                        }
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