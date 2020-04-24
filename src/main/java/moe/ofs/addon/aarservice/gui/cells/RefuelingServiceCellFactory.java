package moe.ofs.addon.aarservice.gui.cells;

import javafx.scene.control.ListView;
import moe.ofs.addon.aarservice.Dispatcher;
import moe.ofs.addon.aarservice.domains.TankerService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;


@Component
public class RefuelingServiceCellFactory implements FactoryBean<RefuelingServiceCell> {

    private final Dispatcher dispatcher;

    private ListView<TankerService> listView;

    public RefuelingServiceCellFactory(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public RefuelingServiceCellFactory listView(ListView<TankerService> listView) {
        this.listView = listView;
        return this;
    }

    @Override
    public RefuelingServiceCell getObject() {
        return new RefuelingServiceCell(listView, dispatcher);
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
