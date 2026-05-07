# BedWars
A fast-paced team PvP minigame where players protect their bed while trying to destroy the beds of other teams. Once a bed is destroyed, eliminated players can no longer respawn

This plugin implements the classic BedWars experience, generators, shops, and customizable gameplay settings.

## Installation
> [!NOTE]
> This Plugin is build on top of the [GameManagementAPI](https://gamemanagementapi.github.io/docs/getting-started#what-is-gamemanagementapi)

1. Download the [latest version](https://github.com/Vaycore-Network/BedWars/releases/latest) of this plugin
2. and move it into the `plugins` folder of your server
3. Download and install the latest version of [GameManager](https://github.com/GameManagementAPI/GameManager). _You can read more about the installation [here](https://gamemanagementapi.github.io/docs/first-steps/installation#1-installing-the-gamemanager-plugin)_.
4. Start your server

# Customization
## Providing Maps
Maps are provided in the standard "GMA-way". You can read more about that [here](https://gamemanagementapi.github.io/docs/first-steps/providing-maps).
But in addition to the normal metadata, this plugin requires you to add another section to the `metadata.yml` configuration file:
```yaml
bedwars:
  beds:
    0: [x, y, z]
    1: [x, y, z]
    # ...
  
  shops:
    0: [x, y, z]
    1: [x, y, z]
    # ...
  
  spawners:
    # Provide spawners here
    # Read section below for more details
```

### Configuring Item Spawners
Inside the `bedwars.spawners` section in the `metadata.yml`-file of each map, add the following entry for each of your spawners:
```yaml
spawner_name:
    location: [x, y, z]
    material: material # e.g. IRON_INGOT
    interval: 5        # 5 second interval between two drops
    maximum: 64        # if 64 items are at the location of the spawner, no more items will spawn

    display:
      enabled:  true  # if set to 'false', spawner hologram won't be visible
      name:     "spawner.type.iron.name" # name of spawner (types: gold, brick, iron, diamond, emerald)
      material: IRON_BLOCK # block displayed above spawner
```

## Customizing Team Blocks
When the server loads the plugin for the first time, it will create a `teams.yml` file which can be found at `plugins/BedWars`.
Edit this file to customize the default block colors of different teams:
```yaml
blocks:
  0:
    primary: RED_CONCRETE
    glass: RED_STAINED_GLASS
    wool: RED_WOOL
  
  1:
    # ...
  
  # ...
```

### Customizing the Shop
When the server loads the plugin for the first time, it will create a `shop.json` file which can be found at `plugins/BedWars`.
Inside of this file, you'll be able to customize the different shop pages. Each item is described in this schema:
```json
"slot": { "materialName": "material", "amount": 1, "currencyName": "IRON_INGOT", "cost": 3, "key": "custom_translation_key" }
```

Optional arguments are:
- ```"effect": "type:amplifier:duration"```