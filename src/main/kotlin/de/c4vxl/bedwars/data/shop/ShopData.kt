package de.c4vxl.bedwars.data.shop

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.c4vxl.bedwars.Main
import de.c4vxl.bedwars.ui.ShopUI

object ShopData {
    private val config = Main.instance.dataFolder.resolve("shop.json")

    /**
     * Holds a list of shop pages
     */
    val pages: Map<ShopUI.Page, Map<Int, ShopItem>> by lazy {
        val content: Map<ShopUI.Page, Map<String, ShopItem>> = Gson().fromJson(
            config.readText(),
            object : TypeToken<Map<ShopUI.Page, Map<String, ShopItem>>>() {}.type
        )
        content.mapValues { (_, v) -> v.mapKeys { it.key.toInt() } }
    }
}