package moe.ofs.addon.aarservice.services;

import moe.ofs.addon.aarservice.domains.DispatchedTanker;
import moe.ofs.backend.handlers.MissionStartObservable;
import moe.ofs.backend.services.mizdb.AbstractMissionDataService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MissionPersistentTankerService extends AbstractMissionDataService<DispatchedTanker> {

    @Override
    public String getRepositoryName() {
        return "tanker service db";
    }

    @PostConstruct
    private void register() {
        MissionStartObservable missionStartObservable = s -> createRepository();
        missionStartObservable.register();
    }
}
