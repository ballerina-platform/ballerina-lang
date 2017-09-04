package org.ballerinalang.connector.api;

import org.ballerinalang.connector.impl.BConnectorFuture;
import org.ballerinalang.model.values.BValue;

/**
 * Created by rajith on 9/4/17.
 */
public class ResourceExecutor {

    public static ConnectorFuture execute(Resource resource, BValue... values) {
        return new BConnectorFuture();
    }

}
