package moe.ofs.addon.aarservice.domains;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import moe.ofs.backend.domain.BaseEntity;
import moe.ofs.backend.object.Unit;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class TankerService extends BaseEntity {
    private Route route;
    private String aircraftType;
    private String tankerMissionName;
    private Integer startingAirdromeId;

    private Unit unit;
    private BriefedWaypoint holdingFix;

    private CustomPattern customPattern;

    public TankerService(Route route, String aircraftType, Integer startingAirdromeId, BriefedWaypoint holdingFix) {
        this.route = route;
        this.aircraftType = aircraftType;
        this.startingAirdromeId = startingAirdromeId;
        this.holdingFix = holdingFix;
    }

    @Override
    public String toString() {
        return aircraftType + " " + startingAirdromeId + " " + route;
    }
}
