package moe.ofs.addon.aarservice.domains;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CustomPattern implements Serializable {
    private double patternAltitude;
    private double patternLegLength;
    private double patternInbound;

    @Override
    public String toString() {
        return String.format("Pattern: ALT %.0f, LEG %.0f m, INBOUND %.0f",
                patternAltitude, patternLegLength, patternInbound);
    }
}
