package moe.ofs.addon.aarservice.domains;

import lombok.Getter;
import lombok.Setter;
import moe.ofs.addon.navdata.domain.NavFix;

@Getter
@Setter
public class BriefedWaypoint {
    private NavFix navFix;
    private double assignedAltitude;
}
