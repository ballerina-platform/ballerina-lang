/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.builder.components;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean class of client struct object.
 */
public class ClientStruct {
    private String packageName;
    private List<EndPoint> nonBlockingEndpoint = new ArrayList<>();
    private List<EndPoint> blockingEndpoint = new ArrayList<>();
    private List<MessageListener> messageListener = new ArrayList<>();
    
    public ClientStruct(boolean isNonBlockingEP, boolean isBlockingEP, String serviceName, String packageName) {
        this.packageName = packageName;
        if (isNonBlockingEP) {
            nonBlockingEndpoint.add(new EndPoint(serviceName));
            messageListener.add(new MessageListener(serviceName));
        }
        if (isBlockingEP) {
            blockingEndpoint.add(new EndPoint(serviceName));
        }
    }
    
    public List<EndPoint> getNonBlockingEndpoint() {
        return nonBlockingEndpoint;
    }
    
    public void setNonBlockingEndpoint(List<EndPoint> nonBlockingEndpoint) {
        this.nonBlockingEndpoint = nonBlockingEndpoint;
    }
    
    public List<EndPoint> getBlockingEndpoint() {
        return blockingEndpoint;
    }
    
    public void setBlockingEndpoint(List<EndPoint> blockingEndpoint) {
        this.blockingEndpoint = blockingEndpoint;
    }
    
    public List<MessageListener> getMessageListener() {
        return messageListener;
    }
    
    public void setMessageListener(List<MessageListener> messageListener) {
        this.messageListener = messageListener;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
