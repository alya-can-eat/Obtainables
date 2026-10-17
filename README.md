# Obtainables
Plugin that makes otherwise unobtainable blocks and items in Minecraft b1.7.3 obtainable. For Poseidon (CB1060 Fork)

---

> [!WARNING]
> This plugin was vibecoded but seems to work good.

---

Some blocks and items exist in Beta 1.7.3 but can never be collected in survival. This plugin adds vanilla-feeling ways to get them, **without modifying the world**. No blocks are placed, no chunks or chests are changed. Everything works through drops, fishing and crafting.

## Features

| Item | How to get it |
|---|---|
| **Apple** | Oak leaves rarely drop an apple when they decay or are broken (not with shears). Default 0.5 % (1/200), the same as Release 1.0. |
| **Sponge** | Rare catch when fishing, replaces the raw fish. Default 1 %. |
| **Chainmail armour** | Zombies and skeletons rarely drop one random chainmail piece. Default 2 %, only when killed by a player (melee or arrow). Zombie pigmen are excluded. |
| **Ice** | Crafted from 9 snow blocks. |
| **Cobweb** | Crafted from 5 string in an X shape. |

These mechanics can be individually tweaked and disabled in the config file.

Since apples are now obtainable, the vanilla golden apple recipe (8 gold blocks + apple) becomes usable too.

## Why these items?

- **Apple**: only drops when a player named "Notch" dies.
- **Sponge**: never generates in Beta 1.7.3.
- **Chainmail armour**: its recipe uses fire as ingredient, which cannot be held in an inventory.
- **Ice**: drops nothing and there is no Silk Touch.
- **Cobweb**: never generates in Beta 1.7.3 and drops string when broken.

## Recipes

Ice (`#` = snow block):

```
# # #
# # #
# # #
```

Cobweb (`s` = string, empty slots stay empty):

```
s   s
  s
s   s
```

## Requirements

- Project Poseidon (Beta 1.7.3 Server - Fork of CB1060)
  - https://github.com/retromcorg/Project-Poseidon
- Java 8

Only tested with Project Poseidon. It may work on CraftBukkit 1060, but that is untested.

## Installation

1. Put `Obtainables*.jar` into the `plugins/` folder.
2. Start the server. `plugins/Obtainables/config.yml` is created on the first start.

## Configuration

`plugins/Obtainables/config.yml`

| Key | Default | Description |
|---|---|---|
| `apple-chance` | `0.5` | Chance in % for an apple from oak leaves. `0` disables it |
| `sponge-fishing-chance` | `1.0` | Chance in % that a fishing catch is a sponge. `0` disables it |
| `chainmail-chance` | `2.0` | Chance in % for a chainmail piece from zombies and skeletons. `0` disables it |
| `chainmail-player-kill-only` | `true` | Only drop chainmail when a player killed the mob |
| `ice-recipe` | `true` | Enable the ice recipe |
| `cobweb-recipe` | `true` | Enable the cobweb recipe |

Recipe changes need a server restart. Bukkit 1060 cannot remove single recipes at runtime.

## Building from source

Compile against the Project Poseidon server jar (Java 8 target):

```bash
javac --release 8 -cp poseidon.jar -d build src/ch/alya/obtainables/*.java
cp plugin.yml build/
jar cf Obtainables.jar -C build .
```
