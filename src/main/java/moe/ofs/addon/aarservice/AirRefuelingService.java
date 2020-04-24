package moe.ofs.addon.aarservice;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;
import moe.ofs.addon.aarservice.gui.controllers.AirRefuelingServiceControlPanel;
import moe.ofs.addon.navdata.gui.controllers.MainAnchorPane;
import moe.ofs.backend.Plugin;
import moe.ofs.backend.UTF8Control;
import moe.ofs.backend.Viewable;
import moe.ofs.backend.handlers.MissionStartObservable;
import moe.ofs.backend.services.ParkingInfoService;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * This AirRefuelingService class is an example of an external addon/plugin for Lava Project
 * An addon should implement Plugin method to be able to read and write configurations
 * An addon can access API from Lava Project backend-core by autowiring util and services
 *
 *
 * AirRefuelingService will automatically add AAR aircraft to the sim on mission start
 * It can spawn aircraft based on predefined templates or user define routes
 * User should specify the airport at which the AAR service aircraft should be based
 * By default it will use a route provided by this addon, but end user can define route by adding a Location list
 */

@Slf4j
@Component
public class AirRefuelingService implements Plugin, Viewable {

    private final String name = "Refueling Service";
    private final String desc = "Provide auto scheduled air refueling service";

    MissionStartObservable missionStartObservable;

    private Parent gui;

    private final FxWeaver fxWeaver;

    private final ParkingInfoService parkingInfoService;


    private final Dispatcher dispatcher;

    public AirRefuelingService(FxWeaver fxWeaver, ParkingInfoService parkingInfoService, Dispatcher dispatcher) {
        this.fxWeaver = fxWeaver;
        this.parkingInfoService = parkingInfoService;
        this.dispatcher = dispatcher;
    }

    @Override
    public void register() {

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
        return desc;
    }

    @Override
    public Image getIcon() {
        return new Image(getClass().getResourceAsStream("/aarservice_icon.png"));
    }

    @Override
    public Parent getPluginGui() throws IOException {
        if(gui == null) {
            ResourceBundle resourceBundle =
                    ResourceBundle.getBundle("AirRefuelingServiceControlPanel", new UTF8Control());
            gui = fxWeaver.loadView(AirRefuelingServiceControlPanel.class, resourceBundle);
        }
        return gui;
    }
}
