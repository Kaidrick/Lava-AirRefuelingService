package moe.ofs.addon.aarservice.domains;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import moe.ofs.backend.object.tasks.TacanModeChannel;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@Builder
public class Comm implements Serializable {
    private Map<Object, Object> callsign;
    private long frequency;
    private int channel;
    private TacanModeChannel modeChannel;
    private String beaconMorseCode;
    private boolean bearingInfoAvailable;
}
