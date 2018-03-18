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
 * .
 */
public class SampleClient {
    private String packageName;
    private List<NonBlockingEndPoint> nonBlockingEndpoint = new ArrayList<>();
    private List<BlockingEndPoint> blockingEndpoint = new ArrayList<>();
    private List<MessageListener> messageListener = new ArrayList<>();
    
    public SampleClient(boolean isNonBlokingEP, boolean isBlockingEP, String serviceName, String packageName) {
        this.packageName = packageName;
        if (isNonBlokingEP) {
            nonBlockingEndpoint.add(new NonBlockingEndPoint(serviceName));
        }
        if (isBlockingEP) {
            blockingEndpoint.add(new BlockingEndPoint(serviceName));
            messageListener.add(new MessageListener(serviceName));
        }
    }
    
    public List<NonBlockingEndPoint> getNonBlockingEndpoint() {
        return nonBlockingEndpoint;
    }
    
    public void setNonBlockingEndpoint(List<NonBlockingEndPoint> nonBlockingEndpoint) {
        this.nonBlockingEndpoint = nonBlockingEndpoint;
    }
    
    public List<BlockingEndPoint> getBlockingEndpoint() {
        return blockingEndpoint;
    }
    
    public void setBlockingEndpoint(List<BlockingEndPoint> blockingEndpoint) {
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
