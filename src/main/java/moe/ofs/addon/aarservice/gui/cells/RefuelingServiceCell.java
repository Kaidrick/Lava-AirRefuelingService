package moe.ofs.addon.aarservice.gui.cells;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import moe.ofs.addon.aarservice.Dispatcher;
import moe.ofs.addon.aarservice.domains.TankerService;

public class RefuelingServiceCell extends ListCell<TankerService> {

    private TankerService service;

    private final Dispatcher dispatcher;

    private HBox mainHBox = new HBox(5);
    private Pane filler = new Pane();

    private Label name = new Label();

    private Button dispatchButton = new Button("Dispatch");
    private Button callbackButton = new Button("RTB");
    private Button terminateButton = new Button("Terminate");

    public RefuelingServiceCell(ListView<TankerService> listView, Dispatcher dispatcher) {
        super();

        this.dispatcher = dispatcher;

        mainHBox.getChildren().addAll(name, filler, dispatchButton, callbackButton, terminateButton);
        HBox.setHgrow(filler, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(TankerService item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        setGraphic(null);

        if(item != null && !empty) {
            name.setText(item.toString());

            dispatchButton.setOnAction(event -> dispatcher.dispatch(item));
            terminateButton.setOnAction(event -> dispatcher.terminateDispatch(item));

            terminateButton.setStyle("-fx-background-color: #be3a42; -fx-text-fill: #ffffff;");

            setGraphic(mainHBox);
        }
    }
}
