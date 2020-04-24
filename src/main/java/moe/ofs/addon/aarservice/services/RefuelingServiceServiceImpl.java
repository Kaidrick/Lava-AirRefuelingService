package moe.ofs.addon.aarservice.services;

import moe.ofs.addon.aarservice.domains.TankerService;
import moe.ofs.backend.handlers.MissionStartObservable;
import moe.ofs.backend.services.map.AbstractMapService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class RefuelingServiceServiceImpl extends AbstractMapService<TankerService> implements RefuelingServiceService {

    @PostConstruct
    private void resetOnMissionRestart() {
        MissionStartObservable observable = s -> {
            deleteAll();
        };
        observable.register();
    }

    @Override
    public TankerService save(TankerService object) {
        return super.save(object);
    }
}
