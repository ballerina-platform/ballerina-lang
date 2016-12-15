package org.wso2.ballerina.core.nativeimpl.connectors;


import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.Interpreter;

import org.wso2.ballerina.core.model.values.BValue;


/**
 * Represents Native Ballerina Action.
 */
public abstract class AbstractNativeAction implements Interpreter {
    private NativeConnector connector;
//   Add other attributes

    public AbstractNativeAction(NativeConnector connector) {
        this.connector = connector;
    }

    @Override
    public void interpret(Context ctx) {
        connector.init();
    }

    public abstract BValue[] execute(Context context);

}
