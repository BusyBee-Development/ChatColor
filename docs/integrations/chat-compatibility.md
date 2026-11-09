---
id: chat-compatibility
title: Chat Compatibility
sidebar_position: 2
---

Almost every "my colors don't show up" report comes down to the same question: **where in the
chat pipeline does ChatColor sit relative to the plugin that formats your chat?** This page
explains the pipeline and how to inspect it on a live server.

---

## How ChatColor applies color

On Paper, ChatColor listens to `AsyncChatEvent` and installs a **chat renderer**. It does not
write color codes into the message.

That distinction is the whole design. A renderer runs *after every listener has finished*, once
per viewer, at the moment the message is turned into what the client sees. A plugin that strips
color codes out of chat runs during the event, before the renderer — so by the time ChatColor
applies the color, nothing is left that could strip it.

The alternative — writing `§x§f§c§b§6§e§1` into the message during the event — leaves the color
sitting in a string that every later listener is free to rewrite. That is the `LEGACY` path, and
it is why it is no longer the default.

### When the renderer isn't enough: direct mode

The renderer design assumes the plugin below ChatColor actually *renders* the component it is
handed. Not all of them do. EssentialsChat's Paper renderer takes its own copy of the message
inside its `HIGHEST` handler and substitutes that copy for `%2$s`, never reading the component
the renderer chain passes it. Wrapping a renderer like that colors nothing — ChatColor's output
goes in as an argument and is discarded.

For those plugins ChatColor switches to **direct mode**: it colors `event.message()` in place
from *in front of* the formatter, and the formatter then formats an already-colored message.
`message-mode: "AUTO"` picks the right one for you, and `event-priority: "DEFAULT"` drops to
`HIGH` so ChatColor gets there first.

Direct mode is only used where it has to be, because it gives up the renderer's main advantage:
a color written into the message can be stripped by anything that runs later. In EssentialsChat's
case that is safe — its own color filter runs at `LOWEST`, long before ChatColor.

---

## Reading `/color debug`

Run `/color debug` (or `/color debug all`), say something in chat, and check the **console**.
Turn it off with `/color debug off` when you're done.

### The pipeline dump

```
[ChatColor] [DEBUG] ===== chat pipeline =====
[ChatColor] [DEBUG] paper=true hook=MODERN (AsyncChatEvent) priority=HIGHEST mode=RENDERER
[ChatColor] [DEBUG] config: apply-to-message=true apply-to-name=false late-bind=false chat-hook=AUTO event-priority=DEFAULT message-mode=AUTO
[ChatColor] [DEBUG] AsyncChatEvent: 8 listener(s), in execution order
[ChatColor] [DEBUG]   LOWEST   EssentialsChat  (com.earth2me.essentials.chat.processing.PaperChatHandler$ChatListener)
[ChatColor] [DEBUG]   NORMAL   EssentialsChat  (com.earth2me.essentials.chat.processing.PaperChatHandler$ChatListener)
[ChatColor] [DEBUG]   HIGHEST  AnoModules  (dev.mrtroxy.anomodules.modules.chathover.ChatHoverListener)
[ChatColor] [DEBUG]   HIGHEST  ChatColor  (net.busybee.chatcolor.listeners.ChatListener)
[ChatColor] [DEBUG]   MONITOR  ChatColor  (net.busybee.chatcolor.utils.ChatDebugger)
[ChatColor] [DEBUG] =========================
```

What to look for:

- **`hook=`** should be `MODERN (AsyncChatEvent)` on Paper. `LEGACY` on Paper means someone set
  `chat-hook` explicitly.
- **Your chat formatter should appear on the same event as ChatColor.** If EssentialsChat is
  listed under `AsyncChatEvent` and ChatColor under `AsyncPlayerChatEvent`, they are on different
  pipelines and ChatColor's output will be stripped downstream.
- **`mode=` tells you which strategy is in use.** `RENDERER` is the default. `DIRECT` means a
  formatter that ignores renderers was detected — currently that means EssentialsChat.
- **`priority=` should read `HIGHEST` in `RENDERER` mode and `HIGH` in `DIRECT` mode.** `DEFAULT`
  resolves to the right one automatically; anything else means `event-priority` was set by hand.
- **In `RENDERER` mode, ChatColor should be at or near the bottom of its priority group.** It
  binds one tick after startup specifically so it registers *after* other plugins at the same
  priority, which lets it wrap their renderer instead of being overwritten by it. In the dump
  above, ChatColor is listed below `AnoModules` at the same `HIGHEST` priority — that is the
  ordering working correctly. In `DIRECT` mode the opposite is true: ChatColor must appear
  **above** the formatter, which is what dropping to `HIGH` achieves.

