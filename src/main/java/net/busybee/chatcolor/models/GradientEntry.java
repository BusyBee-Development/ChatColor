package net.busybee.chatcolor.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GradientEntry implements SelectableEntry {

    private final String key;
    private final String displayName;
    private final String tag;
    private final String permission;
    private final String iconMaterial;

    @Override
    public String getEntryType() {
        return "GRADIENT";
    }
}
