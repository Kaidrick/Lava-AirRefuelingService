package moe.ofs.backend;

import moe.ofs.backend.handlers.MissionStartObservable;
import moe.ofs.backend.services.FlyableUnitService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * This AirRefuelingService class is an example of an external addon/plugin for Lava Project
 * An addon should implement Plugin method to be able to read and write configurations
 * An addon can access API from Lava Project backend-core by autowiring util and services
 */
@Service
public class AirRefuelingService implements Plugin {

    private final FlyableUnitService service;

    public AirRefuelingService(FlyableUnitService service) {
        this.service = service;
    }

    public final String name = "Refueling Service";
    public final String desc = "Say Hello on player spawn";

    @Override
    public void register() {
        System.out.println("registering " + getName());
        MissionStartObservable missionStartObservable = () -> service.findAll().forEach(System.out::println);
        missionStartObservable.register();
    }

    @Override
    public void unregister() {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @PostConstruct
    @Override
    public void init() {
        System.out.println("AirRefuelingService plugin bean constructed...register -> new version!");
        Plugin.super.init();
        PluginClassLoader.loadedPluginSet.add(this);
        register();
    }
}
