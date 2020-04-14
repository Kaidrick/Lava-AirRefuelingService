package moe.ofs.addon.aarservice.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import moe.ofs.addon.aarservice.domains.BriefedWaypoint;
import moe.ofs.addon.aarservice.domains.Route;
import moe.ofs.addon.aarservice.gui.cells.BriefedWaypointCell;
import moe.ofs.addon.navdata.domain.NavFix;
import moe.ofs.addon.navdata.services.NavFixSearchService;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@FxmlView
@Controller
public class RouteCreationDialog implements Initializable {

    @FXML
    private TextField routeNameTextField;

    @FXML
    private ListView<BriefedWaypoint> briefedWaypointListView;

    @FXML
    private ListView<NavFix> searchResultListView;

    @FXML
    private TextField searchIdentTextField;

    @FXML
    private TextField assignedAltitudeTextField;

    private Route route;

    private final NavFixSearchService service;

    public RouteCreationDialog(NavFixSearchService service) {
        this.service = service;
    }

    public Route getResult() {
        return route;
    }


    @FXML
    private void generateRoute() {

        if(!routeNameTextField.getText().equals("") && briefedWaypointListView.getItems().size() != 0) {
            Route route = new Route();
            route.setName(routeNameTextField.getText());
            route.getBriefedWaypoints().addAll(briefedWaypointListView.getItems());

            this.route = route;
        }
    }

    @FXML
    private void resetIdentSearchAndAltitude() {
        searchIdentTextField.clear();
        assignedAltitudeTextField.clear();
    }

    @FXML
    private void addToBriefedWaypointListView() {
        // get current selection of the combobox along with altitude
        // create new briefedwaypoint instance and added to list view
        NavFix navFix = searchResultListView.getSelectionModel().getSelectedItem();
        String altitudeString = assignedAltitudeTextField.getText();

        if(navFix != null && !altitudeString.equals("")) {
            BriefedWaypoint waypoint = new BriefedWaypoint();
            waypoint.setNavFix(navFix);
            waypoint.setAssignedAltitude(Double.parseDouble(altitudeString));

            briefedWaypointListView.getItems().add(waypoint);
        }
    }

    @FXML
    private void deleteFromBriefedWaypointListView() {
        briefedWaypointListView.getItems()
                .remove(briefedWaypointListView.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void cleanAllBriefedWaypointListView() {
        briefedWaypointListView.getItems().clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        briefedWaypointListView.setCellFactory(c -> new BriefedWaypointCell(briefedWaypointListView));

        searchIdentTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue.equals("")) {
                searchResultListView.getItems().clear();

                service.findAnyByCode(newValue)
                        .forEach(searchResultListView.getItems()::add);
            } else {
                searchResultListView.getItems().clear();
            }
        }));
    }
}
