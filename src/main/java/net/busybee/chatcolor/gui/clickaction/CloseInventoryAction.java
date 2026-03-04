package net.busybee.chatcolor.gui.clickaction;

import net.busybee.chatcolor.gui.clickaction.api.GuiClickAction;
import org.bukkit.entity.Player;

public class CloseInventoryAction implements GuiClickAction {

    public CloseInventoryAction(){
    }

    @Override
    public void execute(Player player) {

        player.closeInventory();

    }
}
