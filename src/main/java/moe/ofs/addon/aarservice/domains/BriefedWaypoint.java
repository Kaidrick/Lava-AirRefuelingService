package moe.ofs.addon.aarservice.domains;

import lombok.Getter;
import lombok.Setter;
import moe.ofs.addon.navdata.domain.NavFix;
import moe.ofs.backend.function.unitconversion.Lengths;
import moe.ofs.backend.function.unitconversion.Speeds;

import java.io.Serializable;

@Getter
@Setter
public class BriefedWaypoint implements Serializable {
    private NavFix navFix;
    private double assignedAltitude;
    private double assignedSpeed;

    @Override
    public String toString() {
        return navFix.getCode() + " " +
                Speeds.metersPerSecondToKnots(assignedSpeed) + " kts / " +
                Lengths.metersToFeet(assignedAltitude) + " ft";
    }
}
