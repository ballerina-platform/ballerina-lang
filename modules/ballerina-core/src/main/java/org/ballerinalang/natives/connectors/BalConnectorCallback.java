/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.natives.connectors;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.DefaultBalCallback;
import org.ballerinalang.runtime.threadpool.ResponseWorkerThread;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * A class used by connectors to receive response from external system and correlate request context with response.
 */
public class BalConnectorCallback extends DefaultBalCallback {

    private Context context;

    private boolean responseArrived = false;

    private BValue valueRef;

    private boolean nonBlockingExecution;

    // Reference for post validation.
    private AbstractNativeAction nativeAction;

    public BalConnectorCallback(Context context) {
        super(context.getBalCallback());
        this.context = context;
    }

    public boolean isResponseArrived() {
        return responseArrived;
    }

    public BValue getValueRef() {
        return valueRef;
    }

    public void setNonBlockingExecution(boolean nonBlockingExecution) {
        this.nonBlockingExecution = nonBlockingExecution;
    }

    public AbstractNativeAction getNativeAction() {
        return nativeAction;
    }

    public void setNativeAction(AbstractNativeAction nativeAction) {
        this.nativeAction = nativeAction;
    }

    @Override
    public void done(CarbonMessage carbonMessage) {
        BMessage bMessage = new BMessage(carbonMessage);
        valueRef = bMessage;
        context.getControlStackNew().currentFrame.returnValues[0] = valueRef;
        responseArrived = true;

        // Release Thread.
        if (nonBlockingExecution) {
            ThreadPoolFactory.getInstance().getExecutor().execute(new ResponseWorkerThread(carbonMessage, this));
        } else {
            synchronized (context) {
                context.notifyAll();
            }
        }
    }

    public Context getContext() {
        return context;
    }

}
