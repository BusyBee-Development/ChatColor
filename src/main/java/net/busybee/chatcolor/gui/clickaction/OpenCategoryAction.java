package net.busybee.chatcolor.gui.clickaction;

import net.busybee.chatcolor.gui.ChatColorGUI;
import net.busybee.chatcolor.gui.clickaction.api.GuiClickAction;
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
