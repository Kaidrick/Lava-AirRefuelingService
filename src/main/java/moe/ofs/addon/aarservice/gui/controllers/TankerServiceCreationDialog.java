package moe.ofs.addon.aarservice.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import moe.ofs.addon.aarservice.domains.*;
import moe.ofs.addon.aarservice.services.RouteService;
import moe.ofs.backend.function.unitconversion.Lengths;
import moe.ofs.backend.object.tasks.TacanModeChannel;
import moe.ofs.backend.object.tasks.main.OrbitPattern;
import moe.ofs.backend.object.unitofmeasure.Length;
import moe.ofs.backend.services.ParkingInfoService;
import net.rgielen.fxweaver.core.FxmlView;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@FxmlView
@Controller
public class TankerServiceCreationDialog implements Initializable {

    @FXML
    private TextField tankerMissionNameTextField;

    @FXML
    private ComboBox<String> tankerTypeComboBox;

    @FXML
    private ComboBox<String> initialAirdromeComboBox;

    @FXML
    private ComboBox<String> tankerCallsignComboBox;

    @FXML
    private TextField tankerRadioFrequencyTextField;

    @FXML
    private TextField tankerBeaconChannelTextField;

    @FXML
    private ComboBox<TacanModeChannel> tankerBeaconChannelModeComboBox;

    @FXML
    private TextField beaconMorseCodeTextField;

    @FXML
    private CheckBox beaconBearingAvailabilityCheckBox;

    @FXML
    private ToggleSwitch customOrbitPatternToggleSwitch;

    @FXML
    private ListView<Route> routeListView;

    @FXML
    private ListView<BriefedWaypoint> anchorPointListView;

    @FXML
    private ComboBox<OrbitPattern> patternComboBox;

    @FXML
    private HBox patternDataHBox;

    @FXML
    private Label patternInboundLabel;

    @FXML
    private Label patternOutboundLabel;

    @FXML
    private Slider patternHeadingSlider;

    @FXML
    private TextField tankerRtbFuelPercentTextField;

    @FXML
    private TextField tankerPatternAltitudeTextField;

    @FXML
    private TextField tankerPatternLegLengthTextField;

    @FXML
    private ComboBox<Length> patternAltitudeUnitComboBox;

    @FXML
    private ComboBox<Length> patternLegLengthUnitComboBox;


    private final RouteService routeService;
    private final ParkingInfoService parkingInfoService;

    private TankerService tankerService;

    Map<String, Integer> airdromeMap = new HashMap<>();

    public TankerServiceCreationDialog(RouteService routeService, ParkingInfoService parkingInfoService) {
        this.routeService = routeService;
        this.parkingInfoService = parkingInfoService;
    }

    public TankerService getResult() {
        return tankerService;
    }

    @FXML
    private void generateResult(ActionEvent actionEvent) {
        // get tanker mission identification
        String tankerMissionName = tankerMissionNameTextField.getText();

        // get tanker type
        String tankerType = tankerTypeComboBox.getSelectionModel().getSelectedItem();
        // get init airdrome
        int airdromeId = airdromeMap.get(initialAirdromeComboBox.getSelectionModel().getSelectedItem());
        // get route and convert to dcs route
        Route route = routeListView.getSelectionModel().getSelectedItem();

        BriefedWaypoint holdingFix = anchorPointListView.getSelectionModel().getSelectedItem();

        if(!tankerMissionName.equals("") && route != null && holdingFix != null) {
            // return tanker service instance
            tankerService = new TankerService(route, tankerType, airdromeId, holdingFix);

            tankerService.setTankerMissionName(tankerMissionName);

            Map<Object, Object> callsign = new HashMap<>();
            callsign.put(1, 1);
            callsign.put(2, 1);
            callsign.put(3, 1);
            callsign.put("name", "Texaco11");  // TODO --> read from combo box

            Comm comm = Comm.builder()
                    .callsign(callsign)
                    .beaconMorseCode(beaconMorseCodeTextField.getText())
                    .bearingInfoAvailable(beaconBearingAvailabilityCheckBox.isSelected())
                    .channel(Integer.parseInt(tankerBeaconChannelTextField.getText()))
                    .modeChannel(tankerBeaconChannelModeComboBox.getSelectionModel().getSelectedItem())
                    .frequency((long) (Double.parseDouble(tankerRadioFrequencyTextField.getText()) * 1E6))
                    .build();

            tankerService.setComm(comm);

            if(customOrbitPatternToggleSwitch.isSelected()) {
                if(!tankerPatternAltitudeTextField.getText().isEmpty() &&
                        !tankerPatternLegLengthTextField.getText().isEmpty()) {
                    CustomPattern pattern = new CustomPattern();

                    // check system of measurement
                    if(patternAltitudeUnitComboBox.getValue() == Length.FEET) {
                        pattern.setPatternAltitude(Lengths.feetToMeters(
                                Double.parseDouble(tankerPatternAltitudeTextField.getText())));
                    } else {
                        pattern.setPatternAltitude(Double.parseDouble(tankerPatternAltitudeTextField.getText()));
                    }

                    if(patternLegLengthUnitComboBox.getValue() == Length.NAUTICAL_MILES) {
                        pattern.setPatternLegLength(
                                Lengths.nauticalMilesToMeters(
                                        Double.parseDouble(tankerPatternLegLengthTextField.getText())));
                    } else {
                        pattern.setPatternLegLength(
                                Lengths.kilometersToMeters(
                                        Double.parseDouble(tankerPatternLegLengthTextField.getText())));
                    }

                    pattern.setPatternInbound(patternHeadingSlider.getValue());

                    tankerService.setCustomPattern(pattern);

                    System.out.println("pattern = " + pattern);
                }
            }

            Node source = (Node) actionEvent.getSource();
            Stage stage  = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        tankerBeaconChannelModeComboBox.getItems().addAll(TacanModeChannel.values());

        patternComboBox.getItems().addAll(OrbitPattern.values());

        patternAltitudeUnitComboBox.getItems().addAll(Length.FEET, Length.METERS);
        patternLegLengthUnitComboBox.getItems().addAll(Length.NAUTICAL_MILES, Length.KILOMETERS);

        customOrbitPatternToggleSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                patternDataHBox.setVisible(true);
            } else {
                patternDataHBox.setVisible(false);
            }
        });
        patternDataHBox.setVisible(false);

        patternHeadingSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            int inbound = newValue.intValue();
            int outbound = 360 - inbound;

            patternInboundLabel.setText(String.format("%03d", inbound));
            patternOutboundLabel.setText(String.format("%03d", outbound));
        }));




        routeListView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if(newValue != null) {
                anchorPointListView.getItems().clear();
                anchorPointListView.getItems().addAll(newValue.getBriefedWaypoints());
            }
        }));

        tankerService = null;

        routeListView.getItems().addAll(routeService.findAll());


        // TODO --> pull type from defined data? check unit.lua definition?
        tankerTypeComboBox.getItems().add("KC-135");
        tankerTypeComboBox.getItems().add("KC-130");


        parkingInfoService.getAllParking()
                .forEach(parkingInfo ->
                        airdromeMap.putIfAbsent(parkingInfo.getAirdromeName(), parkingInfo.getAirdromeId()));

        initialAirdromeComboBox.getItems().clear();
        initialAirdromeComboBox.getItems().addAll(airdromeMap.keySet());
    }
}
