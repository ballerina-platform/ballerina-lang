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
package org.ballerinalang.bre;

import org.ballerinalang.bre.nonblocking.BLangExecutionVisitor;
import org.ballerinalang.runtime.BalCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code Context} represents the runtime state of a program.
 *
 * @since 0.8.0
 */
public class Context {

    //TODO: Rename this into BContext and move this to runtime package
    private ControlStack controlStack;
    private CarbonMessage cMsg;
    private BalCallback balCallback;
    protected Map<String, Object> properties = new HashMap();
    private CallableUnitInfo serviceInfo;
    private BLangExecutionVisitor executor;
    private Object serverConnectorProtocol;

    public Context() {
        this.controlStack = new ControlStack();
    }

    public Context(CarbonMessage cMsg) {
        this.cMsg = cMsg;
        this.controlStack = new ControlStack();
    }

    public ControlStack getControlStack() {
        return this.controlStack;
    }

    public CarbonMessage getCarbonMessage() {
        return this.cMsg;
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public BalCallback getBalCallback() {
        return this.balCallback;
    }

    public void setBalCallback(BalCallback balCallback) {
        this.balCallback = balCallback;
    }
    
    public CallableUnitInfo getServiceInfo() {
        return this.serviceInfo;
    }

    public void setServiceInfo(CallableUnitInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public void setExecutor(BLangExecutionVisitor executor) {
        this.executor = executor;
    }

    public BLangExecutionVisitor getExecutor() {
        return executor;
    }

    public Object getServerConnectorProtocol() {
        return serverConnectorProtocol;
    }

    public void setServerConnectorProtocol(Object serverConnectorProtocol) {
        this.serverConnectorProtocol = serverConnectorProtocol;
    }
}
