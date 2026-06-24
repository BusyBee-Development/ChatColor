# 🌈 ChatColor

**ChatColor** is an advanced Minecraft chat color plugin built for **Paper 1.21+** that lets players personalize their chat messages with solid colors, multi-stop gradients, and character-cycling patterns — all through a sleek GUI or simple commands.

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
- 📊 **bStats** — Anonymized usage statistics.
- 📊 **FastStats** — Anonymized usage statistics.

---

## 📦 Installation

1. Download the `ChatColor.jar` file.
2. Place it in your server's `plugins/` folder.
3. Restart your server.
4. Configure files in the `plugins/ChatColor/` folder.
5. (Optional) Install **PlaceholderAPI** for placeholder support.

**Requirements:**
- Paper (or Spigot) **1.21+**
- Java **21+**
- PlaceholderAPI *(optional)*

---

## 🕹️ Commands

| Command                                            | Description                                 | Permission          |
|----------------------------------------------------|---------------------------------------------|---------------------|
| `/color`                                           | Opens the main color selector GUI           | `chatcolor.use`     |
| `/color gui [player]`                              | Opens the color selector for you or others  | `chatcolor.use`*    |
| `/color reset [player]`                            | Removes active chat color                   | `chatcolor.use`*    |
| `/color set <type> <key> [player]`                 | Sets a color, gradient, or pattern by key   | `chatcolor.use`*    |
| `/color create <name> <tag> <icon> [permission]`   | Create a new custom color                   | `chatcolor.create`  |
| `/color reload`                                    | Reloads all plugin configuration            | `chatcolor.reload`  |

*\*Using `[player]` argument (or running from console) requires `chatcolor.admin`.*

**Aliases:** `/chatcolor`, `/cc`

**Note for Console:** Console usage requires specifying a target player for `gui`, `reset`, and `set`.

---

## 🔑 Permissions

| Permission               | Description                            | Default |
|--------------------------|----------------------------------------|---------|
| `chatcolor.use`          | Access to the GUI and basic commands   | `true`  |
| `chatcolor.reload`       | Reload the plugin config               | `op`    |
| `chatcolor.create`       | Create custom colors                   | `op`    |
| `chatcolor.admin`        | Use admin command arguments & console  | `op`    |
| `chatcolor.minimessage`  | Use MiniMessage & legacy codes in chat | `false` |
| `chatcolor.gui.solid`    | Access to Solid Colors section         | `true`  |
| `chatcolor.gui.gradient` | Access to Gradients section            | `true`  |
| `chatcolor.gui.pattern`  | Access to Patterns section             | `true`  |
| `chatcolor.color.*`      | Access to all solid colors             | `op`    |
| `chatcolor.gradient.*`   | Access to all gradients                | `op`    |
| `chatcolor.pattern.*`    | Access to all patterns                 | `op`    |
| `chatcolor.group.<name>` | Apply group-based default colors       | `false` |

Individual entries have their own permission nodes, for example:
- `chatcolor.color.red`
- `chatcolor.gradient.sunset`
- `chatcolor.pattern.rainbow`

---

## ⚙️ Configuration

### `config.yml`
Controls general plugin settings and behavior.

```yaml
settings:
  apply-to-message: true   # Apply color to chat messages
  apply-to-name: false     # Apply color to display name
  default-color: "NONE"    # Default color for new players
  group-defaults:          # Group-based prioritized defaults
    admin: "<gradient:red:gold>"
    vip: "<aqua>"
  event-priority: "DEFAULT" # Options: HIGHEST, LOWEST, etc. or DEFAULT (auto-detect)
  late-bind: false         # Use for compatibility issues
  clean-console: true      # Strip colors from console logs
```

### `gui/gui.yml`
Full control over GUI titles, items, layouts, and messages.

### `colors/colors.yml`
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

### `colors/patterns.yml`
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

### Universal Placeholders
These placeholders automatically detect whether the player has a **Solid Color**, **Gradient**, or **Pattern** selected and apply it.

| Placeholder              | Description                                                           | Example Output      |
|:-------------------------|:----------------------------------------------------------------------|:--------------------|
| `%chatcolor_message%`    | Colors the player's last chat message (Legacy)                        | `§cHello world!`    |
| `%chatcolor_mm_message%` | Colors the player's last chat message (**MiniMessage - Use for LPC**) | `<red>Hello world!` |
| `%chatcolor_<text>%`     | Colors arbitrary `<text>` with player's color (Legacy)                | `§6§lBusyBee`       |
| `%chatcolor_mm_<text>%`  | Colors arbitrary `<text>` with player's color (MiniMessage)           | `<red>BusyBee`      |

**Note:** The `mm_` prefix and `_mm` suffix are interchangeable (e.g., `%chatcolor_message_mm%`).

**Pro Tip:** Use the `mm_` variant (e.g., `%chatcolor_mm_message%`) for modern plugins like **LPC** or **Tab**.

### Status Placeholders
| Placeholder                | Description                         | Example Output                           |
|:---------------------------|:------------------------------------|:-----------------------------------------|
| `%chatcolor_color_key%`    | Key of player's active selection    | `red`, `sunset`, `rainbow`, `none`       |
| `%chatcolor_color_type%`   | Type of selection                   | `SOLID`, `GRADIENT`, `PATTERN`, `NONE`   |
| `%chatcolor_color%`        | Raw MiniMessage tag / Pattern key   | `<red>`, `rainbow`                       |
| `%chatcolor_color_legacy%` | Legacy color code for current color | `§c`                                     |

### Color Overrides
Force a specific color defined in `colors.yml` or `patterns.yml` on any text.

| Placeholder                          | Example                                               |
|:-------------------------------------|:------------------------------------------------------|
| `%chatcolor_<colorName>_<text>%`     | `%chatcolor_red_Warning!%` -> `§cWarning!`            |
| `%chatcolor_mm_<colorName>_<text>%`  | `%chatcolor_mm_sunset_Hello%` -> `<gradient...>Hello` |

---

## 🛠️ Integration Tips

### LuckPermsChat (LPC)
To use gradients or patterns with LPC:
1. In LPC `config.yml`, set your format to use `%chatcolor_message_mm%` (e.g., `{message}: %chatcolor_message_mm%`).
2. In ChatColor `config.yml`, set `apply-to-message: false` and `late-bind: true`.

### DiscordSRV
- ChatColor automatically works with DiscordSRV. 
- For the best experience on Paper, ensure `UseModernPaperChatEvent: true` is set in DiscordSRV's config.

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
