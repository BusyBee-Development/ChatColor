package me.mattyhd0.chatcolor.gui.clickaction;

import me.mattyhd0.chatcolor.gui.ChatColorGUI;
import me.mattyhd0.chatcolor.gui.clickaction.api.GuiClickAction;
import org.bukkit.entity.Player;

public class BackToCategoriesAction implements GuiClickAction {

    @Override
    public void execute(Player player) {
        ChatColorGUI.openGui(player);
    }

}
