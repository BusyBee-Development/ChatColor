package net.busybee.chatcolor.pattern.manager;

import net.busybee.chatcolor.ChatColorPlugin;
import net.busybee.chatcolor.configuration.SimpleYMLConfiguration;
import net.busybee.chatcolor.pattern.api.BasePattern;
import net.busybee.chatcolor.pattern.format.TextFormatOptions;
import net.busybee.chatcolor.pattern.type.PatternType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class PatternManager {

    private Map<String, BasePattern> loadedPatternsMap;

    public PatternManager(){

        SimpleYMLConfiguration patterns = new SimpleYMLConfiguration("patterns.yml");
        patterns.loadFile();
        loadedPatternsMap = new HashMap<>();

        for (String key: patterns.getKeys(false)){
            load(patterns.getConfigurationSection(key));
        }

    }

    public void load(ConfigurationSection configurationSection){

        ChatColorPlugin.getInstance().sendConsoleMessage("&7Loading pattern "+configurationSection.getName()+"...");

        PatternType type = PatternType.SINGLE;
        String patternMode = configurationSection.getString("mode");

        try{
            type  = PatternType.valueOf(patternMode);
        } catch (Exception e){
            ChatColorPlugin.getInstance().sendConsoleMessage("&cPattern mode '"+patternMode+"' is invalid using '"+type+"' instead. valid pattern modes: "+String.join(", ", Arrays.toString(PatternType.values())));
        }

        String permission = configurationSection.getString("permission");
        String category = configurationSection.getString("category", "Regular");
        ChatColor[] colors = getColors(configurationSection.getStringList("colors"));

        try {

            TextFormatOptions textFormatOptions = TextFormatOptions.fromConfigurationSection(configurationSection);
            BasePattern pattern = type.buildPattern(configurationSection.getName(), category, permission, textFormatOptions, configurationSection, colors);

            if(pattern != null){
                loadedPatternsMap.put(configurationSection.getName(), pattern);
                ChatColorPlugin.getInstance().sendConsoleMessage("&7Loaded pattern "+pattern.getName(true)+"&7!");
            }

        } catch (Exception error){
            error.printStackTrace();
        }

    }
    private ChatColor[] getColors(List<String> colors){

        List<ChatColor> colorsList = new ArrayList<>();

        for(String colorString: colors){

            colorString = colorString.replaceAll("&", "");

            ChatColor color = ChatColor.WHITE;

            try {

                if(colorString.length() == 1){
                    color = ChatColor.getByChar(colorString.charAt(0));
                } else if (colorString.matches("#[a-zA-Z0-9]{6}")){
                    try {
                        color = ChatColor.of(colorString);
                    } catch (NoSuchMethodError ignored){}
                } else {
                    color = ChatColor.valueOf(colorString);
                }

            } catch (Exception e){

                ChatColorPlugin.getInstance().sendConsoleMessage("&CCannot load color '"+colorString+"' using '"+color.getName()+"' instead, valid formats are: 'f', '&&cf', 'WHITE', '#&cFFFFFF'");

            }


            colorsList.add(color);
        }

        return colorsList.toArray(new ChatColor[0]);

    }

    public BasePattern getPatternByName(String name){
        return loadedPatternsMap.get(name);
    }

    public List<BasePattern> getAllPatterns(){
        return new ArrayList<>(loadedPatternsMap.values());
    }

    public Map<String, List<BasePattern>> getPatternsByCategory() {
        Map<String, List<BasePattern>> categories = new TreeMap<>();
        for (BasePattern pattern : loadedPatternsMap.values()) {
            categories.computeIfAbsent(pattern.getCategory(), k -> new ArrayList<>()).add(pattern);
        }
        return categories;
    }

}
