package moe.ofs.addon.aarservice.gui.cells;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import moe.ofs.addon.aarservice.domains.BriefedWaypoint;

public class BriefedWaypointCell extends ListCell<BriefedWaypoint> {

    public BriefedWaypointCell(ListView<BriefedWaypoint> listView) {
        super();
    }

    @Override
    protected void updateItem(BriefedWaypoint item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        setGraphic(null);

        if(item !=null && !empty) {
            String stringBuilder = item.getNavFix() +
                    " @ " +
                    item.getAssignedAltitude();

            setText(stringBuilder);
        }
    }
}
