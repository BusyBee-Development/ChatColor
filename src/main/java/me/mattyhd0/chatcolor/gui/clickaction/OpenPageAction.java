package me.mattyhd0.chatcolor.gui.clickaction;

import me.mattyhd0.chatcolor.gui.ChatColorGUI;
import me.mattyhd0.chatcolor.gui.clickaction.api.GuiClickAction;
import org.bukkit.entity.Player;

public class OpenPageAction implements GuiClickAction {

    private String category;
    private int page;

    public OpenPageAction(String category, int page){
        this.category = category;
        this.page = page;
    }

    @Override
    public void execute(Player player) {
        ChatColorGUI.openPatterns(player, category, page);
    }

}
