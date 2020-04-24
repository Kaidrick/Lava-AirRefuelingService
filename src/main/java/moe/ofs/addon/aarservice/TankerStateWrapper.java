package moe.ofs.addon.aarservice;

import lombok.Getter;
import lombok.Setter;
import moe.ofs.addon.aarservice.domains.DispatchedTanker;
import moe.ofs.addon.aarservice.domains.TankerService;
import moe.ofs.backend.function.coordoffset.Offset;
import moe.ofs.backend.handlers.ExportUnitDespawnObservable;
import moe.ofs.backend.handlers.ExportUnitUpdateObservable;

import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.time.Instant;

/**
 * TankerStateWrapper monitors and controls the state of tanker mission
 * If tanker is near area of operation
 * If tanker is dead or no longer in mission env
 * If tanker is stationary for too long
 */

@Getter
@Setter
public class TankerStateWrapper {

    private final TankerService service;

    ExportUnitUpdateObservable observable;

    private final DispatchedTanker dispatchedTanker;

    private boolean initiated;

    private PropertyChangeSupport support;

    // a executor service will check timestamp periodically
    private Instant lastMotionTimestamp;

    public TankerStateWrapper(TankerService service, DispatchedTanker dispatchedTanker) {
        this.service = service;
        this.dispatchedTanker = dispatchedTanker;

        lastMotionTimestamp = Instant.now();

        observable = (previous, current) -> {
            if(service.getUnit().getId() == current.getRuntimeID()) {
                double dist = Offset.slantRange(previous.getPosition(), current.getPosition());
                // timestamp is updated if and only if dist > 1
                if(dist > 1) {
                    lastMotionTimestamp = Instant.now();
                }
            }
        };
        observable.register();

        // tanker destroyed
        ExportUnitDespawnObservable despawnObservable = exportObject -> {
            if(exportObject.getRuntimeID() == service.getUnit().getId()) {
                observable.unregister();
            }
        };
        despawnObservable.register();

    }

    public void dispose() {
        observable.unregister();
    }

    public boolean isTankerStuck(int maxWaitTime) {
        return Duration.between(lastMotionTimestamp, Instant.now()).getSeconds() > maxWaitTime;
    }


    // unregister if tanker is deleted, recalled or destroyed, or mission restarted
}
