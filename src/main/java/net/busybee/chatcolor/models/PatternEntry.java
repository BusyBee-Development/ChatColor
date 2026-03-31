package net.busybee.chatcolor.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PatternEntry implements SelectableEntry {

    private final String key;
    private final String displayName;
    private final String permission;
    private final String iconMaterial;
    private final List<String> colors;

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public String getEntryType() {
        return "PATTERN";
    }
}
