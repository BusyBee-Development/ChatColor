package net.busybee.chatcolor;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyChatColor {

    public static String translateAlternateColorCodes(String textToTranslate, Player player) {
        char altColorChar = '&';
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            int next = i + 1;
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                ChatColor color = ChatColor.getByChar(b[next]);
                if(color == null) continue;
                if (player.hasPermission("chatcolor.*") || hasColorPermission(player, color)) {
                    b[i] = ChatColor.COLOR_CHAR;
                    b[next] = Character.toLowerCase(b[next]);
                } else {
                    b[i] = Character.MIN_VALUE;
                    b[next] = Character.MIN_VALUE;
                }
            }
        }

        String result = new String(b).replace(String.valueOf(Character.MIN_VALUE), "");
        result = applyHex(result, player);
        result = applyGradient(result, player);
        return result;
    }

    public static String applyHex(String textToTranslate, Player player){

        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(textToTranslate);

        if(textToTranslate.length() > 0){

            while (matcher.find()) {

                String color = textToTranslate.substring(matcher.start()+1, matcher.end());

                try {
                    ChatColor md5Color = ChatColor.of(color);
                    if(hasColorPermission(player, md5Color)){
                        textToTranslate = textToTranslate.replace("&"+color, md5Color.toString());
                    } else {
                        textToTranslate = textToTranslate.replace("&"+color, "");
                    }
                } catch (NoSuchMethodError ignored){
                }

                matcher = pattern.matcher(textToTranslate);

            }

        }

        return textToTranslate;

    }

    public static boolean isHex(ChatColor color){
        return (isColor(color) && color.getName().startsWith("#"));
    }
    public static boolean isFormat(ChatColor color){
        return (color.getColor() == null);
    }
    public static boolean isColor(ChatColor color){
        return (color.getColor() != null);
    }


    public static boolean hasColorPermission(Player player, ChatColor color){
        for (String permission: getPermissionsOf(color)){
            if(player.hasPermission(permission)){
                return true;
            }
        }

        return false;
    }

    public static String[] getPermissionsOf(ChatColor color){

        if(isHex(color)){
            return new String[]{ "chatcolor.hex.*", "chatcolor.hex."+color.getName().replace("#", "") };
        }

        if(isColor(color)){
            return new String[]{ "chatcolor.color.*", "chatcolor.color."+color.getName()};
        }

        if(isFormat(color)) {
            return new String[]{ "chatcolor.format.*", "chatcolor.format."+color.getName()};
        }

        return new String[]{"chatcolor.unknown"};

    }

    public static String applyGradient(String textToTranslate, Player player) {
        Pattern pattern = Pattern.compile("<gradient:(#[a-fA-F0-9]{6}):(#[a-fA-F0-9]{6})>([^<]+)</gradient>");
        Matcher matcher = pattern.matcher(textToTranslate);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String startColorHex = matcher.group(1);
            String endColorHex = matcher.group(2);
            String text = matcher.group(3);

            try {
                if (player.hasPermission("chatcolor.*") || player.hasPermission("chatcolor.gradient")) {
                    String gradientText = createGradient(text, startColorHex, endColorHex);
                    matcher.appendReplacement(result, Matcher.quoteReplacement(gradientText));
                } else {
                    matcher.appendReplacement(result, Matcher.quoteReplacement(text));
                }
            } catch (Exception e) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(text));
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String createGradient(String text, String startHex, String endHex) {
        try {
            Color startColor = Color.decode(startHex);
            Color endColor = Color.decode(endHex);

            float rStart = startColor.getRed();
            float gStart = startColor.getGreen();
            float bStart = startColor.getBlue();

            float rEnd = endColor.getRed();
            float gEnd = endColor.getGreen();
            float bEnd = endColor.getBlue();

            int length = text.length();
            if (length == 0) return text;

            float rStep = (rEnd - rStart) / length;
            float gStep = (gEnd - gStart) / length;
            float bStep = (bEnd - bStart) / length;

            StringBuilder gradientText = new StringBuilder();

            for (int i = 0; i < length; i++) {
                float r = rStart + (rStep * i);
                float g = gStart + (gStep * i);
                float b = bStart + (bStep * i);

                Color color = new Color(r / 255f, g / 255f, b / 255f);
                ChatColor chatColor = ChatColor.of(color);
                gradientText.append(chatColor).append(text.charAt(i));
            }

            return gradientText.toString();
        } catch (Exception e) {
            return text;
        }
    }

}
