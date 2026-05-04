package de.c4vxl.bedwars.item

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.scheduler.BukkitRunnable

/**
 * A custom block
 * These blocks destroy themselves
 */
object CloudBlock {
    private var eventsInitialized = false

    fun item(language: Language) =
        ItemBuilder(
            Material.SNOW_BLOCK,
            language.getCmp("item.custom.cloud_block.name"),
            lore = listOf(language.getCmp("item.custom.cloud_block.desc.1")),
            key = "bw_cloud_block"
        )
            .translatable("item.custom.cloud_block.name")
            .apply {
                if (!eventsInitialized) {
                    onEvent(BlockPlaceEvent::class.java) { event ->
                        animateBlockBreak(event.block, 3)
                    }
                }

                eventsInitialized = true
            }

    /**
     * Animates a block breaking
     * @param block The block to break
     * @param duration The duration in seconds
     * @param maxStages The amount of "breaking" stages
     */
    private fun animateBlockBreak(block: Block, duration: Int, maxStages: Int = 9) {
        var stage = 0
        object : BukkitRunnable() {
            override fun run() {
                if (stage > maxStages) {
                    sendBlockBreak(block, 0f)
                    block.type = Material.AIR
                    cancel()
                    return
                }

                sendBlockBreak(block, stage / maxStages.toFloat())
                stage++
            }
        }.runTaskTimer(Main.instance, 0, (duration * 20) / maxStages.toLong())
    }

    /**
     * Sends a block break animation to all players
     * @param block The block to break
     * @param progress The progress
     */
    private fun sendBlockBreak(block: Block, progress: Float) {
        val hash = block.location.hashCode()
        block.world.players.forEach {
            it.sendBlockDamage(block.location, progress, hash)
        }
    }
}