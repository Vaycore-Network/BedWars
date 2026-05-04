package de.c4vxl.bedwars.item

import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.player.PlayerInteractEvent

/**
 * A custom booster item
 * When right-clicked a player will be boosted into the direction he is looking
 */
object Booster {
    fun item(language: Language) =
        ItemBuilder(
            Material.FLINT_AND_STEEL,
            lore = listOf(language.getCmp("item.custom.booster.desc.1"), language.getCmp("item.custom.booster.desc.2"))
        )
            .onEvent(PlayerInteractEvent::class.java) { event ->
                if (!event.action.isRightClick)
                    return@onEvent

                event.isCancelled = true

                if (event.player.hasCooldown(event.item!!)) {
                    event.player.playSound(event.player.location, Sound.BLOCK_ANVIL_PLACE, 2f, 1f)
                    return@onEvent
                }

                event.player.velocity = event.player.eyeLocation.direction.multiply(1.5)
                event.player.setCooldown(event.item!!, 5 * 20)
            }
            .translatable("item.custom.booster.name")
}