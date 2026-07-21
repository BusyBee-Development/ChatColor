package net.busybee.chatcolor.data;

import java.util.UUID;

public class PlayerColorData {

    private UUID uuid;
    private String colorType;
    private String colorKey;
    private String colorTag;

    public PlayerColorData() {}

    public PlayerColorData(UUID uuid, String colorType, String colorKey, String colorTag) {
        this.uuid = uuid;
        this.colorType = colorType;
        this.colorKey = colorKey;
        this.colorTag = colorTag;
    }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public String getColorType() { return colorType; }
    public void setColorType(String colorType) { this.colorType = colorType; }
    public String getColorKey() { return colorKey; }
    public void setColorKey(String colorKey) { this.colorKey = colorKey; }
    public String getColorTag() { return colorTag; }
    public void setColorTag(String colorTag) { this.colorTag = colorTag; }

    public boolean hasColor() {
        return colorType != null && !colorType.equals("NONE");
    }

    public void reset() {
        this.colorType = "NONE";
        this.colorKey = null;
        this.colorTag = null;
    }
}
