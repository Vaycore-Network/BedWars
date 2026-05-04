package de.c4vxl.bedwars.item

import de.c4vxl.bedwars.data.TeamData
import de.c4vxl.bedwars.data.TeamData.getBlockVariant
import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

/**
 * A custom item
 * When used: places a plattform under the player
 */
object LastChance {
    fun item(language: Language) =
        ItemBuilder(
            Material.BLAZE_ROD,
            lore = listOf(language.getCmp("item.custom.last_chance.desc.1"), language.getCmp("item.custom.last_chance.desc.2"))
        )
            .onEvent(PlayerInteractEvent::class.java) { event ->
                if (!event.action.isRightClick)
                    return@onEvent

                event.isCancelled = true

                placePlattform(event.player, event.item!!)
                event.item!!.amount -= 1
            }
            .translatable("item.custom.last_chance.name")

    /**
     * Places a plattform
     * @param player The player that placed the plattform
     */
    private fun placePlattform(player: Player, item: ItemStack) {
        val team = player.gma.team ?: return
        val block = team.getBlockVariant(TeamData.BlockVariant.GLASS)
        val blocks = plattformLocation(player.location.subtract(0.0, 3.0, 0.0))

        blocks
            .filter { it.type.isAir || it.isLiquid || !it.isSolid }
            .forEach {
                // Track blocks as "placed by a player"
                @Suppress("UnstableApiUsage")
                BlockPlaceEvent(it, it.state, it, item, player, true, player.handRaised)
                    .callEvent()

                it.type = block
            }
    }

    /**
     * Returns the list of blocks of the plattform
     * @param center The center location of the plattform
     */
    private fun plattformLocation(center: Location) =
        listOf(
            0 to 0,
            0 to 1, 0 to 2, 0 to -1, 0 to -2,
            1 to 0, 2 to 0, -1 to 0, -2 to 0,
            1 to 1, 1 to -1, -1 to 1, -1 to -1
        ).map { (x, z) -> center.clone().add(x.toDouble(), 0.0, z.toDouble()).block }
}