---
id: configuration
title: Configuration
sidebar_position: 1
---

**ChatColor** uses multiple YAML files to keep settings, colors, patterns, and messages organized and easy to customize.

---

## `config.yml`

This file controls general settings and core behavior of the plugin.

```yaml
settings:
  apply-to-message: true   # Whether to apply colors to chat messages
  apply-to-name: false     # Whether to apply colors to the player's display name
  default-color: "NONE"    # The default color for new players (Key from colors.yml/patterns.yml)
  group-defaults:          # Group-based default colors
    admin: "<gradient:red:gold>"
    vip: "<aqua>"
  event-priority: "DEFAULT" # The listener priority for the chat event (LOWEST to MONITOR)
  chat-hook: "AUTO"        # Which chat event to hook (AUTO / MODERN / LEGACY)
  late-bind: false         # Use if you use %chatcolor_message% in other plugin formats
  clean-console: true      # Strips color codes from console output
  show-standard-colors: true     # Toggle standard colors in GUI
  show-standard-gradients: true  # Toggle standard gradients in GUI
  show-standard-patterns: true   # Toggle standard patterns in GUI
```

### Settings Detailed

| Setting            | Description                                                                                                                                                                    |
|:----------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `apply-to-message` | Whether to apply the player's selected color to their chat messages.                                                                                                           |
| `apply-to-name`    | Whether to apply the color to the player's display name in chat.                                                                                                               |
| `default-color`    | A MiniMessage tag applied to players who haven't picked one, e.g. `"<gray>"`. Set to `NONE` to disable. Overridden by `group-defaults` when the player matches one.             |
| `group-defaults`   | Map of `permission-node-suffix: color-tag`. Players with `chatcolor.group.<suffix>` get that color. **Evaluated top to bottom — the first match wins**, so list staff first.    |
| `event-priority`   | Set to `DEFAULT` to auto-detect, or set one manually (e.g. `HIGHEST`). See [below](#listener-priority-event-priority) — you should rarely need to change this.                  |
| `chat-hook`        | Which chat event to hook. `AUTO` (recommended), `MODERN`, or `LEGACY`. Paper only — Spigot always uses `LEGACY`. See below.                                                     |
| `message-mode`     | How the color is applied to the message. `AUTO` (recommended) picks for you, `RENDERER` colors it as chat is rendered, `DIRECT` colors the event message itself — use `DIRECT` when a formatter (e.g. EssentialsChat) ignores what the renderer hands it. Invalid values fall back to `AUTO` with a warning. |
| `late-bind`        | Stops ChatColor coloring the message itself, so another plugin can place it via `%chatcolor_message%`. Enable this **only** when a chat plugin builds its format from placeholders (LPC), otherwise chat comes out uncolored. |
| `clean-console`    | If true, the plugin strips color codes from its own console output to keep logs readable. `/color debug` output is escaped so this cannot hide it.                              |
| `show-standard-*`  | Set to `false` to skip loading the bundled colors, gradients, or patterns entirely — they disappear from the GUI **and** from `/color set` and the API. Custom colors are unaffected. |

---

## Chat Hook (`chat-hook`)

ChatColor registers **exactly one** chat listener. This matters more than it sounds.

Paper has two chat pipelines: the modern one built on `AsyncChatEvent`, and a legacy one built on
the deprecated `AsyncPlayerChatEvent`. A plugin that registers on both would color the same
message twice on the way through, so ChatColor picks one.

| Value    | Behaviour                                                                                                                                                    |
|:-----------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `AUTO`   | **Recommended.** `MODERN` on Paper (and Folia), `LEGACY` on Spigot.                                                                                          |
| `MODERN` | Always use `AsyncChatEvent`. ChatColor colors from a **chat renderer**, which runs after every listener has finished, so nothing downstream can strip it.     |
| `LEGACY` | Always use `AsyncPlayerChatEvent`. Only needed if a plugin cancels chat on the legacy event and you need ChatColor to run before it. See the warning below.   |

The active choice is logged on startup:

```
[ChatColor] Chat hook: MODERN (AsyncChatEvent), priority HIGHEST
```

`/color reload` re-runs this resolution and rebinds the listener, so you do not need a full
restart after changing `chat-hook` or `event-priority`.

### Why `MODERN` is safe even when other chat plugins are installed

On Paper, `AsyncChatEvent` fires for **every** chat message regardless of what else is installed:

- With no legacy listeners, Paper fires it directly.
- With legacy listeners present, Paper fires `AsyncPlayerChatEvent` first and then feeds the
  result straight into `AsyncChatEvent`.

So the modern event is always **last** in the chain, which is exactly where a colorizer wants to
be. On top of that, `MODERN` does not write color codes into the message at all — it installs a
renderer that applies the color after every listener has had its say. A plugin that strips color
codes out of chat therefore has nothing of ours left to strip.

> **Earlier versions got this wrong.** `AUTO` used to join whichever event already had listeners
> on it. That sounds like "go where the chat plugins are", but `AsyncPlayerChatEvent` is also
> where every plugin that merely *captures* chat input lives — WorldGuard, mcMMO, HeadDatabase,
> GUI prompts. On a populated server one of those is always present, so `AUTO` picked `LEGACY`
> every time and left ChatColor running *before* a modern formatter such as EssentialsChat, which
> then stripped the codes. If you previously worked around this by forcing `chat-hook: MODERN`,
> you can safely set it back to `AUTO`.

### When you would still want `LEGACY`

Only if a plugin **cancels or consumes** chat on `AsyncPlayerChatEvent` and you need ChatColor to
act before that happens. Be aware of two costs:

1. Colors written on the legacy event can still be stripped by anything reading the modern event
   afterwards — EssentialsChat included. This is the exact failure `MODERN` was introduced to fix.
2. Paper renders legacy-pipeline messages through a shared legacy serializer. On most builds that
   serializer has no hex support, so `#FF7F00` arrives at the client as the nearest of the 16
   named colors (gold). Solid colors are unaffected; only gradients and custom hex are
   approximated. ChatColor detects this at startup and warns you.

---

## Listener Priority (`event-priority`)

`DEFAULT` auto-detects, and what it picks depends on which hook is active:

| Hook     | `DEFAULT` resolves to                                                                     |
|:-----------|:----------------------------------------------------------------------------------------------|
| `MODERN` | Always `HIGHEST`.                                                                         |
| `LEGACY` | `HIGHEST` when EssentialsChat, LPC, or DeluxeChat is running, otherwise `NORMAL`.         |

The two rules differ because the hooks work differently. On `MODERN`, ChatColor installs a chat
renderer and **the last plugin to install one wins**, so being late is correct no matter what else
is installed — there is nothing to detect. On `LEGACY`, ChatColor writes color codes into the
message, so it genuinely matters whether a known formatter is going to run before it.

> Earlier versions used the plugin-name check on both hooks. That only ever worked for the three
> plugins named above and missed everything else — including every chat plugin on Folia, since
> none of those three run there.

ChatColor never takes `MONITOR` automatically. That priority is reserved for plugins that only
observe an event, and leaving it free means you can still configure something to run after
ChatColor if you need to.

**When to override.** Only when another plugin is replacing ChatColor's renderer instead of
wrapping it. Run `/color debug`, say something, and look at `ourRendererStillInstalled` — if it
reports `false`, set `event-priority: "MONITOR"` to get the last word.

---

## `gui/gui.yml`

This file allows for deep customization of the GUI, including all titles, items, layouts, and status messages.

See the [GUI](../core-reference/gui.md) page for more details.

| Section    | Description                                             |
|:-------------|:-------------------------------------------------------------|
| `titles`   | MiniMessage formatted titles for each menu page.        |
| `layouts`  | Define inventory size and slot positions for items.     |
| `status`   | Text shown in item lore based on player access/state.   |
| `items`    | Material, name, and lore for navigational/button items. |

---

## `colors/colors.yml`

This file defines all your solid colors and gradients.

### Solid Colors
Solid colors use a single MiniMessage tag or a hex code.

```yaml
colors:
  red:
    display-name: "Red"
    tag: "<red>"
    permission: "chatcolor.color.red"
    icon: "RED_WOOL"
  hot-pink:
    display-name: "Hot Pink"
    tag: "<#FF69B4>"
    permission: "chatcolor.color.hot_pink"
    icon: "PINK_WOOL"
```

### Gradients
Gradients use the `<gradient:color1:color2:...>` tag.

```yaml
gradients:
  sunset:
    display-name: "Sunset"
    tag: "<gradient:#FF4500:#FF8C00:#FFD700>"
    permission: "chatcolor.gradient.sunset"
    icon: "ORANGE_WOOL"
```

### Custom Colors

The `custom-colors` section holds anything created with `/color create`. It starts empty (`{}`)
and you can also hand-edit it. Entries here appear in the GUI alongside the standard colors and
are listed by `/color list custom`.

```yaml
custom-colors:
  pastel-pink:
    display-name: "Pastel Pink"
    tag: "<#FCB6E1>"
    permission: "chatcolor.custom.pastel-pink"
    icon: "PINK_WOOL"
    default: "false"
```

| Key            | Required | Notes                                                                                       |
|:-----------------|:-----------|:-----------------------------------------------------------------------------------------------|
| `display-name` | No       | Shown in the GUI. Falls back to the key.                                                    |
| `tag`          | Yes      | Any MiniMessage tag, including gradients.                                                   |
| `permission`   | No       | **Omit or leave blank to make the color public.** You may also write `none`/`public`/`everyone`/`all`. |
| `icon`         | No       | Any placeable Bukkit material. Defaults to `WHITE_WOOL`.                                    |
| `default`      | No       | Bukkit permission default: `true`, `op`, or `false`. Defaults to `false`.                   |

The same `permission` / `default` rules apply to the `colors`, `gradients`, and `patterns`
sections.

### What happens when a permission is revoked

Color selections are resolved fresh on every message rather than trusting the tag stored when the
player picked it. So if you revoke someone's access to a color, or delete the entry from YAML,
they immediately fall back to their group default (or to no color) without needing to re-pick.

---

## `colors/patterns.yml`

Patterns cycle colors character-by-character throughout the message.

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

## `lang/messages.yml`

All plugin messages can be fully customized with MiniMessage formatting. Use `<prefix>` to include the defined prefix.

```yaml
prefix: "<dark_gray>[<gradient:blue:aqua>ChatColor<dark_gray>] "
color-applied: "<prefix><green>Color <reset><color> <gray>has been applied!"
color-created: "<prefix><green>Custom color '<gray><name><green>' has been created!"
color-reset: "<prefix><gray>Your chat color has been <red>reset<gray>."
no-permission: "<prefix><red>You don't have permission to use that color."
config-reloaded: "<prefix><green>Configuration reloaded successfully."
```

---

## Automatic Configuration Updates

**You should never have to delete a config file to update ChatColor.** Dropping in a new jar
and restarting is enough: new settings, new colours and rewritten documentation arrive on their
own, and everything you typed stays where you put it.

### What is kept, and what is refreshed

| | Behaviour on update |
|:----|:-----------------------|
| Values you changed | **Kept.** Always, even when the shipped default changes. |
| Entries you added yourself | **Kept.** Your own colours under `colors:`, your own GUI items, anything the bundled file has never heard of. |
| `custom-colors` | **Kept.** This is the section ChatColor writes on your behalf from `/color`. |
| Keys you deleted | **Stay deleted** — see below. |
| New settings and colours | **Added**, with their documentation. |
| New fields on entries you already have | **Added** (e.g. the per-entry `default:` field). |
| Reworded comments | **Refreshed** from the jar, so the guidance in your file matches the version you are running. |
| Settings ChatColor no longer uses | **Removed if you never touched them**, kept and reported if you did. |
| Key order and section layout | **Reset** to the bundled order, so your file reads like the documentation. |

A backup is written to `plugins/ChatColor/backups/` before any change, and the ten most recent
per file are kept. The write itself goes through a temporary file, so an interrupted restart
cannot leave a half-written config behind.

Nothing is written at all when there is nothing to change — a restart that changes no settings
leaves the file byte-for-byte identical and creates no backup.

### Deleted keys, and the one exception

If you delete a standard colour you do not want, it should not silently reappear on the next
update. To manage that, ChatColor records what it has already shipped you in
`plugins/ChatColor/data/.config-state.yml`. A key missing from your file is then either
something new, which gets added, or something you removed, which does not.

**The exception is the first update after this feature shipped.** There is no record yet on
that run, so ChatColor cannot tell the two apart and back-fills everything missing — including
any standard colour you had deleted. Delete it once more and it will stay gone from then on.

To hide all the standard colours at once instead of deleting them one by one, use the toggles in
`config.yml`:

```yaml
settings:
  show-standard-colors: false
  show-standard-gradients: false
  show-standard-patterns: false
```

The ledger is bookkeeping, not configuration. Deleting it is safe: the install simply looks new
again and back-fills once.

### Reading the startup log

A migration that changed something prints a summary:

```
[ChatColor] === colors.yml Migration Summary ===
[ChatColor] Your existing settings were kept. Previous copy: backups/
[ChatColor] Added 3 new key(s):
[ChatColor]   + colors.red.default
[ChatColor]   + colors.pink.tag
[ChatColor] Removed 1 obsolete key(s):
[ChatColor]   - settings.legacy-mode
[ChatColor] === Migration Complete ===
```

| Marker | Meaning |
|:---|:-----|
| `+` | New key delivered from the jar |
| `-` | Key ChatColor no longer uses, and which still held the value we shipped |
| `~` | A renamed setting; your value was moved to the new name |
| `!` | Needs your attention — see below |

Lines marked `!` are the only ones worth acting on:

- *"…is no longer used, but you have customised it - left in place"* — a setting that has been
  retired. Your value is untouched but nothing reads it any more; delete the line when convenient.
- *"…changed from a value to a section - your old value is in the backup"* — a setting that grew
  into a group of settings. The new structure is in place; recover your old value from
  `backups/` if you need it.

### Forcing a clean regenerate

If you genuinely want the bundled file back:

1. Stop the server.
2. Delete the file (for example `colors/colors.yml`) **and** its entry will be re-seeded
   automatically — no need to touch `data/.config-state.yml`.
3. Start the server.

Copy anything you want to keep out first. If you only realise afterwards, your previous file is
still in `backups/`.

### For a corrupted file

Invalid YAML — usually a stray tab, or an unclosed quote — cannot be merged. ChatColor moves the
file to `backups/<name>.corrupted-<timestamp>` and writes a fresh copy from the jar, so the
server still starts. Your broken file is intact in `backups/` if you want to recover values from
it by hand.

---

## Troubleshooting

If colors aren't appearing in chat, see [Common Issues](../support/common-issues.md) for a
step-by-step checklist, or run `/color debug` and check
[Chat Compatibility](../integrations/chat-compatibility.md) for how to interpret the output.
