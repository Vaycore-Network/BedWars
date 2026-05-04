package de.c4vxl.bedwars.item

import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Fireball
import org.bukkit.event.player.PlayerInteractEvent

/**
 * A custom fireball item
 * A throwable explosive
 */
object Fireball {
    fun item(language: Language) =
        ItemBuilder(
            Material.FLINT_AND_STEEL,
            lore = listOf(language.getCmp("item.custom.fireball.desc.1")),
            key = "bw_fireball"
        )
            .onEvent(PlayerInteractEvent::class.java) { event ->
                if (!event.action.isRightClick)
                    return@onEvent

                // Spawn fireball
                (event.player.location.world.spawnEntity(
                    event.player.eyeLocation.add(event.player.eyeLocation.direction),
                    EntityType.FIREBALL
                ) as Fireball).let {
                    it.isVisualFire = true
                    it.yield = 3.0F
                    it.velocity = event.player.eyeLocation.direction.multiply(2.5)
                }

                event.item!!.amount -= 1
                event.isCancelled = true
            }
            .translatable("item.custom.fireball.name")
}