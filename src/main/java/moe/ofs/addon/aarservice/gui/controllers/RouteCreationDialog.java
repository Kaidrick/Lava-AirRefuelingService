package moe.ofs.addon.aarservice.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import moe.ofs.addon.aarservice.domains.BriefedWaypoint;
import moe.ofs.addon.aarservice.domains.Route;
import moe.ofs.addon.aarservice.gui.cells.BriefedWaypointCell;
import moe.ofs.addon.navdata.domain.NavFix;
import moe.ofs.addon.navdata.services.NavFixSearchService;
import moe.ofs.backend.function.unitconversion.Lengths;
import moe.ofs.backend.function.unitconversion.Speeds;
import moe.ofs.backend.object.unitofmeasure.Length;
import moe.ofs.backend.object.unitofmeasure.Speed;
import moe.ofs.backend.object.unitofmeasure.SystemOfMeasurement;
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

    @FXML
    private TextField assignedSpeedTextField;

    @FXML
    private ComboBox<SystemOfMeasurement> systemOfMeasurementComboBox;

    private Route route;

    private final NavFixSearchService service;

    public RouteCreationDialog(NavFixSearchService service) {
        this.service = service;
    }

    public Route getResult() {
        return route;
    }


    @FXML
    private void generateRoute(ActionEvent actionEvent) {

        if(!routeNameTextField.getText().equals("") && briefedWaypointListView.getItems().size() != 0) {
            Route route = new Route();
            route.setName(routeNameTextField.getText());
            route.getBriefedWaypoints().addAll(briefedWaypointListView.getItems());

            this.route = route;

            Node  source = (Node) actionEvent.getSource();
            Stage stage  = (Stage) source.getScene().getWindow();
            stage.close();
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
        String speedString = assignedSpeedTextField.getText();

        if(navFix != null && !altitudeString.equals("") && !speedString.equals("")) {
            BriefedWaypoint waypoint = new BriefedWaypoint();
            waypoint.setNavFix(navFix);

            if(systemOfMeasurementComboBox.getSelectionModel().getSelectedItem()
                    .equals(SystemOfMeasurement.IMPERIAL)) {
                waypoint.setAssignedAltitude(Lengths.feetToMeters(Double.parseDouble(altitudeString)));
                waypoint.setAssignedSpeed(Speeds.knotsToMetersPerSeconds(Double.parseDouble(speedString)));
            } else {
                waypoint.setAssignedAltitude(Double.parseDouble(altitudeString));
                waypoint.setAssignedSpeed(Speeds.kilometersPerHourToMetersPerSecond(Double.parseDouble(speedString)));
            }

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
        route = null;

        systemOfMeasurementComboBox.getItems().addAll(SystemOfMeasurement.IMPERIAL, SystemOfMeasurement.METRIC);
        systemOfMeasurementComboBox.getSelectionModel().select(SystemOfMeasurement.IMPERIAL);
        SystemOfMeasurement system = systemOfMeasurementComboBox.getSelectionModel().getSelectedItem();

        if(system.equals(SystemOfMeasurement.IMPERIAL)) {
            assignedAltitudeTextField.setPromptText(Length.FEET.toString());
            assignedSpeedTextField.setPromptText(Speed.KNOTS.toString());
        } else {
            assignedAltitudeTextField.setPromptText(Length.METERS.toString());
            assignedSpeedTextField.setPromptText(Speed.KILOMETERS_PER_HOUR.toString());
        }

        systemOfMeasurementComboBox.getSelectionModel().selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> {
                    assignedAltitudeTextField.setPromptText(Lengths.of(newValue).toString());
                    assignedSpeedTextField.setPromptText(Speeds.of(newValue).toString());
                }));

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
