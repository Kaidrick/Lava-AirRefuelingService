package moe.ofs.addon.aarservice.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import moe.ofs.addon.aarservice.AirRefuelingService;
import moe.ofs.addon.aarservice.Dispatcher;
import moe.ofs.addon.aarservice.domains.DispatchedTanker;
import moe.ofs.addon.aarservice.domains.Route;
import moe.ofs.addon.aarservice.domains.TankerService;
import moe.ofs.addon.aarservice.gui.cells.RefuelingServiceCellFactory;
import moe.ofs.addon.aarservice.services.RefuelingServiceService;
import moe.ofs.addon.aarservice.services.RouteService;
import moe.ofs.backend.Configurable;
import moe.ofs.backend.UTF8Control;
import moe.ofs.backend.interaction.StageControl;
import moe.ofs.backend.services.MissionDataService;
import moe.ofs.backend.util.I18n;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;

@Slf4j
@FxmlView
@Controller
public class AirRefuelingServiceControlPanel implements Initializable, Configurable {

    @FXML
    private AnchorPane mainControlPanelContainer;

    @FXML
    private Button addRouteButton;

    @FXML
    private Button editRouteButton;

    @FXML
    private Button deleteRouteButton;

    @FXML
    private ToggleSwitch autoDispatchToggleSwitch;

    @FXML
    private ListView<Route> routeListView;

    @FXML
    private ListView<TankerService> tankerServiceListView;

    private final FxWeaver fxWeaver;
    private final AirRefuelingService airRefuelingService;

    private final RouteService routeService;
    private final RefuelingServiceService refuelingServiceService;
    private final MissionDataService<DispatchedTanker> dispatchedTankerMissionDataService;

    private final RefuelingServiceCellFactory refuelingServiceCellFactory;

    private final Dispatcher dispatcher;

    // update on new stage
    private RouteCreationDialog routeCreationDialog;
    private TankerServiceCreationDialog tankerServiceCreationDialog;

    public AirRefuelingServiceControlPanel(FxWeaver fxWeaver, AirRefuelingService airRefuelingService,
                                           RouteService routeService,
                                           RefuelingServiceService refuelingServiceService,
                                           MissionDataService<DispatchedTanker> dispatchedTankerMissionDataService,
                                           RefuelingServiceCellFactory refuelingServiceCellFactory, Dispatcher dispatcher) {
        this.fxWeaver = fxWeaver;
        this.airRefuelingService = airRefuelingService;

        this.routeService = routeService;
        this.refuelingServiceService = refuelingServiceService;
        this.dispatchedTankerMissionDataService = dispatchedTankerMissionDataService;
        this.refuelingServiceCellFactory = refuelingServiceCellFactory;
        this.dispatcher = dispatcher;
    }

    @FXML
    private void addTankerRoute(ActionEvent actionEvent) {
        // open a dialog
        Node source = (Node) actionEvent.getSource();
        Stage parent  = (Stage) source.getScene().getWindow();

        FxControllerAndView<RouteCreationDialog, AnchorPane> fxControllerAndView =
                fxWeaver.load(RouteCreationDialog.class,
                        ResourceBundle.getBundle("AirRefuelingServiceControlPanel"));

        routeCreationDialog = fxControllerAndView.getController();

        Scene scene = new Scene(fxControllerAndView.getView().orElseThrow(RuntimeException::new));
        Stage stage = new Stage();

        stage.getIcons().add(airRefuelingService.getIcon());

        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(scene);

        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        StageControl.showOnParentCenterAndWait(stage, parent);
        // show and wait for result
        Route route = routeCreationDialog.getResult();
        if(route != null) {
            // save to service
            routeService.save(route);
            routeListView.getItems().add(route);
        }


        HashSet<Route> routeSet = (HashSet<Route>) routeService.findAll();
        writeFile(routeSet, "aar_service_routes");
    }

    @FXML
    private void editTankerRoute(ActionEvent actionEvent) {

    }

    @FXML
    private void deleteTankerRoute(ActionEvent actionEvent) {
        Route route = routeListView.getSelectionModel().getSelectedItem();
        if(route != null) {
            routeService.delete(route);
            routeListView.getItems().remove(routeListView.getSelectionModel().getSelectedItem());


            HashSet<Route> routeSet = (HashSet<Route>) routeService.findAll();
            writeFile(routeSet, "aar_service_routes");
        }
    }

