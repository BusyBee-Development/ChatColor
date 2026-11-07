---
id: intro
title: Introduction
sidebar_position: 1
---

ChatColor is a chat color plugin for **Paper**, **Purpur** and **Folia** servers on 1.21 and newer.
Players pick a solid color, a multi-stop gradient or a character-cycling pattern for their chat
from an in-game GUI or a command, and ChatColor applies it to every message, whichever chat
formatter the server runs.

## What's in this guide

- **[Getting Started](getting-started/quick-start.md)** — install the plugin and give players their first color.
- **[Core Reference](core-reference/commands-and-permissions.md)** — every command and permission node, and the color selector GUI.
- **[Configuration](configuration/configuration.md)** — `config.yml`, the color and pattern files, and how updates keep your edits.
- **[Integrations](integrations/placeholderapi.md)** — PlaceholderAPI, and how ChatColor works alongside EssentialsChat, LPC, ChatManager and DiscordSRV.
- **[Advanced](advanced/developer-api.md)** — the `ChatColorAPI` for other plugins.
- **[Support](support/faq.md)** — FAQ, common issues, and how to get help.

## Key features

- **Solid colors, gradients and patterns**, each with its own permission node, plus custom colors you add from a command.
- **Color selector GUI** with a main menu and a page per category, fully configurable in `gui/gui.yml`.
- **Works with the chat formatter you already run**: EssentialsChat, LPC and ChatManager are detected and set up automatically.
- **PlaceholderAPI placeholders** for the player's color, colored name and colored message.
- **`/color debug`** traces the whole chat pipeline into the console when something doesn't show up.
- **Folia support**, region-thread safe throughout.
