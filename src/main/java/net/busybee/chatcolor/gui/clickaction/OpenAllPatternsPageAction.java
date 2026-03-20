package net.busybee.chatcolor.gui.clickaction;

import net.busybee.chatcolor.gui.ChatColorGUI;
import net.busybee.chatcolor.gui.clickaction.api.GuiClickAction;
import org.bukkit.entity.Player;

public class OpenAllPatternsPageAction implements GuiClickAction {

    private int page;

    public OpenAllPatternsPageAction(int page){
        this.page = page;
    }

    @Override
    public void execute(Player player) {
        ChatColorGUI.openAllPatterns(player, page);
    }

}
