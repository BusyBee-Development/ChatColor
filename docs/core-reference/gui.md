---
id: gui
title: GUI
sidebar_position: 2
---

# GUI

The graphical interface of **ChatColor** is fully customizable via the `gui/gui.yml` file. This allows you to change everything from titles and item materials to the exact layout of the menus.

---

## 📂 Configuration File: `gui/gui.yml`

The file is divided into several main sections:

### 1. `titles`
Define the titles for each page of the GUI. MiniMessage formatting is supported.

```yaml
titles:
  main: "<gradient:blue:aqua><bold>ChatColor"
  solid: "<aqua><bold>Solid Colors"
  gradient: "<gradient:red:blue><bold>Gradients"
  pattern: "<rainbow><bold>Patterns"
```

### 2. `layouts`
Control the size of the inventory and where items are placed.

- **`main`**: The category selection menu.
  - `size`: Must be a multiple of 9 (e.g., 27, 54).
  - `filler-slots`: Slots that will be filled with the filler item.
  - `solid-slot`, `gradient-slot`, `pattern-slot`, `reset-slot`: Specific slots for category buttons.

- **`selector`**: The menu showing the actual colors/gradients/patterns.
  - `size`: Usually 54.
  - `content-slots`: Number of slots used for displaying entries.
  - `nav-row-start`: The row where navigation buttons start.

### 3. `items`
Customize the appearance of button items.

```yaml
items:
  solid:
    material: LIME_DYE
    name: "<green><bold>Solid Colors"
    lore:
      - "<gray>Browse and apply solid colors"
  reset:
    material: BARRIER
    name: "<red><bold>Reset Color"
```

### 4. `status`
These messages are appended to the lore of color items based on the player's status.

- `selected`: Shown if the player currently has this color active.
- `click-to-apply`: Shown if the player has access but hasn't selected it.
- `has-access`: General access indicator.
- `no-access`: Shown if the player lacks the required permission.

---

## 🛠️ Tips for Customization

1. **Filler Items**: Change `filler.material` and `filler.name` to match your server's theme.
2. **Navigation**: You can change the materials for `next-page`, `previous-page`, and `back-menu` items in the `items` section.
3. **MiniMessage**: Use the [MiniMessage Viewer](https://webui.advntr.dev/) to preview your titles and lore before applying them.
4. **Live Reload**: `/color reload` re-reads `gui.yml`, so you can iterate on a layout without restarting. Menus already open keep the old layout until reopened.

---

## Controlling What Appears

Two things filter the selector menus, and they are configured elsewhere:

- **`show-standard-colors` / `show-standard-gradients` / `show-standard-patterns`** in
  `config.yml` skip loading the bundled entries entirely, which is how you present a purely custom
  palette without deleting anything from `colors.yml`. Because they are never loaded, they also
  stop working in `/color set` and the API — not just in the GUI. Custom colors are unaffected.
- **`chatcolor.gui.solid` / `.gradient` / `.pattern`** hide whole categories from the main menu
  per player.

Individual entries a player lacks permission for are still displayed, marked with the `no-access`
status lore, so players can see what is available to unlock. Remove the entry from `colors.yml`
if you want it hidden entirely. This is expected behavior, not a bug — see
[Common Issues](../support/common-issues.md) if a player reports "missing" colors that are
actually just locked.
