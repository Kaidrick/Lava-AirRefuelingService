package moe.ofs.addon.aarservice.services;

import moe.ofs.addon.aarservice.domains.TankerService;
import moe.ofs.backend.services.map.AbstractMapService;
import org.springframework.stereotype.Service;

@Service
public class RefuelingServiceServiceImpl extends AbstractMapService<TankerService> implements RefuelingServiceService {

    @Override
    public TankerService save(TankerService object) {
        return super.save(object);
    }
}
