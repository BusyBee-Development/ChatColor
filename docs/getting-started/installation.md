---
id: installation
title: Installation
sidebar_position: 1
---

Setting up **ChatColor** on your Minecraft server is a quick and easy process.

Looking for the fast version, or the full requirements list? See
[Quick Start](quick-start.md) and [Requirements](requirements.md).

---

## Setup Steps

1. **Download:** Get the latest `ChatColor.jar` file.
2. **Install:** Place the `.jar` file into your server's `plugins/` directory.
3. **Start Server:** Start (or restart) your server to generate the default configuration files.
4. **Configure:** Adjust the configuration files in the `plugins/ChatColor/` folder (see [Configuration](../configuration/configuration.md)).
5. **Reload:** Use `/color reload` to apply any changes made to the configuration files while the server is running.

---

## Verifying the install

On startup, ChatColor logs which chat pipeline it hooked:

```
[ChatColor] Chat hook: MODERN (AsyncChatEvent), priority HIGHEST
```

`MODERN` is what you want on Paper and Folia. If it says `LEGACY` on those platforms, check
`chat-hook` in `config.yml` — it should be `"AUTO"`.

The plugin also reports how many color permissions it registered:

```
[ChatColor] Registered 42 colour permission(s).
```

Those nodes are registered with the server at runtime, which is what lets LuckPerms tab-complete
them and lets `chatcolor.custom.*` actually expand.

---

## Updating

Drop the new jar in and restart. ChatColor migrates your configuration automatically: it adds any
new keys, preserves your existing values, and writes a timestamped backup to `backups/` first. See
[Configuration](../configuration/configuration.md#automatic-configuration-updates) for the full
details of what is kept, refreshed, or removed.

Use a **full restart** rather than a plugin reloader when updating. The chat listener binds one
tick after enable so it can register after other chat plugins, and reloader plugins can break
that ordering.

---

## Troubleshooting

Having trouble after install? See [Common Issues](../support/common-issues.md) for a checklist,
or [Chat Compatibility](../integrations/chat-compatibility.md) for a deep dive into
`/color debug` output.
