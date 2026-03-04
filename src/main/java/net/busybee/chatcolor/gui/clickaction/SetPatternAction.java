package net.busybee.chatcolor.gui.clickaction;

import net.busybee.chatcolor.CPlayer;
import net.busybee.chatcolor.ChatColorPlugin;
import net.busybee.chatcolor.gui.clickaction.api.GuiClickAction;
import net.busybee.chatcolor.pattern.api.BasePattern;
import org.bukkit.entity.Player;

public class SetPatternAction implements GuiClickAction {

    private String patternName;

    public SetPatternAction(String patternName){
        this.patternName = patternName;
    }

    @Override
    public void execute(Player player) {

        BasePattern pattern = ChatColorPlugin.getInstance().getPatternManager().getPatternByName(patternName);
        CPlayer cPlayer = ChatColorPlugin.getInstance().getDataMap().get(player.getUniqueId());
        if(cPlayer != null) cPlayer.setPattern(pattern);
    }
}
