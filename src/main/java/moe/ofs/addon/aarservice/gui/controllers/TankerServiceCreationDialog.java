package moe.ofs.addon.aarservice.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import moe.ofs.addon.aarservice.domains.*;
import moe.ofs.addon.aarservice.gui.cells.AnchorPointCell;
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
import java.util.Optional;
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
    private ComboBox<TankerCallsign> tankerCallsignComboBox;

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

    private Map<String, Integer> airdromeMap = new HashMap<>();

    public TankerServiceCreationDialog(RouteService routeService, ParkingInfoService parkingInfoService) {
        this.routeService = routeService;
        this.parkingInfoService = parkingInfoService;
    }

    public TankerService getResult() {
        return tankerService;
    }

    private Optional<String> getAirdromeNameById(int id) {
        return airdromeMap.entrySet().stream()
                .filter(stringIntegerEntry -> stringIntegerEntry.getValue() == id)
                .findAny().map(Map.Entry::getKey);
    }

    public void loadData(TankerService tankerService) {
        tankerMissionNameTextField.setText(tankerService.getTankerMissionName());
        tankerTypeComboBox.getSelectionModel().select(tankerService.getAircraftType());

        Optional<String> airdromeNameOptional = getAirdromeNameById(tankerService.getStartingAirdromeId());
        airdromeNameOptional.ifPresent(s -> initialAirdromeComboBox.getSelectionModel().select(s));

        patternComboBox.getSelectionModel().select(OrbitPattern.RACE_TRACK);

        Comm comm = tankerService.getComm();
        tankerRadioFrequencyTextField.setText(String.valueOf(comm.getFrequency()));
        tankerBeaconChannelTextField.setText(String.valueOf(comm.getChannel()));
        tankerBeaconChannelModeComboBox.getSelectionModel().select(comm.getModeChannel());
        beaconMorseCodeTextField.setText(comm.getBeaconMorseCode());
        beaconBearingAvailabilityCheckBox.setSelected(comm.isBearingInfoAvailable());

        // get route with the same name

        routeListView.getSelectionModel().select(tankerService.getRoute());
        anchorPointListView.getSelectionModel().select(tankerService.getHoldingFix());

        CustomPattern pattern;
        if((pattern = tankerService.getCustomPattern()) != null) {
            customOrbitPatternToggleSwitch.selectedProperty().set(true);

            patternHeadingSlider.setValue(pattern.getPatternInbound());
            tankerPatternAltitudeTextField.setText(String.valueOf(pattern.getPatternAltitude()));
            tankerPatternLegLengthTextField.setText(String.valueOf(pattern.getPatternLegLength()));
        }
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
            callsign.put(1, tankerCallsignComboBox.getValue().getIndex());
            callsign.put(2, 1);
            callsign.put(3, 1);
            callsign.put("name", tankerCallsignComboBox.getValue() + "11");

            Comm comm = Comm.builder()
                    .callsign(callsign)
                    .beaconMorseCode(beaconMorseCodeTextField.getText())
                    .bearingInfoAvailable(beaconBearingAvailabilityCheckBox.isSelected())
                    .channel(Integer.parseInt(tankerBeaconChannelTextField.getText()))
                    .modeChannel(tankerBeaconChannelModeComboBox.getSelectionModel().getSelectedItem())
                    .frequency(Double.parseDouble(tankerRadioFrequencyTextField.getText()))
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

        anchorPointListView.setCellFactory(AnchorPointCell::new);

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
            int outbound = inbound < 180 ? inbound + 180 : inbound - 180;

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

        tankerTypeComboBox.getItems().addAll(
                // NATO
                "KC-135",
                "KC135MPRS",
                "KC130",
                "S-3B Tanker",

                // Russian
                "IL-78M"
        );

        tankerCallsignComboBox.getItems().addAll(TankerCallsign.values());

        parkingInfoService.getAllParking()
                .forEach(parkingInfo ->
                        airdromeMap.putIfAbsent(parkingInfo.getAirdromeName(), parkingInfo.getAirdromeId()));

        initialAirdromeComboBox.getItems().clear();
        initialAirdromeComboBox.getItems().addAll(airdromeMap.keySet());
    }
}
