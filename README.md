# 🌈 ChatColor

**ChatColor** is an advanced Minecraft chat color plugin built for **Paper 1.20+** that lets players personalize their chat messages with solid colors, multi-stop gradients, and character-cycling patterns — all through a sleek GUI or simple commands.

---

## ✨ Features

- 🎨 **24+ Solid Colors** — From standard Minecraft colors to custom hex values like Hot Pink, Coral, Mint, and Lavender.
- 🌅 **12 Gradient Presets** — Multi-stop gradients including Sunset, Ocean, Cosmic, Rose Gold, Lava, and more.
- 🔥 **6 Pattern Presets** — Character-cycling color patterns like Rainbow, Fire, Ice, Galaxy, Toxic, and Cherry.
- 🛠️ **Custom Color Creation** — Create your own custom colors with simple commands.
- 🖥️ **Interactive GUI** — Full inventory-based color selector with a main menu and category pages.
- ⌨️ **Command Support** — Set, reset, and manage colors entirely from the command line.
- 🔒 **Per-Entry Permissions** — Grant or restrict individual colors, gradients, and patterns per player/group.
- 💾 **Persistent Data** — Player color selections are saved to disk and restored on rejoin.
- 🔌 **PlaceholderAPI Support** — Expose player color data as placeholders for use in other plugins.
- 🛠️ **Developer API** — Clean `ChatColorAPI` class for third-party plugin integration.
- ⚡ **Hot Reload** — Reload all configuration files at runtime without restarting.

---

## 📦 Installation

1. Download the `ChatColor.jar` file.
2. Place it in your server's `plugins/` folder.
3. Restart your server.
4. Configure `config.yml`, `colors.yml`, `messages.yml`, and `patterns.yml` to your liking.
5. (Optional) Install **PlaceholderAPI** for placeholder support.

**Requirements:**
- Paper (or Spigot) **1.20+**
- Java **17+**
- PlaceholderAPI *(optional)*

---

## 🕹️ Commands

| Command | Description | Permission |
|---|---|---|
| `/color` | Opens the main color selector GUI | `chatcolor.use` |
| `/color gui` | Opens the main color selector GUI | `chatcolor.use` |
| `/color reset` | Removes your active chat color | `chatcolor.use` |
| `/color set <type> <key>` | Sets a color, gradient, or pattern by key | `chatcolor.use` |
| `/color create <name> <tag> <icon>` | Create a new custom color | `chatcolor.create` |
| `/color reload` | Reloads all plugin configuration | `chatcolor.reload` |

**Aliases:** `/chatcolor`, `/cc`

---

## 🔑 Permissions

| Permission | Description | Default |
|---|---|---|
| `chatcolor.use` | Access to the GUI and basic commands | `true` |
| `chatcolor.reload` | Reload the plugin config | `op` |
| `chatcolor.create` | Create custom colors | `op` |
| `chatcolor.color.*` | Access to all solid colors | `op` |
| `chatcolor.gradient.*` | Access to all gradients | `op` |
| `chatcolor.pattern.*` | Access to all patterns | `op` |

Individual entries have their own permission nodes, for example:
- `chatcolor.color.red`
- `chatcolor.gradient.sunset`
- `chatcolor.pattern.rainbow`

---

## ⚙️ Configuration

### `config.yml`
Controls GUI titles and general plugin settings.

```yaml
settings:
  apply-to-message: true   # Apply color to chat messages
  apply-to-name: false     # Apply color to display name
  default-color: "NONE"    # Default color for new players
  event-priority: "LOWEST" # Priority for the chat listener
```

### `colors.yml`
Defines all solid colors and gradients.

```yaml
colors:
  red:
    display-name: "Red"
    tag: "<red>"
    permission: "chatcolor.color.red"
    icon: "RED_WOOL"

gradients:
  sunset:
    display-name: "Sunset"
    tag: "<gradient:#FF4500:#FF8C00:#FFD700>"
    permission: "chatcolor.gradient.sunset"
    icon: "ORANGE_WOOL"
```

### `patterns.yml`
Defines character-cycling color patterns.

```yaml
patterns:
  rainbow:
    display-name: "Rainbow"
    permission: "chatcolor.pattern.rainbow"
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

---

## 🔌 PlaceholderAPI

When PlaceholderAPI is installed, the following placeholders are available:

| Placeholder | Description |
|---|---|
| `%chatcolor_type%` | The player's active color type (`SOLID`, `GRADIENT`, `PATTERN`, or `NONE`) |
| `%chatcolor_key%` | The key of the player's active color selection |
| `%chatcolor_tag%` | The raw MiniMessage tag for the player's active color |

---

## 🛠️ Developer API

Add **ChatColor** as a dependency and use the `ChatColorAPI` to interact with player color data programmatically.

```java
ChatColor plugin = (ChatColor) Bukkit.getPluginManager().getPlugin("ChatColor");
ChatColorAPI api = plugin.getChatColorAPI();

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
plugins/ChatColor/
├── config.yml       # General settings and GUI titles
├── colors.yml       # Solid color and gradient definitions
├── patterns.yml     # Pattern definitions
├── messages.yml     # All plugin messages
└── data/            # Per-player color data (JSON)
```

---

## 👤 Author

Made with ❤️ by **BusyBee**
