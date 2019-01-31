package org.ballerinalang.stdlib.task.listener.objects;

import org.ballerinalang.connector.api.Service;
import org.ballerinalang.stdlib.task.utils.TaskRegistry;

import java.util.ArrayList;

import static org.ballerinalang.stdlib.task.utils.TaskIdGenerator.generate;

/**
 * Abstract class which represents a ballerina task.
 */
public abstract class AbstractTask implements Task {

    protected String id = generate();
    protected ArrayList<Service> serviceList;
    protected long noOfRuns, maxRuns;

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        TaskRegistry.getInstance().remove(id);
    }
}
