package moe.ofs.addon.aarservice.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import moe.ofs.addon.aarservice.AirRefuelingService;
import moe.ofs.addon.aarservice.domains.Route;
import moe.ofs.addon.aarservice.services.RefuelingServiceService;
import moe.ofs.addon.aarservice.services.RouteService;
import moe.ofs.backend.interaction.PluginStage;
import moe.ofs.backend.interaction.StageControl;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

@FxmlView
@Controller
public class AirRefuelingServiceControlPanel implements Initializable {

    @FXML
    private Button addRouteButton;

    @FXML
    private Button editRouteButton;

    @FXML
    private Button deleteRouteButton;

    @FXML
    private ListView<Route> routeListView;


    private final FxWeaver fxWeaver;
    private final AirRefuelingService airRefuelingService;

    private final RouteService routeService;
    private final RefuelingServiceService refuelingServiceService;

    // update on new stage
    private RouteCreationDialog routeCreationDialog;

    public AirRefuelingServiceControlPanel(FxWeaver fxWeaver, AirRefuelingService airRefuelingService, RouteService routeService, RefuelingServiceService refuelingServiceService) {
        this.fxWeaver = fxWeaver;
        this.airRefuelingService = airRefuelingService;

        this.routeService = routeService;
        this.refuelingServiceService = refuelingServiceService;
    }

    @FXML
    private void addTankerRoute(ActionEvent actionEvent) {
        // open a dialog
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
        StageControl.showOnParentCenterAndWait(stage, PluginStage.stageMap.get(airRefuelingService.getName()));
        // show and wait for result
        System.out.println("wait done" + routeCreationDialog.getResult());

        if(routeCreationDialog.getResult() != null)
            routeService.save(routeCreationDialog.getResult());
    }

    @FXML
    private void addRefuelingService(ActionEvent actionEvent) {
        Scene scene = new Scene(fxWeaver.loadView(TankerServiceCreationDialog.class));
        Stage stage = new Stage();

        stage.getIcons().add(airRefuelingService.getIcon());

        JMetro jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(scene);

        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        StageControl.showOnParentCenter(stage, PluginStage.stageMap.get(airRefuelingService.getName()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

}
