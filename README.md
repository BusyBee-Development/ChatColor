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
  show-standard-colors: true # Toggle visibility of standard colors
  show-standard-gradients: true # Toggle visibility of standard gradients
  show-standard-patterns: true # Toggle visibility of standard patterns
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

When PlaceholderAPI is installed, the following placeholders are available. All placeholders return results in **Legacy Hex format** (`§x§r§r§g§g§b§b`) for universal compatibility.

| Placeholder                | Aliases                                                | Description                                                 | Example Output           |
|:---------------------------|:-------------------------------------------------------|:------------------------------------------------------------|:-------------------------|
| `%chatcolor_color%`        | `%chatcolor%`, `%chatcolor_prefix%`, `%chatcolor_tag%` | Returns the player's active legacy color code.              | `§c` or `§x§f§f§5§5§f§f` |
| `%chatcolor_name%`         | -                                                      | Returns the player's name with their active color applied.  | `§cBusyBee`              |
| `%chatcolor_message%`      | -                                                      | Applies the player's color to their last sent message.      | `§cHello world!`         |
| `%chatcolor_apply_<text>%` | `%chatcolor_apply:<text>%`                             | Wraps the provided `<text>` with the player's active color. | `§cWelcome!`             |
| `%chatcolor_key%`          | -                                                      | Returns the internal identifier of the selection.           | `rainbow`                |
| `%chatcolor_type%`         | -                                                      | Returns the category (`SOLID`, `GRADIENT`, `PATTERN`).     | `GRADIENT`               |

**Note:** For modern plugins that support MiniMessage (like LPC or Tab), we recommend using these placeholders in their format strings and letting the plugin handle the conversion if necessary.

---

## 🛠️ Integration Tips

### LuckPermsChat (LPC)
To use gradients or patterns with LPC:
1. In LPC `config.yml`, set your format to use `%chatcolor_message%` (e.g., `{message}: %chatcolor_message%`).
2. In ChatColor `config.yml`, set `apply-to-message: false` and `late-bind: true`.

### DiscordSRV
- ChatColor automatically works with DiscordSRV. 
- For the best experience on Paper, ensure `UseModernPaperChatEvent: true` is set in DiscordSRV's config.
