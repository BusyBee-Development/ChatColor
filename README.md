# ChatColor
Plugin for Spigot that allows you to create patterns and colors for the chat.

## Features
- **Pattern Modes:** Create unique chat styles with various pattern modes.
- **GUI Support:** Easy to use GUI for players to select their chat color.
- **MySQL Support:** Sync player data across multiple servers.
- **PlaceholderAPI Support:** Use placeholders to show the player's current pattern.
- **ChatControl Support:** Compatible with ChatControl for chat formatting.
- **Update Checker:** Get notified when a new version is available.

## Commands

| Users                    | Permission         | Description                    |
|:-------------------------|:------------------:|:-------------------------------|
| /chatcolor               |                    | Opens the main help or GUI     |
| /chatcolor set [pattern] |   chatcolor.set    | Set your chat pattern          |
| /chatcolor list          |   chatcolor.list   | List available patterns        |
| /chatcolor disable       | chatcolor.disable  | Disable your chat pattern      |
| /chatcolor gui           |   chatcolor.gui    | Open the pattern selection GUI |


| Admin                                  | Permission             | Description                     |
|:---------------------------------------|:-----------------------:|:--------------------------------|
| /chatcoloradmin                        |                         | Opens the admin help            |
| /chatcoloradmin set [player] [pattern] | chatcolor.admin.set     | Set a player's pattern          |
| /chatcoloradmin disable [player]       | chatcolor.admin.disable | Disable a player's pattern      |
| /chatcoloradmin gui [player]           | chatcolor.admin.gui     | Open GUI for a player           |
| /chatcoloradmin reload                 | chatcolor.admin.reload  | Reload the plugin configuration |

## Permissions

| Permission | Description |
|:---|:---|
| chatcolor.updatenotify | Receive notifications about plugin updates on join |
| chatcolor.color.[pattern_name] | Permission to use a specific pattern (if enabled in config) |
| chatcolor.* | Grants all permissions |

## Pattern Modes

| Mode                 | Description                                            |
|:---------------------|:-------------------------------------------------------|
| SINGLE               | Use only the first color                               |
| RANDOM               | Use the colors randomly                                |
| LINEAR               | Use all colors in a linear fashion                     |
| LINEAR_IGNORE_SPACES | Use all the colors in a linear way ignoring the spaces |
| GRADIENT             | Makes a gradient with the colors entered               |

## Placeholders

| Placeholder                        | Information                                                                  |
|:-----------------------------------|:-----------------------------------------------------------------------------|
| %chatcolor_pattern_name%           | Returns the name of the pattern that the equipped player has                 |
| %chatcolor_pattern_name_formatted% | Returns the name of the pattern that the player has equipped but with colors |

## Configuration
The plugin is highly configurable. You can change messages, patterns, and enable/disable features in `config.yml`.

### MySQL
To enable MySQL support, set `mysql.enable` to `true` in `config.yml` and provide your database credentials.

### ChatControl Support
ChatColor automatically detects ChatControl and hooks into it to provide color support. No extra configuration is needed.
