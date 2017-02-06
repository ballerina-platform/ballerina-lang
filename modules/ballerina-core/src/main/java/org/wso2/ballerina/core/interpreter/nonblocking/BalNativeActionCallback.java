/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 */

package org.wso2.ballerina.core.interpreter.nonblocking;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.LinkedNodeExecutor;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.InvokeNativeActionNode;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.runtime.DefaultBalCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * A class used by Native Action to receive response from external system and correlate request context with response.
 */
public class BalNativeActionCallback extends DefaultBalCallback {

    private LinkedNodeExecutor executor;

    private Context context;

    private InvokeNativeActionNode current;

    private BValue valueRef;

    public BalNativeActionCallback(Context context, LinkedNodeExecutor executor, InvokeNativeActionNode current) {
        super(context.getBalCallback());
        this.context = context;
        this.executor = executor;
        this.current = current;
    }

    public BValue getValueRef() {
        return valueRef;
    }

    @Override
    public void done(CarbonMessage carbonMessage) {
        BMessage bMessage = new BMessage(carbonMessage);
        valueRef = bMessage;
        //context.getControlStack().setValue(4, valueRef);
        context.getControlStack().setReturnValue(0, valueRef);
        current.getCallableUnit().validate(this);
        current.next.executeLNode(executor);
    }


}
