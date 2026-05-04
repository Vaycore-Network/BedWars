package de.c4vxl.bedwars.handler

import de.c4vxl.bedwars.Main
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Handles the translation of item names depending on the player holding the item
 */
class ItemTranslationHandler : Listener {
    companion object {
        /**
         * Registers a translatable item name
         * @param key The translation key of the item
         */
        fun ItemBuilder.translatable(key: String): ItemBuilder {
            this.editMeta { it.persistentDataContainer.set(NamespacedKey("bedwars", "translatable"), PersistentDataType.STRING, key) }
            return this
        }

        /**
         * Tries to translate a translatable item
         * @param item The item to translate
         * @param language The language to resolve translations in
         */
        fun translate(item: ItemStack, language: Language): ItemStack {
            // Try to get the translation key of the item
            val key = item.persistentDataContainer
                .get(NamespacedKey("bedwars", "translatable"), PersistentDataType.STRING)
                ?: return item

            // Get translation
            val translation = language.getCmp(key)

            // Update item display name
            val meta = item.takeIf { it.hasItemMeta() }?.itemMeta ?: return item
            meta.itemName(translation)
            item.setItemMeta(meta)

            return item
        }
    }

    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onPickup(event: EntityPickupItemEvent) {
        // Get the game
        val player = event.entity as? Player ?: return

        // Return if not in a game
        player.gma.game?.takeIf { it.isRunning } ?: return

        translate(event.item.itemStack, player.language.child("bedwars"))
    }

    @EventHandler
    fun onItemClick(event: InventoryClickEvent) {
        // Get the game
        val player = event.whoClicked as? Player ?: return

        // Return if not in a game
        player.gma.game?.takeIf { it.isRunning } ?: return

        // Translate item
        translate(event.currentItem ?: return, player.language.child("bedwars"))
    }
}