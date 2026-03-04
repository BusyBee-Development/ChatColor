package net.busybee.chatcolor.gui.clickaction;

import net.busybee.chatcolor.gui.clickaction.api.GuiClickAction;
import net.busybee.chatcolor.util.Util;
import org.bukkit.entity.Player;

public class SendMessageAction implements GuiClickAction {

    private String message;

    public SendMessageAction(String message){
        this.message = message;
    }

    @Override
    public void execute(Player player) {

        player.sendMessage(Util.color(message));

    }
}