    @FXML
    private void addRefuelingService(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage parent  = (Stage) source.getScene().getWindow();

        FxControllerAndView<TankerServiceCreationDialog, AnchorPane> fxControllerAndView =
                fxWeaver.load(TankerServiceCreationDialog.class,
                        ResourceBundle.getBundle("AirRefuelingServiceControlPanel"));

        tankerServiceCreationDialog = fxControllerAndView.getController();

        Scene scene = new Scene(fxControllerAndView.getView().orElseThrow(RuntimeException::new));
        Stage stage = new Stage();

        stage.getIcons().add(airRefuelingService.getIcon());

        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(scene);

        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        StageControl.showOnParentCenterAndWait(stage, parent);

        TankerService tankerService = tankerServiceCreationDialog.getResult();
        if(tankerService != null) {
            refuelingServiceService.save(tankerService);
            tankerServiceListView.getItems().add(tankerService);

            HashSet<TankerService> serviceSet =
                    (HashSet<TankerService>) refuelingServiceService.findAll();
            writeFile(serviceSet, "aar_service_entries");
        }

        // TODO --> save tanker service by theater to file
        // TODO --> auto start dispatch
    }

    @FXML
    private void editRefuleingService(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage parent  = (Stage) source.getScene().getWindow();

        FxControllerAndView<TankerServiceCreationDialog, AnchorPane> fxControllerAndView =
                fxWeaver.load(TankerServiceCreationDialog.class,
                        ResourceBundle.getBundle("AirRefuelingServiceControlPanel"));

        tankerServiceCreationDialog = fxControllerAndView.getController();

        Scene scene = new Scene(fxControllerAndView.getView().orElseThrow(RuntimeException::new));
        Stage stage = new Stage();

        stage.getIcons().add(airRefuelingService.getIcon());

        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(scene);

        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);

        TankerService selectedItem = tankerServiceListView.getSelectionModel().getSelectedItem();
        if(selectedItem == null)
            return;

        tankerServiceCreationDialog.loadData(selectedItem);

        StageControl.showOnParentCenterAndWait(stage, parent);

        TankerService tankerService = tankerServiceCreationDialog.getResult();
        if(tankerService != null) {
            refuelingServiceService.save(tankerService);

            System.out.println("refuelingServiceService.findAll() = " + refuelingServiceService.findAll());

            HashSet<TankerService> serviceSet =
                    (HashSet<TankerService>) refuelingServiceService.findAll();
            writeFile(serviceSet, "aar_service_entries");
        }
    }

    @FXML
    private void removeRefuelingService(ActionEvent actionEvent) {
        TankerService service = tankerServiceListView.getSelectionModel().getSelectedItem();

        refuelingServiceService.delete(service);
        tankerServiceListView.getItems().remove(service);

        System.out.println("refuelingServiceService = " + refuelingServiceService.findAll());

        HashSet<TankerService> serviceSet =
                (HashSet<TankerService>) refuelingServiceService.findAll();
        writeFile(serviceSet, "aar_service_entries");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tankerServiceListView.setCellFactory(lv -> refuelingServiceCellFactory.listView(lv).getObject());

        // initialize list views
        routeListView.getItems().addAll(routeService.findAll());
        tankerServiceListView.getItems().addAll(refuelingServiceService.findAll());


        // set auto dispatch toggle switch position
        if(xmlConfigExists("Refueling Service")) {
            // set auto dispatch toggle switch position
            boolean autoDispatch =
                    Boolean.parseBoolean(readConfiguration("Refueling Service", "auto_dispatch"));

            autoDispatchToggleSwitch.selectedProperty().set(autoDispatch);
        }

        // auto dispatch toggle switch save config
        autoDispatchToggleSwitch.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            writeConfiguration("Refueling Service", "auto_dispatch", newValue.toString());

            // immediately start dispatch if new value is true
            if(newValue) {
                refuelingServiceService.findAll().forEach(dispatcher::dispatch);
            }
        }));

        I18n.localeProperty().addListener(((observable, oldValue, newValue) -> {
            ResourceBundle bundle =
                    ResourceBundle.getBundle("AirRefuelingServiceControlPanel", newValue, new UTF8Control());

            log.info("call pane container for " + bundle.getLocale());

            I18n.toPaneOrNotToPane(mainControlPanelContainer, bundle);
        }));
    }

    @Override
    public String getName() {
        return "air_refueling_service";
    }
}
