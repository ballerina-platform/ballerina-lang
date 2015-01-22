package org.wso2.siddhi.core.extension.holder;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.table.EventTable;

public class EventTableExtensionHolder extends AbstractExtensionHolder {
    private static EventTableExtensionHolder instance;

    private EventTableExtensionHolder(SiddhiContext siddhiContext) {
        super(EventTable.class, siddhiContext);
    }

    public static EventTableExtensionHolder getInstance(SiddhiContext siddhiContext) {
        if (instance == null) {
            instance = new EventTableExtensionHolder(siddhiContext);
        }
        return instance;
    }
}
