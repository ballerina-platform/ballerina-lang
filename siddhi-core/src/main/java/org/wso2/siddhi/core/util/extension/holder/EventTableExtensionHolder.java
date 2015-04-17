package org.wso2.siddhi.core.util.extension.holder;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.table.EventTable;

public class EventTableExtensionHolder extends AbstractExtensionHolder {

    private static EventTableExtensionHolder instance;

    protected EventTableExtensionHolder(ExecutionPlanContext executionPlanContext) {
        super(EventTable.class, executionPlanContext);
    }

    public static EventTableExtensionHolder getInstance(ExecutionPlanContext executionPlanContext) {
        if (instance == null) {
            instance = new EventTableExtensionHolder(executionPlanContext);
        }
        return instance;
    }
}
