package moe.ofs.addon.aarservice.services;

import moe.ofs.addon.aarservice.domains.Route;
import moe.ofs.backend.services.CrudService;

import java.util.Optional;

public interface RouteService extends CrudService<Route> {
    Optional<Route> findByName(String name);
}
