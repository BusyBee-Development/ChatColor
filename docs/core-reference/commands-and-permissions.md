---
id: commands-permissions
title: Commands & Permissions
sidebar_position: 1
---

Manage all aspects of **ChatColor** with these commands and their associated permissions.

---

## Commands

The base command for the plugin is `/color`, with aliases: `/chatcolor` and `/colors`.

| Command                                                          | Description                                | Permission           |
|:-----------------------------------------------------------------|:--------------------------------------------|:---------------------|
| `/color`                                                         | Opens the main color selector GUI          | `chatcolor.use`      |
| `/color gui [player]`                                            | Opens the color selector for you or others | `chatcolor.use`*     |
| `/color reset [player]`                                          | Removes active chat color                  | `chatcolor.use`*     |
| `/color set <type> <key|hex> [player]`                               | Sets a color, gradient, or pattern by key  | `chatcolor.use`*     |
| `/color list [type]`                                             | Lists everything you have access to        | `chatcolor.use`      |
| `/color create <name> <tag> <icon> [permission] [default]`       | Create or update a custom color            | `chatcolor.create`   |
| `/color delete <name>`                                           | Delete a custom color (alias: `remove`)    | `chatcolor.create`   |
| `/color reload`                                                  | Reloads all plugin configuration           | `chatcolor.reload`   |
| `/color debug [off\|pipeline\|all]`                              | Trace the chat pipeline in the console     | `chatcolor.debug`    |

*\*Using the `[player]` argument requires the `chatcolor.admin` permission.*

**Valid types for `set`:** `color` (or `solid`), `gradient`, `pattern`.

**Valid types for `list`:** `colors` (default), `gradients`, `patterns`, `custom`.

**Examples:**

```
/color set gradient sunset
/color set solid red BusyBee
/color list patterns
/color create pastel-pink <#FCB6E1> PINK_WOOL
/color create staff-glow <gradient:aqua:white> DIAMOND chatcolor.staff.glow
/color create freebie <yellow> GOLD_INGOT none
/color delete pastel-pink
```

### Creating and deleting custom colors

`<tag>` may be any MiniMessage tag — `<red>`, `<#FCB6E1>`, or a full
`<gradient:#FF4500:#FFD700>`. `<icon>` is any Bukkit material that is a placeable item.

The two optional arguments control access:

| Argument       | Values                                       | Behaviour                                                               |
|:---------------|:---------------------------------------------|:--------------------------------------------------------------------------|
| `[permission]` | *(omitted)*                                  | Node defaults to `chatcolor.custom.<name>`                              |
|                | `none`, `public`, `everyone`, `all`          | Color is public — no permission needed, nothing is registered           |
|                | any other string                             | Used verbatim as the permission node                                    |
| `[default]`    | `true`, `op`, `false` *(default `false`)*    | The permission's Bukkit default, i.e. who has it without being granted  |

Creating a color registers its permission node with the server immediately, so LuckPerms can
tab-complete and grant it without a restart. Re-running `create` with an existing name **updates**
that color in place rather than failing. Names that collide with a standard color or gradient are
rejected.

`/color delete` also **clears that color from every player currently using it** and reports how
many were affected, so nobody is left pointing at an entry that no longer exists.

### Debugging chat (`/color debug`)

When a player has a color permission but their chat still comes out white, this traces the whole
chat pipeline into the server console.

| Sub-command             | Effect                                                                   |
|:-------------------------|:---------------------------------------------------------------------------|
| `/color debug`          | Toggle tracing for yourself                                              |
| `/color debug all`      | Toggle tracing for every player                                          |
| `/color debug pipeline` | Dump the chat listener order once, without tracing messages              |
| `/color debug off`      | Stop tracing everyone                                                    |

Output goes to the **console only**, never to chat. While tracing is off it costs a single
volatile read per message, so leaving the permission granted is harmless — but turn tracing
itself off when you are done, since it logs several lines per chat message.

See [Chat Compatibility](../integrations/chat-compatibility.md) for how to read the output.

---

## Console Usage

When running commands from the server console, you must specify a target player for any command that affects a player's color or opens a GUI.

