package de.c4vxl.bedwars.data.shop

import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.data.TeamData
import de.c4vxl.bedwars.data.TeamData.getBlockVariant
import de.c4vxl.bedwars.handler.ItemTranslationHandler.Companion.translatable
import de.c4vxl.bedwars.item.Booster
import de.c4vxl.gamemanager.gma.player.GMAPlayer
import de.c4vxl.gamemanager.gma.team.Team
import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

data class ShopItem(
    val materialName: String,
    val amount: Int,
    val currencyName: String,
    val cost: Int,
    val key: String? = null,
    val effect: String? = null
) {
    /**
     * Returns a material from its name and resolves team-based variants automatically
     * @param name The name of the material
     * @param team The team to use for team based variants
     */
    private fun getMaterial(name: String, team: Team? = null): Material {
        val uppercase = name.uppercase()

        // Handle variants
        if (uppercase.startsWith("VARIANT:")) {
            val variant = TeamData.BlockVariant.valueOf(uppercase.removePrefix("VARIANT:"))
            return team!!.getBlockVariant(variant)
        }

        if (uppercase.startsWith("POTION:"))
            return Material.POTION

        return Material.getMaterial(name) ?: run {
            Main.logger.warning("Invalid material name: $name")
            error("Invalid material name: $name")
        }
    }

    /**
     * Returns the currency material required to pay for that item
     */
    val currency: Material get() = getMaterial(currencyName)

    /**
     * Returns the potion effect of this item (if present)
     */
    val potionEffect: PotionEffect? get() =
        effect?.let {
            val (effect, amplifier, duration) = it.split(":")
            PotionEffect(PotionEffectType.values().find { it.name == effect } ?: return null,
                (duration.toIntOrNull() ?: return null) * 20, amplifier.toIntOrNull() ?: return null,
                true, true)
        }

    /**
     * Creates an item builder instance for this item
     * @param player The player to receive the item
     */
    fun builder(player: GMAPlayer): ItemBuilder {
        // Handle custom items
        if (materialName.startsWith("custom:")) {
            val lang = player.language.child("bedwars")
            return when (materialName.lowercase().removePrefix("custom:")) {
                "booster" -> Booster.item(lang)
                else -> error("Invalid custom item '${materialName}'")
            }
        }

        val material = getMaterial(materialName, player.team)
        return ItemBuilder(material, unbreakable = true)
            .let { key?.let { k -> it.translatable(k) } ?: it }
            .editMeta { meta ->
                // Add potion effect
                potionEffect?.let { (meta as? PotionMeta)?.addCustomEffect(it, true) }

                // Set display name
                meta.displayName(key?.let { player.language.child("bedwars").getCmp(it) }
                    ?: Component.translatable(material.translationKey()).color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD))
            }
    }
}