package de.c4vxl.bedwars

import de.c4vxl.bedwars.handler.*
import de.c4vxl.gamemanager.gma.team.Team
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ResourceUtils
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class Main : JavaPlugin() {
    companion object {
        lateinit var instance: Main
        lateinit var logger: Logger
    }

    override fun onLoad() {
        instance = this
        Main.logger = this.logger

        // Load CommandAPI
        CommandAPI.onLoad(
            CommandAPIPaperConfig(this)
                .silentLogs(true)
                .verboseOutput(false)
        )
    }

    override fun onEnable() {
        // Enable CommandAPI
        CommandAPI.onEnable()

        // Register language extensions
        ResourceUtils.readResource("langs", Main::class.java).split("\n")
            .forEach { langName ->
                // Register translations
                Language.provideLanguageExtension(
                    "bedwars",
                    langName,
                    ResourceUtils.readResource("lang/$langName.yml", Main::class.java)
                )

                // Register team labels
                Language.get(langName).child("bedwars").let {
                    Team.registerLabelTranslation(langName, buildMap {
                        for (i in 0..9)
                            put(i, it.get("team.$i.label"))
                    })
                }
            }

        // Save configs
        saveResource("teams.yml", false)
        saveResource("shop.json", false)

        // Register handlers
        GameHandler()
        RespawnHandler()
        MapHandler()
        ItemTranslationHandler()
        SpawnerHandler()
        ScoreboardHandler()
        ShopHandler()
        ItemHandler()
        UpgradesHandler()

        logger.info("[+] $name has been enabled!")
    }

    override fun onDisable() {
        // Disable CommandAPI
        CommandAPI.onDisable()

        logger.info("[+] $name has been disabled!")
    }
}