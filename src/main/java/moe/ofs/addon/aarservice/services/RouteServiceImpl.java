package moe.ofs.addon.aarservice.services;

import moe.ofs.addon.aarservice.domains.Route;
import moe.ofs.backend.services.map.AbstractMapService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RouteServiceImpl extends AbstractMapService<Route> implements RouteService {
    @Override
    public Optional<Route> findByName(String name) {
        return map.values().stream().filter(route -> route.getName().equals(name)).findAny();
    }
}
