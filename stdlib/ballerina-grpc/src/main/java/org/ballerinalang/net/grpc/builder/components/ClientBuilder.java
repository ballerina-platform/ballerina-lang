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
 * This class is responsible for generating client components.
 */
public class ClientBuilder {
    private String packageName;
    private String rootDescriptorKey;
    private List<Struct> struct = new ArrayList<>();
    private List<Stub> stubs = new ArrayList<>();
    private List<Descriptor> descriptors = new ArrayList<>();
    private List<BlockingFunction> blockingFunctions = new ArrayList<>();
    private List<NonBlockingFunction> nonBlockingFunctions = new ArrayList<>();
    private List<StreamingFunction> streamingFunctions = new ArrayList<>();
    private List<StubFunctionBuilder> stubFunctions = new ArrayList<>();
    private Client client;
    private String connectorId;
    
    public ClientBuilder(String packageName, String connectorId) {
        this.packageName = packageName;
        this.connectorId = connectorId;
        this.client = new Client(connectorId);
    }
    
    public void addBlockingFunction(String operationId, String inputDataType, String outputDataType, String methodId) {
        BlockingFunction blockingFunctionsObj = new BlockingFunction
                ("Blocking", connectorId, operationId, inputDataType, outputDataType, methodId);
        blockingFunctions.add(blockingFunctionsObj);
    }
    
    public void addStubFunctionBuilder(String stubTypeName) {
        StubFunctionBuilder stubFunctionsObj = new StubFunctionBuilder(connectorId, stubTypeName);
        stubFunctions.add(stubFunctionsObj);
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
    
    public void addStruct(String structId, String[] attributesNameArr, String[] attributesTypeArr) {
        Struct structObj = new Struct(structId);
        for (int i = 0; i < attributesNameArr.length; i++) {
            structObj.addAttribute(attributesNameArr[i], attributesTypeArr[i]);
        }
        struct.add(structObj);
    }
    
    public void addStub(String stubTypeName, String stubType) {
        Stub stub = new Stub(connectorId, stubTypeName, stubType);
        stubs.add(stub);
    }
    
    public void addStubObjects(String stubTypeName, String stubType) {
        Stub stubObject = new Stub(connectorId, stubTypeName, stubType);
        client.addStubObjects(stubObject);
    }
    
    public void addDescriptor(Descriptor descriptor) {
        descriptors.add(descriptor);
    }
    
    public List<Struct> getStructs() {
        return struct;
    }
    
    public void setStructs(List<Struct> structs) {
        this.struct = structs;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public String getRootDescriptorKey() {
        return rootDescriptorKey;
    }
    
    public void setRootDescriptorKey(String rootDescriptorKey) {
        this.rootDescriptorKey = rootDescriptorKey;
    }
    
    public List<Struct> getStruct() {
        return struct;
    }
    
    public void setStruct(List<Struct> struct) {
        this.struct = struct;
    }
    
    public List<Descriptor> getDescriptors() {
        return descriptors;
    }
    
    public void setDescriptors(List<Descriptor> descriptors) {
        this.descriptors = descriptors;
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
    
    public List<StubFunctionBuilder> getStubFunctionBuilder() {
        return stubFunctions;
    }
    
    public void setStubFunctionBuilder(List<StubFunctionBuilder> stubFunctions) {
        this.stubFunctions = stubFunctions;
    }
    
    public boolean isFunctionsStremingNotEmpty() {
        return (!nonBlockingFunctions.isEmpty() || !streamingFunctions.isEmpty());
    }
    
    public boolean isFunctionsUnaryNotEmpty() {
        return (!blockingFunctions.isEmpty());
    }
    
    
}
