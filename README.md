# 🌈 LuminaColor

**LuminaColor** is an advanced Minecraft chat color plugin built for **Paper 1.20+** that lets players personalize their chat messages with solid colors, multi-stop gradients, and animated character-cycling patterns — all through a sleek GUI or simple commands.

---

## ✨ Features

- 🎨 **24 Solid Colors** — From standard Minecraft colors to custom hex values like Hot Pink, Coral, Mint, and Lavender
- 🌅 **12 Gradient Presets** — Multi-stop gradients including Sunset, Ocean, Cosmic, Rose Gold, Lava, and more
- 🔥 **6 Pattern Presets** — Character-cycling color patterns like Rainbow, Fire, Ice, Galaxy, Toxic, and Cherry
- 🖥️ **Interactive GUI** — Full inventory-based color selector with a main menu and category pages
- ⌨️ **Command Support** — Set, reset, and manage colors entirely from the command line
- 🔒 **Per-Entry Permissions** — Grant or restrict individual colors, gradients, and patterns per player/group
- 💾 **Persistent Data** — Player color selections are saved to disk and restored on rejoin
- 🔌 **PlaceholderAPI Support** — Expose player color data as placeholders for use in other plugins
- 🛠️ **Developer API** — Clean `LuminaColorAPI` class for third-party plugin integration
- ⚡ **Hot Reload** — Reload all configuration files at runtime without restarting

---

## 📦 Installation

1. Download the `LuminaColor.jar` file
2. Place it in your server's `plugins/` folder
3. Restart your server
4. Configure `config.yml`, `messages.yml`, and `patterns.yml` to your liking
5. (Optional) Install **PlaceholderAPI** for placeholder support

**Requirements:**
- Paper (or Spigot) **1.20+**
- Java **17+**
- PlaceholderAPI *(optional)*

---

## 🕹️ Commands

| Command | Description | Permission |
|---|---|---|
| `/color` | Opens the main color selector GUI | `luminacolor.use` |
| `/color gui` | Opens the main color selector GUI | `luminacolor.use` |
| `/color reset` | Removes your active chat color | `luminacolor.use` |
| `/color set color <key>` | Sets a solid color by key | `luminacolor.use` |
| `/color set gradient <key>` | Sets a gradient by key | `luminacolor.use` |
| `/color set pattern <key>` | Sets a pattern by key | `luminacolor.use` |
| `/color reload` | Reloads all plugin configuration | `luminacolor.reload` |

**Aliases:** `/chatcolor`, `/cc`, `/luminacolor`

---

## 🔑 Permissions

| Permission | Description | Default |
|---|---|---|
| `luminacolor.use` | Access to the GUI and commands | `true` |
| `luminacolor.reload` | Reload the plugin config | `op` |
| `luminacolor.color.*` | Access to all solid colors | `op` |
| `luminacolor.gradient.*` | Access to all gradients | `op` |
| `luminacolor.pattern.*` | Access to all patterns | `op` |

Individual entries have their own permission nodes defined in `config.yml` / `patterns.yml`, for example:
- `luminacolor.color.red`
- `luminacolor.gradient.sunset`
- `luminacolor.pattern.rainbow`

---

## ⚙️ Configuration

### `config.yml`
Controls GUI titles, general settings, and all color/gradient definitions.

```yaml
settings:
  apply-to-message: true   # Apply color to chat messages
  apply-to-name: true      # Apply color to display name
  default-color: "NONE"    # Default color for new players

colors:
  red:
    display-name: "Red"
    tag: "<red>"
    permission: "luminacolor.color.red"
    icon: "RED_WOOL"
```

### `patterns.yml`
Defines character-cycling color patterns. Each message character cycles through the listed color tags.

```yaml
patterns:
  rainbow:
    display-name: "Rainbow"
    permission: "luminacolor.pattern.rainbow"
    icon: "YELLOW_WOOL"
    colors:
      - "<red>"
      - "<gold>"
      - "<yellow>"
      - "<green>"
      - "<aqua>"
      - "<blue>"
      - "<light_purple>"
```

### `messages.yml`
All plugin messages with full MiniMessage support and placeholder tags.

