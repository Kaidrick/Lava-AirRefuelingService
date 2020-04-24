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
import moe.ofs.addon.aarservice.AirRefuelingService;
import moe.ofs.addon.aarservice.Dispatcher;
import moe.ofs.addon.aarservice.domains.DispatchedTanker;
import moe.ofs.addon.aarservice.domains.Route;
import moe.ofs.addon.aarservice.domains.TankerService;
import moe.ofs.addon.aarservice.gui.cells.RefuelingServiceCellFactory;
import moe.ofs.addon.aarservice.services.RefuelingServiceService;
import moe.ofs.addon.aarservice.services.RouteService;
import moe.ofs.backend.Configurable;
import moe.ofs.backend.interaction.StageControl;
import moe.ofs.backend.services.MissionDataService;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

@FxmlView
@Controller
public class AirRefuelingServiceControlPanel implements Initializable, Configurable {

    @FXML
    private Button addRouteButton;

    @FXML
    private Button editRouteButton;

    @FXML
    private Button deleteRouteButton;

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
                fxWeaver.load(RouteCreationDialog.class);

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
                fxWeaver.load(TankerServiceCreationDialog.class);

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
    private void removeRefuelingService(ActionEvent actionEvent) {
        TankerService service = tankerServiceListView.getSelectionModel().getSelectedItem();

        refuelingServiceService.delete(service);
        tankerServiceListView.getItems().remove(service);

        HashSet<TankerService> serviceSet =
                (HashSet<TankerService>) refuelingServiceService.findAll();
        writeFile(serviceSet, "aar_service_entries");
    }

    @FXML
    private void testSpawnTanker() {
        TankerService tankerService = tankerServiceListView.getSelectionModel().getSelectedItem();

        if(tankerService == null)
            return;

        // check if this tanker is already in game
        Optional<DispatchedTanker> optional =
                dispatchedTankerMissionDataService.findBy("name",
                tankerService.getTankerMissionName(), DispatchedTanker.class);

        if(optional.isPresent()) {
            System.out.println("tanker already exists in mission");

            // only start tracking runtime id but do not respawn tanker unit
            // what needs to be added to start tracking?
            // runtime id and holding fix

            // construct tanker service anyway
            dispatcher.buildMissionGroup(tankerService);
            dispatcher.resumeDispatch(tankerService, optional.get());
            System.out.println("resume dispatch info for " +
                    tankerService.getTankerMissionName());

        } else {
            System.out.println("dispatch new tanker");
            dispatcher.dispatch(tankerService);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tankerServiceListView.setCellFactory(lv -> refuelingServiceCellFactory.listView(lv).getObject());

        if(dataFileExists("aar_service_routes")) {
            Set<Route> routeSet = readFile("aar_service_routes");
            routeSet.forEach(routeService::save);
            routeListView.getItems().addAll(routeSet);
        }

        if(dataFileExists("aar_service_entries")) {
            Set<TankerService> serviceSet = readFile("aar_service_entries");
            serviceSet.forEach(refuelingServiceService::save);
            tankerServiceListView.getItems().addAll(serviceSet);
        }
    }

    @Override
    public String getName() {
        return "air_refueling_service";
    }
}