### The per-message trace

```
[ChatColor] [DEBUG] --- djtmk [MODERN] "hello"
[ChatColor] [DEBUG]   stored:   type=SOLID key=pastel-pink tag={#fcb6e1}
[ChatColor] [DEBUG]   resolved: pattern=none tag={#fcb6e1}
[ChatColor] [DEBUG]   entry:    "pastel-pink" needs chatcolor.custom.pastel-pink -> hasPermission=true isPermissionSet=true
[ChatColor] [DEBUG]   essentials.chat.color = false (unset, using default)
[ChatColor] [DEBUG]   renderer in place before us: dev.mrtroxy.anomodules...ChatHoverListener$$Lambda
[ChatColor] [DEBUG]   MONITOR:  cancelled=false viewers=2 ourRendererStillInstalled=true
[ChatColor] [DEBUG]   RENDERER RAN
[ChatColor] [DEBUG]     in : hello
[ChatColor] [DEBUG]     out: {#FCB6E1}hello
```

| Line                            | What it tells you                                                                                                    |
|:------------------------------------|:----------------------------------------------------------------------------------------------------------------------|
| `stored:`                       | What the player picked, as saved to disk.                                                                            |
| `resolved:`                     | What actually applies right now. `tag=NONE` means nothing will be colored — usually a revoked permission.            |
| `entry:`                        | ChatColor's own permission check. If `hasPermission=false`, the problem is your permission setup, not the pipeline.   |
| `essentials.chat.*`             | Whether EssentialsX would let this player type their own color codes. Never affects ChatColor's own color.           |
| `renderer in place before us:`  | Whose renderer ChatColor is wrapping. ChatColor preserves it rather than replacing it.                               |
| `ourRendererStillInstalled`     | **`false` means a plugin replaced our renderer after us** — raise ChatColor's `event-priority`. `RENDERER` mode only. |
| `RENDERER RAN`                  | The renderer actually executed. If this is missing, a plugin delivered the message itself and bypassed rendering.     |
| `WROTE message (direct mode)`   | `DIRECT` mode equivalent of `RENDERER RAN`. The colored message is now on the event for the formatter to pick up.    |
| `ZELCHAT module coloured`       | ZelChat is installed and ChatColor colored the message inside it. See [ZelChat](#zelchat).                           |

In `DIRECT` mode the trace looks like this instead, and the color should still be visible on the
`MONITOR` line — that is the proof it survived the formatter:

```
[ChatColor] [DEBUG]   WROTE message (direct mode, we do not render)
[ChatColor] [DEBUG]     in : hello
[ChatColor] [DEBUG]     out: {#FCB6E1}hello
[ChatColor] [DEBUG]   MONITOR:  cancelled=false viewers=2
[ChatColor] [DEBUG]     renderer: io.papermc.paper.chat.ViewerUnawareImpl
[ChatColor] [DEBUG]     message : {#FCB6E1}hello
```

Section signs are printed as `(S)` and MiniMessage angle brackets as `{`/`}` so that the
`clean-console` filter cannot strip the very codes you are trying to inspect.

### Diagnosing from the trace

| Symptom                                                        | Cause                                                        | Fix                                                          |
|:--------------------------------------------------------------------|:-------------------------------------------------------------------|:-------------------------------------------------------------------|
| `entry: ... hasPermission=false`                               | Player lacks the color's permission node                     | Grant it, or make the entry public                           |
| `resolved: tag=NONE`                                           | No color selected and no group default matches               | Set `default-color` or a `group-defaults` entry              |
| `late-bind is on, ChatColor stops here`                        | `late-bind: true`                                            | Set it to `false` unless a plugin places `%chatcolor_message%` |
| `apply-to-message is off, ChatColor stops here`                | `apply-to-message: false`                                    | Set it to `true`                                             |
| `[LEGACY]` on a Paper server                                   | `chat-hook` forced to `LEGACY`                               | Set `chat-hook: "AUTO"`                                      |
| `ourRendererStillInstalled=false`                              | A plugin at a later priority replaced the renderer           | Set `event-priority: "MONITOR"` to get the last word         |
| No `RENDERER RAN` line                                         | A plugin delivered the message itself, bypassing rendering   | Check that plugin's Paper-chat setting                       |
| `RENDERER RAN` with a colored `out:`, but chat is still plain  | The plugin below us ignores the message its renderer is given | Set `message-mode: "DIRECT"` and `event-priority: "HIGH"`   |
| `mode=DIRECT` with `priority=HIGHEST` or `MONITOR`             | Forced `event-priority` puts us behind the formatter's read  | Set `event-priority: "DEFAULT"`                              |

---

## Known plugin interactions

### EssentialsChat

Works out of the box on `AUTO`. Paper only: EssentialsX doesn't support Folia, and Folia refuses to
load it. Use the standard `{MESSAGE}` tag in the Essentials format:

```yaml
group-formats:
  Default: '{DISPLAYNAME}&7: {MESSAGE}'
```

EssentialsChat is the reason **direct mode** exists. Its Paper renderer is built like this:

```java
// EssentialsX, PaperChatListenerProvider, HIGHEST
final Component eventMessage = serializer.deserialize(paperChatEvent.getMessage()); // copy taken here
event.renderer(ChatRenderer.viewerUnaware((player, displayName, message) ->
        format.replaceText(/* %1$s -> displayName, %2$s -> eventMessage */)));
//                                          ^^^^^^^ never read
```

The renderer substitutes `eventMessage` — the copy it took at `HIGHEST` — and ignores the
`message` parameter entirely. Any plugin that wraps this renderer to color chat is colouring a
value that gets thrown away.

So on `AUTO`, ChatColor detects EssentialsChat and:

- binds at `HIGH` instead of `HIGHEST`, putting it in front of the `HIGHEST` handler that takes
  the copy, and
- writes the colored component into `event.message()` rather than installing a renderer.

EssentialsChat then reads an already-colored message and formats around it. Both settings are
automatic; leave `event-priority` and `message-mode` on their defaults.

> **Do not force `event-priority: "HIGHEST"` or `"MONITOR"` with EssentialsChat installed.** That
> puts ChatColor behind the copy and colors will silently stop working. ChatColor logs a warning
> at startup if you do.

**You do not need to grant `essentials.chat.color` / `essentials.chat.rgb`.** Those nodes control
whether players may type their own `&` codes. EssentialsChat's color filter runs in its `LOWEST`
handler, well before ChatColor colors anything at `HIGH`, so it never sees our color. Leave them
off unless you *want* players hand-coloring their own chat.

> If a player's chat shows orphaned `§x` markers (`§xt§xt§xt`), EssentialsChat is stripping hex
> codes out of a legacy-pipeline message. Its strip pattern removes `§0`-`§9`/`§a`-`§f` but not
> the leading `§x`, which is what leaves the debris. Set `chat-hook: "AUTO"` to fix it.

### LPC (LuckPermsChat) and LPC-Pro

Both need the placeholder. **Put `%chatcolor_message%` where `{message}` was** and set
`apply-to-message: false` and `late-bind: true` in ChatColor's `config.yml`.

Neither one formats the message it is handed, so ChatColor can't color `{message}`:

- **LPC 4.x** reads the chat message as plain text and rebuilds it (adding its own mentions,
  links, emoji and `[item]`), so any color on the chat event is lost.
- **LPC-Pro** cancels the chat event and sends every message itself, so ChatColor's renderer
  never runs.

**LPC 4.x**, in `plugins/LPC/config.yml` (and any `group-formats` you use):

```yaml
chat-format: "{prefix}{name}<dark_gray> »<reset> %chatcolor_message%"
```

Leave `papi-output: "AUTO"`. LPC 4.x reads its whole format as MiniMessage, and `AUTO` returns
MiniMessage for it. See [PlaceholderAPI](placeholderapi.md#2-lpc-and-lpc-pro--placeholder-required).

**LPC-Pro**, in each `plugins/LPC-Pro/formats/groups/<group>.yml`:

```yaml
message:
  text: "&f%chatcolor_message%"
```

> **LPC-Pro takes over ChatColor's commands.** Its own color shop registers `/color` with the
> aliases `/chatcolor` and `/colors`, which are all three of ChatColor's commands. Turn it off in
> `plugins/LPC-Pro/advanced/command-settings.yml` and restart:
>
> ```yaml
> commands:
>   color:
>     enabled: false
> ```
>
> Until you do, players reach ChatColor only through `/chatcolor:color`, and ChatColor warns about
> the clash at startup. LPC-Pro's shop colors also paint over ChatColor's for anyone holding its
> `lpcpro.color.*` permissions, so use one color system, not both.

Because LPC builds the line from the placeholder, LPC's own mentions, links and `[item]` don't
apply inside the message.

Tested with LPC 4.4.0 and LPC-Pro 2.5.1 on Paper and Folia 26.3.

### ChatManager

Works out of the box on `AUTO`. Keep `{message}` in ChatManager's `Chat_Format`:

```yaml
Chat_Format:
  Enable: true
  Default_Format: "%luckperms_prefix% &7{player} &9> {message}"
```

ChatManager builds its chat line on the legacy `AsyncPlayerChatEvent` at `HIGHEST`: it runs
PlaceholderAPI over the format, pastes the message text into `{message}` and sets the result as the
format. The message is baked in as plain text, so a chat renderer has nothing left to color.

So on `AUTO`, ChatColor detects ChatManager and:

- binds the **legacy** chat event instead of the modern one, at `HIGH`, in front of ChatManager's
  `HIGHEST` handler, and
- writes the colored message into the event, which ChatManager then pastes into `{message}`.

The startup line reads `Chat hook: LEGACY (AsyncPlayerChatEvent), priority HIGH`.

**Placeholder route.** To place the color with `%chatcolor_message%` in ChatManager's format instead
of `{message}`, set `apply-to-message: false` in ChatColor's `config.yml`. `late-bind` stays `false`.

> **Do not force `event-priority: "HIGHEST"` or `"MONITOR"` with ChatManager installed.** ChatColor
> would color the message after ChatManager has already pasted it in. ChatColor logs a warning at
> startup if you do.

ChatManager's own color permissions and `Formatted_Messages` only govern `&` codes players type.

### ZelChat

Works out of the box. Keep `{message}` in ZelChat's formats (`formatting/format-components.yml`):

```yaml
default_format:
  '2':
    message: '&7{message}'
```

Solid colors, gradients and patterns all apply, and ZelChat's `[item]`, `[inv]`, `[ec]` and
mentions keep working.

ZelChat works differently from the other formatters. It rebuilds every message from its plain
text, so anything written to the chat event is thrown away, and its renderer ignores the message
it is handed. So ChatColor doesn't color the chat event when ZelChat is installed. It registers a
ZelChat **chat module** instead, which colors the message inside ZelChat after ZelChat's own
modules have run and before ZelChat swaps `[item]` and mentioned names for their interactive
components. While coloring, ChatColor keeps those tokens whole. A gradient or pattern colors one
letter at a time, and splitting `[item]` into six letters would hide it from ZelChat.

ChatColor reads the `[item]`/`[inv]`/`[ec]` prefixes from ZelChat's `settings.yml`, and
`/color reload` picks up changes to them.

> **Don't use `%chatcolor_message%` in a ZelChat format.** ZelChat only looks for `[item]` inside
> `{message}`. A placeholder's text is pasted in after that, so `[item]` stays as plain text.
> `%chatcolor_color%{message}` shows the item, but it can't do patterns.

`/color debug` prints a `ZELCHAT module coloured the message` line for each message, and the
pipeline dump reports `zelchat: hooked`.

### DiscordSRV

Works automatically. For best results set `UseModernPaperChatEvent: true` in DiscordSRV's config
so it reads the same event ChatColor renders on.

### Chat-input plugins (WorldGuard, mcMMO, HeadDatabase, GUI prompts)

These listen to `AsyncPlayerChatEvent` to capture input, not to format chat. They will show up in
the `AsyncPlayerChatEvent` section of the pipeline dump and are harmless — ChatColor deliberately
ignores them when choosing a hook.

---

## Folia

ChatColor is Folia-compatible (`folia-supported: true`).

- `AUTO` resolves to `MODERN` on Folia the same as on Paper, since Folia is a Paper fork.
- The renderer only touches thread-safe state — permission checks and an in-memory data cache,
  with no PlaceholderAPI call in the render path — so it is safe on Folia's regional threads.
- Listener binding uses the global region scheduler.
- `event-priority: "DEFAULT"` resolves to `HIGHEST` on the modern hook without needing to
  recognise any chat plugin by name, so ChatColor binds late on Folia the same as on Paper.

That last point used to be a real gap: auto-detection looked for EssentialsChat, LPC, or
DeluxeChat, and since none of those run on Folia, ChatColor bound at `NORMAL` there and could be
overwritten by a Folia-compatible chat plugin at a higher priority. Fixed.
