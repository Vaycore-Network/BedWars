package de.c4vxl.bedwars.item

import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.bedwars.ui.ShopUI
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent

/**
 * A portable shop item
 */
object PortableShop {
    private var eventsInitialized = false

    fun item(language: Language) =
        ItemBuilder(
            Material.TRAPPED_CHEST,
            language.getCmp("item.custom.portable_shop.name"),
            lore = listOf(language.getCmp("item.custom.portable_shop.desc.1")),
            key = "bw_portable_shop"
        )
            .translatable("item.custom.portable_shop.name")
            .apply {
                if (!eventsInitialized)
                    onEvent(PlayerInteractEvent::class.java) { event ->
                        if (!event.action.isRightClick)
                            return@onEvent

                        ShopUI(event.player).open()
                        event.item!!.amount -= 1
                    }

                eventsInitialized = true
            }
}