---
id: faq
title: FAQ
sidebar_position: 2
---

# FAQ

## General Questions

**What server software and Java version does ChatColor need?**
Paper (recommended) or Spigot 1.21+, and Java 21 or newer. See
[Requirements](../getting-started/requirements.md).

**Does ChatColor support Folia?**
Yes — full support, region-thread safe throughout. See
[Chat Compatibility](../integrations/chat-compatibility.md#folia).

**Does ChatColor support Canvas?**
Yes, it's declared supported in `plugin.yml`.

**How do players open the color selector?**
Run `/color` (aliases `/chatcolor`, `/colors`). See
[Commands & Permissions](../core-reference/commands-and-permissions.md).

**Can players create their own custom colors?**
Only with the `chatcolor.create` permission, via `/color create`. See
[Commands & Permissions](../core-reference/commands-and-permissions.md#creating-and-deleting-custom-colors).

## Configuration

**Will updating the plugin wipe my configuration?**
No. ChatColor migrates your configs automatically on update — your values are kept, new keys are
added, and a backup is written first. See
[Automatic Configuration Updates](../configuration/configuration.md#automatic-configuration-updates).

**Do I need to restart to apply config changes?**
No for most settings — `/color reload` re-reads everything, including `gui/gui.yml`. Use a full
restart specifically when *updating the plugin jar itself*, since the chat listener needs to
rebind after other chat plugins load. See
[Installation](../getting-started/installation.md#updating).

## Compatibility

**Will ChatColor conflict with EssentialsChat, LPC, or DiscordSRV?**
No — all three are handled automatically or via a documented one-time setting. See
[Chat Compatibility](../integrations/chat-compatibility.md) and
[PlaceholderAPI](../integrations/placeholderapi.md).

**Do I need PlaceholderAPI installed?**
No, it's optional. ChatColor colors chat directly on its own pipeline; PlaceholderAPI is only
needed if you want *other* plugins (scoreboards, tablists, placeholder-driven chat formatters) to
reference a player's color. See [PlaceholderAPI](../integrations/placeholderapi.md).

## Troubleshooting

**Colors aren't appearing in chat, what do I do?**
Work through [Common Issues](common-issues.md), and run `/color debug` for a full pipeline trace
— see [Chat Compatibility](../integrations/chat-compatibility.md) for how to read the output.
