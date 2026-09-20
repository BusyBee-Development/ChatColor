---
id: requirements
title: Requirements
sidebar_position: 3
---

## Requirements

- **Server Software:** Paper (recommended) or Spigot **1.21+**
- **Java Version:** **Java 21** or newer.
- **Optional Dependencies:** [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi/versions)
  for additional placeholder support. Essentials, EssentialsChat, and HeadDatabase are also
  soft-dependencies — ChatColor auto-detects and integrates with them if present, and runs fine
  without any of them installed.

### Platform support

| Platform         | Supported    | Notes                                                                                      |
|:-----------------|:-------------|:-------------------------------------------------------------------------------------------|
| Paper 1.21+      | ✅ Full      | Recommended. Uses the modern chat pipeline for exact gradient rendering.                   |
| Folia            | ✅ Full      | Region-thread safe. See [Chat Compatibility](../integrations/chat-compatibility.md#folia). |
| Canvas           | ✅ Full      | Declared supported in `plugin.yml`.                                                        |
| Spigot 1.21+     | ⚠️ Limited   | No modern chat event, so gradients are approximated to the nearest of the 16 named colors. |

---

Ready to install? See [Installation](installation.md), or jump straight to
[Quick Start](quick-start.md).
