package me.mattyhd0.chatcolor.gui.clickaction;

import me.mattyhd0.chatcolor.gui.ChatColorGUI;
import me.mattyhd0.chatcolor.gui.clickaction.api.GuiClickAction;
import org.bukkit.entity.Player;

public class OpenCategoryAction implements GuiClickAction {

    private String category;

    public OpenCategoryAction(String category){
        this.category = category;
    }

    @Override
    public void execute(Player player) {
        ChatColorGUI.openPatterns(player, category, 1);
    }

}
