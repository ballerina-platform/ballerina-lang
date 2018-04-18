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

import static org.ballerinalang.net.grpc.MessageConstants.EMPTY_DATATYPE_NAME;

/**
 * Stub Object bean class.
 */
public class StubObject {
    private String stubType;
    private String connectorId;
    private List<Stub> stubs = new ArrayList<>();
    private List<BlockingFunction> blockingFunctions = new ArrayList<>();
    private List<NonBlockingFunction> nonBlockingFunctions = new ArrayList<>();
    private List<StreamingFunction> streamingFunctions = new ArrayList<>();
    
    public StubObject(String connectorId) {
        this.connectorId = connectorId;
    }
    
    public void addStub(String stubTypeName, String stubType) {
        Stub stub = new Stub(connectorId, stubTypeName, stubType);
        stubs.add(stub);
    }
    
    public void addBlockingFunction(String operationId, String inputDataType, String outputDataType, String methodId) {
        BlockingFunction blockingFunctionsObj = new BlockingFunction
                ("Blocking", connectorId, operationId, inputDataType, outputDataType, methodId);
        if(EMPTY_DATATYPE_NAME.equals(inputDataType)) {
            blockingFunctionsObj.setInputComma(null);
            blockingFunctionsObj.setInputAttributeName(null);
            blockingFunctionsObj.setInputDataType(null);
            blockingFunctionsObj.initEmptyStruct();
        }
        if(EMPTY_DATATYPE_NAME.equals(outputDataType)) {
            blockingFunctionsObj.setOutputComma(null);
            blockingFunctionsObj.setOutputDataType(null);
            blockingFunctionsObj.ignoreCast();
        }
        blockingFunctions.add(blockingFunctionsObj);
    }
    
    public void addNonBlockingFunction(String operationId, String inputDataType, String methodId) {
        NonBlockingFunction nonBlockingFunctionsObj = new NonBlockingFunction(
                null, connectorId, operationId, inputDataType, methodId);
        nonBlockingFunctions.add(nonBlockingFunctionsObj);
    }
    
    public void addStreamingFunction(String operationId, String inputDataType, String methodId) {
        StreamingFunction streamingFunctionsObj = new StreamingFunction("NonBlocking", connectorId,
                operationId, inputDataType, methodId);
        streamingFunctions.add(streamingFunctionsObj);
    }
    
    public List<Stub> getStubs() {
        return stubs;
    }
    
    public void setStubs(List<Stub> stubs) {
        this.stubs = stubs;
    }
    
    public List getBlockingFunction() {
        return blockingFunctions;
    }
    
    public void setBlockingFunction(List<BlockingFunction> blockingFunctions) {
        this.blockingFunctions = blockingFunctions;
    }
    
    public List getNonBlockingFunction() {
        return nonBlockingFunctions;
    }
    
    public void setNonBlockingFunction(List<NonBlockingFunction> nonBlockingFunctions) {
        this.nonBlockingFunctions = nonBlockingFunctions;
    }
    
    public List getStreamingFunction() {
        return streamingFunctions;
    }
    
    public void setStreamingFunction(List<StreamingFunction> streamingFunctions) {
        this.streamingFunctions = streamingFunctions;
    }
    
    public boolean isFunctionsStreamingNotEmpty() {
        return (!nonBlockingFunctions.isEmpty() || !streamingFunctions.isEmpty());
    }
    
    public boolean isFunctionsUnaryNotEmpty() {
        return (!blockingFunctions.isEmpty());
    }
    
    public String getStubType() {
        return stubType;
    }
    
    public void setStubType(String stubType) {
        this.stubType = stubType;
    }
}
