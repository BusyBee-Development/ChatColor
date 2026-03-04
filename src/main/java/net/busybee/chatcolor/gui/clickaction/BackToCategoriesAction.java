package net.busybee.chatcolor.gui.clickaction;

import net.busybee.chatcolor.gui.ChatColorGUI;
import net.busybee.chatcolor.gui.clickaction.api.GuiClickAction;
import org.bukkit.entity.Player;

public class BackToCategoriesAction implements GuiClickAction {

    @Override
    public void execute(Player player) {
        ChatColorGUI.openGui(player);
    }

}
