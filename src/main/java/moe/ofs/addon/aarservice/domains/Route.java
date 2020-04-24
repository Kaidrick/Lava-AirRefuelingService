package moe.ofs.addon.aarservice.domains;


import lombok.Getter;
import lombok.Setter;
import moe.ofs.addon.navdata.domain.NavFix;
import moe.ofs.backend.domain.BaseEntity;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * A route represent a series of GeoPosition that a tanker will fly through
 * during it mission.
 *
 * A route can have one or more orbit track assigned
 */
@Getter
@Setter
public class Route extends BaseEntity {
    private String name;
    private String description;

    private OrbitTrack track;

    private Queue<BriefedWaypoint> briefedWaypoints = new ArrayDeque<>();

    @Override
    public String toString() {
        return "[" + name + "] " +
                briefedWaypoints.stream()
                .map(BriefedWaypoint::getNavFix)
                .map(NavFix::getCode).collect(Collectors.joining(" "));
    }
}
