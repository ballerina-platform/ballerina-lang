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
public class Connector {
    private String connectorId;
    private String stubType;
    
    private List<BlockingAction> blockingAction = new ArrayList<>();
    private List<NonBlockingAction> nonBlockingAction = new ArrayList<>();
    private List<StreamingAction> streamingAction = new ArrayList<>();
    
    
    public Connector(String connectorId, String stubType) {
        this.connectorId = connectorId;
        this.stubType = stubType;
    }
    
    public void addBlockingAction(String operationId, String inputDataType, String outputDataType, String methodId) {
        BlockingAction blockingActionObj = new BlockingAction
                (operationId, inputDataType, outputDataType, methodId);
        blockingAction.add(blockingActionObj);
    }
    
    public void addNonBlockingAction(String operationId, String inputDataType, String methodId) {
        NonBlockingAction nonBlockingActionObj = new NonBlockingAction(operationId, inputDataType, methodId);
        nonBlockingAction.add(nonBlockingActionObj);
    }
    
    public void addStreamingAction(String operationId, String inputDataType, String methodId) {
        StreamingAction streamingActionObj = new StreamingAction(operationId, inputDataType, methodId);
        streamingAction.add(streamingActionObj);
    }
    
    public String getConnectorId() {
        return connectorId;
    }
    
    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }
    
    public List getBlockingAction() {
        return blockingAction;
    }
    
    public void setBlockingAction(List<BlockingAction> blockingAction) {
        this.blockingAction = blockingAction;
    }
    
    public String getStubType() {
        return stubType;
    }
    
    public void setStubType(String stubType) {
        this.stubType = stubType;
    }
    
    public List getNonBlockingAction() {
        return nonBlockingAction;
    }
    
    public void setNonBlockingAction(List<NonBlockingAction> nonBlockingAction) {
        this.nonBlockingAction = nonBlockingAction;
    }
    
    public List getStreamingAction() {
        return streamingAction;
    }
    
    public void setStreamingAction(List<StreamingAction> streamingAction) {
        this.streamingAction = streamingAction;
    }
    
    public boolean isActionsNotEmpty() {
        return (!nonBlockingAction.isEmpty() || !blockingAction.isEmpty() || !streamingAction.isEmpty());
    }
}
