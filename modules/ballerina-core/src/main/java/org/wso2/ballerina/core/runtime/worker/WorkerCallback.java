/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.runtime.worker;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.runtime.DefaultBalCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * {@code WorkerCallback} represents a callback which will get executed once the worker replies to the caller
 *
 * @since 0.8.0
 */
public class WorkerCallback extends DefaultBalCallback {

    private Context context;

    private BValue valueRef;

    public WorkerCallback(Context context) {
        super(context.getBalCallback());
        this.context = context;
    }

    public BValue getValueRef() {
        return valueRef;
    }

    @Override
    public void done(CarbonMessage carbonMessage) {
        BMessage bMessage = new BMessage(carbonMessage);
        valueRef = bMessage;
        context.getControlStack().setReturnValue(0, valueRef);
        synchronized (context) {
            context.notifyAll();
        }
    }

}

