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
    fun item(language: Language) =
        ItemBuilder(
            Material.TRAPPED_CHEST,
            lore = listOf(language.getCmp("item.custom.portable_shop.desc.1")),
            key = "bw_portable_shop"
        )
            .onEvent(PlayerInteractEvent::class.java) { event ->
                if (!event.action.isRightClick)
                    return@onEvent

                ShopUI(event.player).open()
            }
            .translatable("item.custom.portable_shop.name")
}