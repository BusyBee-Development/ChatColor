---
id: placeholderapi
title: PlaceholderAPI
sidebar_position: 1
---

# PlaceholderAPI

ChatColor provides a simplified and powerful PlaceholderAPI expansion. All placeholders are designed to be high-performance and thread-safe. By default they return **Legacy Hex format** (`§x§r§r§g§g§b§b`), and switch to MiniMessage automatically for formatters that need it — see [Output format](#output-format-papi-output).

---

## Setup Requirements

- [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi/versions) must be installed. It's a
  soft-dependency — ChatColor detects it automatically and registers its expansion at startup, no
  configuration needed.
- Placeholders are entirely optional. ChatColor colors chat directly on its own chat pipeline —
  you only need these placeholders when *another* plugin (a scoreboard, tablist, or a
  placeholder-driven chat formatter) needs to read a player's color. See
  [Plugin Integration Guide](#plugin-integration-guide) below and
  [Chat Compatibility](chat-compatibility.md) for the chat-coloring pipeline itself.

---

## Available Placeholders

| Placeholder                | Aliases                                                | Description                                                 | Example Output           |
|:-----------------------------|:-----------------------------------------------------------|:----------------------------------------------------------------|:-----------------------------|
| `%chatcolor_color%`        | `%chatcolor%`, `%chatcolor_prefix%`, `%chatcolor_tag%` | Returns the player's active legacy color code.              | `§c` or `§x§f§f§5§5§f§f` |
| `%chatcolor_name%`         | -                                                      | Returns the player's name with their active color applied.  | `§cBusyBee`              |
| `%chatcolor_message%`      | -                                                      | Applies the player's color to their last sent message.      | `§cHello world!`         |
| `%chatcolor_apply_<text>%` | `%chatcolor_apply:<text>%`                             | Wraps the provided `<text>` with the player's active color. | `§cWelcome!`             |
| `%chatcolor_key%`          | -                                                      | Returns the internal identifier of the selection.           | `rainbow`                |
| `%chatcolor_type%`         | -                                                      | Returns the category (`SOLID`, `GRADIENT`, `PATTERN`).      | `GRADIENT`               |

**Notes on behaviour:**

- A player with no color selected gets their **group default**, then `default-color`, then an
  empty string. Placeholders never return raw MiniMessage tags.
- `%chatcolor_color%` returns a bare color code, so it colors everything that follows it until
  the next code. For a gradient it returns only the **first** stop, because a single code cannot
  express a gradient — use `%chatcolor_apply_<text>%` when you need the gradient across a string.
- `%chatcolor_message%` reflects the player's **most recent** chat message, so it is only
  meaningful inside a chat format.
- Most placeholders resolve for offline players too, falling back to the stored tag.

---

## Output format (`papi-output`)

The example outputs above are the `LEGACY` form (`§` codes). Some formatters parse their whole chat format as MiniMessage, and MiniMessage rejects `§` codes outright — the chat message is dropped and the console shows `Legacy formatting codes have been detected in a MiniMessage string`. `papi-output` in `config.yml` controls which form the placeholders return:

| Value         | Returns                                                                                    |
|:--------------|:-------------------------------------------------------------------------------------------|
| `AUTO`        | `MINIMESSAGE` when LPC 4.x or newer is installed on Paper, `LEGACY` otherwise. Recommended. |
| `LEGACY`      | `§` codes. For scoreboards, tablists, and formatters that use `setFormat()`.                |
| `MINIMESSAGE` | MiniMessage tags. Player-typed text is escaped, so it can't inject tags.                    |

`/color debug` prints the resolved value on its `config:` line.

---

## Plugin Integration Guide

> **Placeholders are optional.** ChatColor colors the message itself, on whichever chat
> pipeline your server is actually using — that includes EssentialsChat and LPC. You only need
> `%chatcolor_message%` if you specifically want a formatter to place the colored message
> inside its own format.

### 1. EssentialsChat — no placeholder needed

EssentialsChat formats chat with legacy `&` codes and does not support PlaceholderAPI in its
`format` string. You don't need one.

*   **Recommended setup:** use the standard `{MESSAGE}` tag in Essentials' `config.yml`.
*   **How it works:** ChatColor applies the color from a chat renderer that runs *after*
    EssentialsChat has finished formatting. The color is never present as a code that Essentials
    could strip.
*   **Example format:**
    ```yaml
    group-formats:
      Default: '{DISPLAYNAME}&7: {MESSAGE}'
    ```
*   **You do not need to grant `essentials.chat.color` or `essentials.chat.rgb`.** Those govern
    whether players can type their own `&` codes and have no bearing on ChatColor.

### 2. LPC (LuckPermsChat) — placeholder optional

LPC installs a Paper chat renderer, and ChatColor wraps it and colors the message before LPC
formats it. The defaults work with no placeholder.

**Recommended — defaults, no placeholder:**

*   Keep `{message}` in LPC's format.
*   In ChatColor's `config.yml`, leave `apply-to-message: true` and `late-bind: false`.

**Alternative — LPC places the color via the placeholder.** Use this only if you want the colored
message inside LPC's own format:

*   In LPC's format, use `%chatcolor_message%` in place of `{message}`:
    ```yaml
    chat-format: "{prefix}{name}&r: %chatcolor_message%"
    ```
*   In ChatColor's `config.yml`, set `apply-to-message: false` and `late-bind: true`. This stops
    ChatColor coloring the message itself so it isn't colored twice.
*   Leave `papi-output: "AUTO"`. LPC 4.x parses its whole format as MiniMessage, and `AUTO` returns
    MiniMessage for it. Without that, LPC throws `Legacy formatting codes have been detected in a
    MiniMessage string` and no chat is sent. Set `papi-output: "MINIMESSAGE"` if LPC isn't detected.

> `late-bind: true` only works when LPC's renderer is the one that ends up delivering the message.
> If another chat plugin installs its own renderer after LPC (a chat-hover or chat-format plugin),
> `%chatcolor_message%` is never used and chat comes out uncolored, with no error. The default
> setup doesn't have this problem.

### 3. DiscordSRV

Works automatically, no placeholder needed. Set `UseModernPaperChatEvent: true` in DiscordSRV's
config so it reads the same event ChatColor renders on.

---

## Technical Details

- **Universal Compatibility:** By default our placeholders return legacy hex strings, so they work in scoreboards, tab-lists, and almost any plugin that supports PAPI, regardless of whether they support MiniMessage. `papi-output` switches them to MiniMessage for formatters that need it.
- **Folia & Paper Ready:** All lookups are performed against a thread-safe cache (`ConcurrentHashMap`), ensuring zero impact on server performance and full compatibility with Folia's regional threading.
- **Gradient Fidelity:** Complex gradients are serialized character-by-character into legacy hex codes, allowing them to render perfectly even in plugins that only understand legacy colors.

---

## Troubleshooting

Placeholder returning literal text like `%chatcolor_color%` instead of a color? Confirm
PlaceholderAPI is installed and run `/papi reload`. For chat-coloring issues (not placeholder
issues), see [Common Issues](../support/common-issues.md).
