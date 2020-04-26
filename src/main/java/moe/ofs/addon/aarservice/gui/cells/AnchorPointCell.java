package moe.ofs.addon.aarservice.gui.cells;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import moe.ofs.addon.aarservice.domains.BriefedWaypoint;
import moe.ofs.backend.function.unitconversion.Lengths;
import moe.ofs.backend.function.unitconversion.Speeds;

public class AnchorPointCell extends ListCell<BriefedWaypoint> {

    public AnchorPointCell(ListView<BriefedWaypoint> listView) {

    }

    @Override
    protected void updateItem(BriefedWaypoint item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        setGraphic(null);

        if(item !=null && !empty) {
            // convert to standard unit
            double speed = Speeds.metersPerSecondToKnots(item.getAssignedSpeed());
            double altitude = Lengths.metersToFeet(item.getAssignedAltitude());

            setText(String.format("%s - %.0f kts / %.0f ft",
                    item.getNavFix().getCode(), speed, altitude));
        }
    }
}
