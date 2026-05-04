package de.c4vxl.bedwars.item

import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Snowball
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

/**
 * A custom projectile
 * Freezes players it hits for 5 seconds
 */
object Freezer {
    private var eventsInitialized = false

    fun item(language: Language) =
        ItemBuilder(
            Material.ICE,
            language.getCmp("item.custom.freezer.name"),
            lore = listOf(language.getCmp("item.custom.freezer.desc.1")),
            key = "bw_freezer"
        )
            .translatable("item.custom.freezer.name")
            .apply {
                if (!eventsInitialized)
                    onEvent(PlayerInteractEvent::class.java) { event ->
                        if (!event.action.isRightClick)
                            return@onEvent

                        // Handle cooldown
                        if (event.player.hasCooldown(event.item!!))
                            return@onEvent

                        event.player.setCooldown(event.item!!, 20 * 20)

                        // Throw projectile
                        event.player.launchProjectile(Snowball::class.java)
                            .persistentDataContainer
                            .set(NamespacedKey("bedwars", "item.freezer"), PersistentDataType.BOOLEAN, true)

                        event.player.playSound(event.player.location, Sound.ENTITY_SNOWBALL_THROW, 2f, 1f)

                        event.item!!.amount -= 1
                        event.isCancelled = true
                    }

                eventsInitialized = true
            }
}