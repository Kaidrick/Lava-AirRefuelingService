package moe.ofs.addon.aarservice.domains;


import lombok.Getter;
import lombok.Setter;
import moe.ofs.backend.domain.BaseEntity;

import java.util.ArrayDeque;
import java.util.Queue;

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

}