| Command                            | Console Support | Notes                                             |
|:-------------------------------------|:------------------|:-----------------------------------------------------|
| `/color`                           | ❌ No           | Use `/color set` instead.                         |
| `/color gui <player>`              | ✅ Yes          | Opens the GUI for the specified player.           |
| `/color reset <player>`            | ✅ Yes          | Resets color for the specified player.            |
| `/color set <type> <key|hex> <player>` | ✅ Yes          | Sets color for the specified player.              |
| `/color list [type]`               | ✅ Yes          | Lists every entry, permissions are not filtered.  |
| `/color create ...`                | ✅ Yes          | Works exactly as it does in-game.                 |
| `/color delete <name>`             | ✅ Yes          | Works exactly as it does in-game.                 |
| `/color reload`                    | ✅ Yes          | Works exactly as it does in-game.                 |
| `/color debug all\|pipeline`       | ⚠️ Partial      | Console cannot chat, so it cannot watch itself.   |

---

## Permissions

### General Permissions

| Permission              | Description                                          | Default  |
|:--------------------------|:----------------------------------------------------------|:-----------|
| `chatcolor.use`         | Access to the GUI and basic commands                 | `true`   |
| `chatcolor.reload`      | Reload the plugin configuration files                | `op`     |
| `chatcolor.create`      | Allows creating and deleting custom colors           | `op`     |
| `chatcolor.admin`       | Use admin arguments (like `[player]`)                | `op`     |
| `chatcolor.set.hex`     | Choose any hex color via `/color set color <hex>`    | `false`  |
| `chatcolor.debug`       | Run `/color debug` to trace the chat pipeline        | `op`     |
| `chatcolor.minimessage` | Use MiniMessage tags & legacy colors in chat         | `false`  |

> `chatcolor.minimessage` lets a player write their **own** formatting (`<red>`, `&c`,
> `<rainbow>`) inside a message. It is unrelated to the color they pick from the GUI, and it is
> `false` by default because it lets players use `<obfuscated>` and imitate staff colors.

### Group Default Permissions

Assign default colors to specific groups using these permission nodes. The colors and groups are defined in `config.yml`.

| Permission               | Description                                | Default |
|:---------------------------|:----------------------------------------------|:----------|
| `chatcolor.group.<name>` | Apply the default color defined for <name> | `false` |

### Wildcard Permissions

| Permission             | Description                                     | Default     |
|:-------------------------|:----------------------------------------------------|:--------------|
| `chatcolor.color.*`    | Access to all standard solid colors             | `op`        |
| `chatcolor.gradient.*` | Access to all standard gradients                | `op`        |
| `chatcolor.pattern.*`  | Access to all patterns                          | `op`        |
| `chatcolor.custom.*`   | Access to all custom colors from `colors.yml`   | `op`        |

### GUI Access Permissions

Control access to specific sections of the main menu GUI.

| Permission               | Description                        | Default |
|:---------------------------|:---------------------------------------|:----------|
| `chatcolor.gui.solid`    | Access to the Solid Colors section | `true`  |
| `chatcolor.gui.gradient` | Access to the Gradients section    | `true`  |
| `chatcolor.gui.pattern`  | Access to the Patterns section     | `true`  |

### Entry-Specific Permissions

Each individual color, gradient, and pattern has its own permission node for granular control. These are defined in `colors/colors.yml` and `colors/patterns.yml`.

**Examples:**
- **Solid Color:** `chatcolor.color.red`
- **Gradient:** `chatcolor.gradient.sunset`
- **Pattern:** `chatcolor.pattern.rainbow`
- **Custom Color:** `chatcolor.custom.pastel-pink`

#### Runtime permission registration

Every entry defined in `colors/colors.yml` and `colors/patterns.yml` is registered with Bukkit
as a **real permission** on startup and re-registered on `/color reload`, then attached as a child
of its matching wildcard.

Two consequences worth knowing:

1. Permission-manager plugins such as LuckPerms can tab-complete your custom nodes, and granting
   `chatcolor.custom.*` genuinely grants every custom color — wildcards in LuckPerms only expand
   over nodes the server knows about.
2. An entry with no `permission:` key in YAML is **public**: everyone can use it, and nothing is
   registered for it. Set a `permission:` if you want it gated.