```yaml
prefix: "<dark_gray>[<gradient:blue:aqua>LuminaColor<dark_gray>] "
color-applied: "<prefix><green>Color <reset><color> <gray>has been applied!"
color-reset: "<prefix><gray>Your chat color has been <red>reset<gray>."
```

---

## 🎨 Built-in Colors

| Key | Display Name | Tag |
|---|---|---|
| `red` | Red | `<red>` |
| `dark-red` | Dark Red | `<dark_red>` |
| `gold` | Gold | `<gold>` |
| `yellow` | Yellow | `<yellow>` |
| `green` | Green | `<green>` |
| `dark-green` | Dark Green | `<dark_green>` |
| `aqua` | Aqua | `<aqua>` |
| `dark-aqua` | Dark Aqua | `<dark_aqua>` |
| `blue` | Blue | `<blue>` |
| `dark-blue` | Dark Blue | `<dark_blue>` |
| `light-purple` | Light Purple | `<light_purple>` |
| `dark-purple` | Dark Purple | `<dark_purple>` |
| `white` | White | `<white>` |
| `gray` | Gray | `<gray>` |
| `dark-gray` | Dark Gray | `<dark_gray>` |
| `black` | Black | `<black>` |
| `hot-pink` | Hot Pink | `<#FF69B4>` |
| `orange` | Orange | `<#FF8C00>` |
| `coral` | Coral | `<#FF6B6B>` |
| `lime` | Lime | `<#00FF7F>` |
| `sky` | Sky Blue | `<#87CEEB>` |
| `mint` | Mint | `<#98FF98>` |
| `lavender` | Lavender | `<#E6E6FA>` |
| `teal` | Teal | `<#008080>` |

---

## 🌅 Built-in Gradients

| Key | Display Name |
|---|---|
| `sunset` | Sunset |
| `ocean` | Ocean |
| `forest` | Forest |
| `candy` | Candy |
| `twilight` | Twilight |
| `arctic` | Arctic |
| `volcanic` | Volcanic |
| `cosmic` | Cosmic |
| `neon` | Neon |
| `rose-gold` | Rose Gold |
| `midnight` | Midnight |
| `lava` | Lava |

---

## 🔥 Built-in Patterns

| Key | Display Name | Description |
|---|---|---|
| `rainbow` | Rainbow | Classic 7-color rainbow cycle |
| `fire` | Fire | Orange-to-gold fire cycle |
| `ice` | Ice | Cool blue ice cycle |
| `galaxy` | Galaxy | Deep space purple-blue cycle |
| `toxic` | Toxic | Bright neon green cycle |
| `cherry` | Cherry | Pink cherry blossom cycle |

---

## 🔌 PlaceholderAPI

When PlaceholderAPI is installed, the following placeholders are available:

| Placeholder | Description |
|---|---|
| `%luminacolor_type%` | The player's active color type (`SOLID`, `GRADIENT`, `PATTERN`, or `NONE`) |
| `%luminacolor_key%` | The key of the player's active color selection |
| `%luminacolor_tag%` | The raw MiniMessage tag for the player's active color |

---

## 🛠️ Developer API

Add LuminaColor as a dependency and use the `LuminaColorAPI` to interact with player color data programmatically.

```java
LuminaColor plugin = (LuminaColor) Bukkit.getPluginManager().getPlugin("LuminaColor");
LuminaColorAPI api = plugin.getLuminaColorAPI();

// Set a player's color
api.setColor(player, "red");
api.setGradient(player, "sunset");
api.setPattern(player, "rainbow");

// Reset a player's color
api.resetColor(player);

// Get player data
PlayerColorData data = api.getPlayerData(player.getUniqueId());
String type = data.getColorType(); // "SOLID", "GRADIENT", "PATTERN"
String key  = data.getColorKey();

// Apply the player's color to a string and get a Component
Component colored = api.applyColorToText(player, "Hello, world!");
```

---

## 📁 File Structure

```
plugins/LuminaColor/
├── config.yml       # Colors, gradients, GUI titles, and settings
├── messages.yml     # All plugin messages
├── patterns.yml     # Pattern definitions
└── data/            # Per-player color data (JSON)
```

---

## 👤 Author

Made with ❤️ by **BusyBee**