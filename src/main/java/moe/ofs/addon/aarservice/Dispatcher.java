package moe.ofs.addon.aarservice;

import moe.ofs.addon.navdata.services.NavaidService;
import moe.ofs.addon.navdata.services.WaypointService;
import org.springframework.stereotype.Component;

@Component
public class Dispatcher {

    private final NavaidService navaidService;
    private final WaypointService waypointService;

    public Dispatcher(NavaidService navaidService, WaypointService waypointService) {
        this.navaidService = navaidService;
        this.waypointService = waypointService;
    }

    // spawn aircraft based on theater name and predefined airport and routes

    public void test() {
        System.out.println("this is a test to dispatcher class bean");
    }
}
