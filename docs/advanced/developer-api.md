---
id: developer-api
title: Developer API
sidebar_position: 1
---

Add **ChatColor** as a dependency in your project to interact with player color data programmatically.

---

## Declaring the Dependency

Add ChatColor to your `plugin.yml` so it loads first:

```yaml
softdepend: [ChatColor]   # or depend: [ChatColor] if you cannot run without it
```

---

## Obtaining the API Instance

To use the API, obtain an instance of the `ChatColorAPI` from the main plugin class.

```java
ChatColor plugin = (ChatColor) Bukkit.getPluginManager().getPlugin("ChatColor");
if (plugin != null) {
    ChatColorAPI api = plugin.getChatColorAPI();
    // Your API calls here
}
```

---

## API Methods

### Set a Player's Color
```java
// Set a solid color by key
api.setColor(player, "red");

// Set a gradient by key
api.setGradient(player, "sunset");

// Set a pattern by key
api.setPattern(player, "rainbow");
```

### Reset a Player's Color
```java
// Remove all active color settings for a player
api.resetColor(player);
```

### Get Player Data
```java
// Get data by UUID
PlayerColorData data = api.getPlayerData(player.getUniqueId());

if (data != null) {
    String type = data.getColorType(); // "SOLID", "GRADIENT", "PATTERN", or "NONE"
    String key  = data.getColorKey();
    String tag  = data.getColorTag();

    boolean hasColor = data.hasColor(); // Returns true if a color/gradient/pattern is set
}
```

### Apply Color to Text
Apply a player's active selection (or their group default) to a string or an Adventure `Component`.

#### Applying to a String
```java
Component coloredText = api.applyColorToText(player, "Hello, world!");
```

#### Applying to a Component
```java
Component myComponent = Component.text("Hello, world!").decorate(TextDecoration.BOLD);
Component coloredComponent = api.applyColorToComponent(player, myComponent);
```

### Get Default Color
Retrieve the default color tag for a player based on their groups or permissions.
```java
String defaultTag = api.getDefaultColorForPlayer(player); // e.g., "<red>" or "NONE"
```

### Retrieve Available Colors, Gradients, and Patterns
Get collections of all registered color options.

```java
Collection<ColorEntry>    colors       = api.getAvailableColors();       // standard only
Collection<ColorEntry>    customColors = api.getAvailableCustomColors(); // custom only
Collection<ColorEntry>    allColors    = api.getAllColors();             // standard + custom
Collection<GradientEntry> gradients    = api.getAvailableGradients();
Collection<PatternEntry>  patterns     = api.getAvailablePatterns();
```

Every entry implements `SelectableEntry`, which is where the access helpers live:

```java
for (ColorEntry entry : api.getAllColors()) {
    entry.getKey();          // "pastel-pink"
    entry.getDisplayName();  // "Pastel Pink"
    entry.getTag();          // "<#FCB6E1>"
    entry.getPermission();   // "chatcolor.custom.pastel-pink", or null/blank if public

    entry.isPublic();          // true when no permission is required
    entry.isAllowed(player);   // true when the player may select it
}
```

---

## Resolving What Actually Applies

`getPlayerData` returns what the player *picked*. These two resolve what is actually **in effect
right now**, taking revoked permissions, deleted entries, and group defaults into account. This is
what you want when rendering.

```java
// The MiniMessage tag currently in effect, or null if nothing applies.
// Also returns null when a pattern is active, since a pattern is a list of colors, not one tag.
String tag = api.resolveActiveTag(player);

// The pattern currently in effect, or null if the player has none or may no longer use it.
PatternEntry pattern = api.resolveActivePattern(player);
```

The ordering both methods follow:

1. The player's selection, **if they still have permission for it**.
2. Otherwise the first matching `group-defaults` entry (`chatcolor.group.<name>`).
3. Otherwise `default-color` from `config.yml`.
4. Otherwise nothing.

If the selected entry has been removed from `colors.yml` entirely, the tag stored on the player is
used as a last resort so their chat doesn't suddenly change.

---

## Thread Safety

Color application happens on Paper's async chat thread, so the read path is built for it:

- `applyColorToText`, `applyColorToComponent`, `resolveActiveTag`, `resolveActivePattern`,
  `getPlayerData`, and the collection getters are all safe to call from any thread. They read a
  `ConcurrentHashMap`-backed cache and perform no I/O.
- `setColor`, `setGradient`, `setPattern`, and `resetColor` mutate player data and queue a disk
  write. Call them from the main thread (or the owning region thread on Folia).
