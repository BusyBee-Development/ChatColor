---
id: common-issues
title: Common Issues
sidebar_position: 1
---

# Common Issues

A checklist for the most frequent reports. For a full technical trace of the chat pipeline, see
[Chat Compatibility](../integrations/chat-compatibility.md).

---

## Colors aren't appearing in chat

Work through this in order:

1. **Check the startup line.** It should read `Chat hook: MODERN (AsyncChatEvent), priority
   HIGHEST` on Paper. If it says `LEGACY` and you are on Paper, something set `chat-hook`
   explicitly — set it back to `AUTO` in `config.yml`.
2. **Confirm `apply-to-message: true`** and `late-bind: false` in `config.yml`. Either one turns
   message coloring off, by design — `late-bind: true` is only for placeholder-driven formatters
   like LPC, see [PlaceholderAPI](../integrations/placeholderapi.md).
3. **Run `/color debug`** as the affected player, say something, and read the console. See
   [Chat Compatibility](../integrations/chat-compatibility.md) for how to interpret it.

## Orphaned `§x` markers in chat or console

A player seeing `§xt§xt§xt` — stray `§x` markers — is the signature of another plugin stripping
hex color codes out of a legacy-pipeline message (EssentialsChat's strip pattern removes `§0`-`§9`
and `§a`-`§f` but not the leading `§x`, which leaves the debris). Setting `chat-hook: "AUTO"` (or
`MODERN`) fixes it — see [Chat Compatibility](../integrations/chat-compatibility.md#known-plugin-interactions).

## A color/gradient/pattern shows "no access" in the GUI

This is expected, not a bug. Entries a player lacks permission for are still shown in the GUI,
marked with `no-access` lore, so players can see what's available to unlock. Remove the entry
entirely from `colors.yml`/`patterns.yml` if you want it hidden instead. See
[GUI](../core-reference/gui.md#controlling-what-appears).

## A config change doesn't seem to have applied

- Most settings apply on `/color reload` — no restart needed, including `gui/gui.yml`.
- If you deleted a standard color/gradient/pattern and it came back after updating, this only
  happens on the **first** update after ChatColor's config-migration ledger shipped — delete it
  once more and it stays gone. See
  [Automatic Configuration Updates](../configuration/configuration.md#deleted-keys-and-the-one-exception).
- If a file won't load at all, check `plugins/ChatColor/backups/` — invalid YAML is moved there
  as `<name>.corrupted-<timestamp>` and replaced with a fresh copy so the server still starts.

## After updating the plugin, something behaves differently

Read the startup migration summary in console (`=== ... Migration Summary ===`). Lines marked `!`
are the only ones needing action — see
[Reading the startup log](../configuration/configuration.md#reading-the-startup-log) for what
each marker means. Your previous file version is always in `backups/` if you need to recover a
value.
