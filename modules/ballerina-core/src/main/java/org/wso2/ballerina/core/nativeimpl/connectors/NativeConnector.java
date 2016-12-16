package org.wso2.ballerina.core.nativeimpl.connectors;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * Represents Native Ballerina Connector.
 */
public abstract class NativeConnector implements Connector {
    public abstract boolean init();
    public BValue[] executeAction(Context context) {

        return new BValue[0];
    }
}
