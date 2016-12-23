/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.interpreter;

import org.wso2.ballerina.core.nativeimpl.connectors.BalConnectorCallback;
import org.wso2.ballerina.core.runtime.BalCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code Context} represents the runtime state of a program.
 *
 * @since 1.0.0
 */
public class Context {

    //TODO: Rename this into BContext and move this to runtime package

    private ControlStack controlStack;
    private CarbonMessage cMsg;
    private BalCallback responseSendingCallback;

    private BalConnectorCallback connectorCallback;


    protected Map<String, Object> properties = new HashMap();

    public Context() {
        controlStack = new ControlStack();
    }

    public Context(CarbonMessage cMsg) {
        this.cMsg = cMsg;
        controlStack = new ControlStack();
    }

    public ControlStack getControlStack() {
        return controlStack;
    }

    public CarbonMessage getCarbonMessage() {
        return cMsg;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public BalCallback getResponseSendingCallback() {
        return responseSendingCallback;
    }

    public void setResponseSendingCallback(BalCallback responseSendingCallback) {
        this.responseSendingCallback = responseSendingCallback;
    }

    public BalConnectorCallback getConnectorCallback() {
        return connectorCallback;
    }

    public void setConnectorCallback(BalConnectorCallback connectorCallback) {
        this.connectorCallback = connectorCallback;
    }
}
