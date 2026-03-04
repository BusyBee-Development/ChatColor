package net.busybee.chatcolor.gui.clickaction;

import net.busybee.chatcolor.gui.clickaction.api.GuiClickAction;
import org.bukkit.entity.Player;

public class PlayerCommandAction implements GuiClickAction {

    private String command;

    public PlayerCommandAction(String command){
        this.command = command;
    }

    @Override
    public void execute(Player player) {

        player.performCommand(command);

    }
}
