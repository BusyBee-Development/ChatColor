package net.busybee.chatcolor.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerColorData {

    private UUID uuid;
    private String colorType;
    private String colorKey;
    private String colorTag;

    public boolean hasColor() {
        return colorType != null && !colorType.equals("NONE");
    }

    public void reset() {
        this.colorType = "NONE";
        this.colorKey = null;
        this.colorTag = null;
    }
}
