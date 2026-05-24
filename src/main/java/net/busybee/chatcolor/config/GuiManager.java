package net.busybee.chatcolor.config;

import net.busybee.chatcolor.ChatColor;
import net.busybee.chatcolor.utils.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GuiManager {

    private final ChatColor plugin;
    private FileConfiguration guiConfig;

    public GuiManager(ChatColor plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.guiConfig = new net.busybee.chatcolor.utils.ConfigMigrator(plugin).migrate("configs/gui.yml");
    }

    public Component get(String key) {
        String raw = this.guiConfig.getString(key, "<red>Missing GUI string: " + key);
        return ColorUtil.colorize(raw);
    }

    public Component get(String key, Map<String, String> placeholders) {
        String raw = this.guiConfig.getString(key, "<red>Missing GUI string: " + key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            raw = raw.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return ColorUtil.colorize(raw);
    }

    public List<Component> getList(String key) {
        List<String> rawList = this.guiConfig.getStringList(key);
        if (rawList.isEmpty()) {
            return List.of(ColorUtil.colorize("<red>Missing GUI list: " + key));
        }
        return rawList.stream()
                .map(ColorUtil::colorize)
                .collect(Collectors.toList());
    }

    public List<Component> getList(String key, Map<String, String> placeholders) {
        List<String> rawList = this.guiConfig.getStringList(key);
        if (rawList.isEmpty()) {
            return List.of(ColorUtil.colorize("<red>Missing GUI list: " + key));
        }
        return rawList.stream()
                .map(line -> {
                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                        line = line.replace("<" + entry.getKey() + ">", entry.getValue());
                    }
                    return ColorUtil.colorize(line);
                })
                .collect(Collectors.toList());
    }

    public String getRaw(String key) {
        return this.guiConfig.getString(key, "<red>Missing GUI string: " + key);
    }
    public int getInt(String key, int def) {
        return this.guiConfig.getInt(key, def);
    }
    public List<Integer> getIntList(String key) {
        return this.guiConfig.getIntegerList(key);
    }

    public Material getMaterial(String key, Material def) {
        String name = this.guiConfig.getString(key);
        if (name == null) return def;
        Material mat = Material.matchMaterial(name);
        return mat != null ? mat : def;
    }

    public ItemStack getItemStack(String key, Material defMat) {
        Material mat = getMaterial(key + ".material", defMat);
        String name = getRaw(key + ".name");
        List<Component> lore = getList(key + ".lore");

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.colorize(name));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack getFillerItem() {
        Material mat = getMaterial("filler.material", Material.GRAY_STAINED_GLASS_PANE);
        String name = getRaw("filler.name");
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.colorize(name));
            item.setItemMeta(meta);
        }
        return item;
    }
}
