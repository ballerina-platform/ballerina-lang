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

package org.wso2.ballerina.core.nativeimpl.connectors;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.MessageValue;
import org.wso2.ballerina.core.runtime.core.DefaultBalCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * A class used by connectors to receive response from external system and correlate request context with response
 */
public class BalConnectorCallback extends DefaultBalCallback {

    private Context context;

    public boolean responseArrvied = false;

    public BValueRef valueRef;

    public BalConnectorCallback(Context context) {
        super(context.getBalCallback());
        this.context = context;
    }

    @Override
    public void done(CarbonMessage carbonMessage) {
        MessageValue messageValue = new MessageValue(carbonMessage);
         valueRef = new BValueRef(messageValue);
        context.getControlStack().setValue(4, valueRef);
        responseArrvied = true;
    }

}
