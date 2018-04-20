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

import org.ballerinalang.net.grpc.exception.GrpcServerValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for generating client components.
 */
public class ClientBuilder {
    
    private String rootDescriptorKey;
    private List<Struct> structs = new ArrayList<>();
    private List<Enum> enums = new ArrayList<>();
    private List<StubObject> stubObject = new ArrayList<>();
    private StubObject stubObjectBlocking;
    private StubObject stubObjectNonBlocking;
    private List<Descriptor> descriptors = new ArrayList<>();
    private List<StubFunctionBuilder> stubFunctions = new ArrayList<>();
    private List<ClientObject> client = new ArrayList<>();
    private ClientObject clientBlocking;
    private ClientObject clientNonBlocking;
    
    private String connectorId;
    
    public ClientBuilder(String connectorId) {
        this.connectorId = connectorId;
    }
    
    public void addStubFunctionBuilder(String stubTypeName) {
        StubFunctionBuilder stubFunctionsObj = new StubFunctionBuilder(connectorId, stubTypeName);
        stubFunctions.add(stubFunctionsObj);
    }
    
    public void addStruct(String structId, String[] attributesNameArr, String[] attributesTypeArr,
                          String[] attributesLabelArr) {
        Struct structObj = new Struct(structId);
        for (int i = 0; i < attributesNameArr.length; i++) {
            structObj.addAttribute(attributesNameArr[i], attributesTypeArr[i], attributesLabelArr[i]);
        }
        structs.add(structObj);
    }
    
    public boolean isStructContains(String structId) {
        for (Struct struct : structs) {
            if (structId.equals(struct.getStructId())) {
                return true;
            }
        }
        return false;
    }
    
    public void addEnum(String enumId, String[] attributesNameArr) {
        Enum enumObj = new Enum(enumId);
        for (int i = 0; i < attributesNameArr.length; i++) {
            String anAttributesNameArr = attributesNameArr[i];
            if (i < attributesNameArr.length - 1) {
                enumObj.addAttribute(anAttributesNameArr, ",");
            } else {
                enumObj.addAttribute(anAttributesNameArr, null);
            }
        }
        enums.add(enumObj);
    }
    
    public void addStubObjects(String stubTypeName, String stubType) {
        Stub stubObject = new Stub(connectorId, stubTypeName, stubType);
        if ("blocking".equals(stubType)) {
            if (clientBlocking == null) {
                this.clientBlocking = new ClientObject(connectorId);
                this.clientBlocking.setStubType("Blocking");
                this.client.add(clientBlocking);
            }
            clientBlocking.addStubObjects(stubObject);
        } else {
            if (clientNonBlocking == null) {
                this.clientNonBlocking = new ClientObject(connectorId);
                this.client.add(clientNonBlocking);
            }
            clientNonBlocking.addStubObjects(stubObject);
        }
    }
    
    public void addStub(String stubTypeName, String stubType) {
        if ("blocking".equals(stubType)) {
            if (stubObjectBlocking == null) {
                this.stubObjectBlocking = new StubObject(connectorId);
                this.stubObjectBlocking.setStubType("Blocking");
                this.stubObject.add(this.stubObjectBlocking);
            }
            stubObjectBlocking.addStub(stubTypeName, stubType);
        } else if ("non-blocking".equals(stubType)) {
            if (stubObjectNonBlocking == null) {
                this.stubObjectNonBlocking = new StubObject(connectorId);
                this.stubObject.add(this.stubObjectNonBlocking);
            }
            stubObjectNonBlocking.addStub(stubTypeName, stubType);
        } else {
            throw new GrpcServerValidationException("invalid stub type '" + stubType + "'.");
        }
    }
    
    public void addBlockingFunction(String operationId, String inputDataType, String outputDataType, String methodId) {
        if (stubObjectBlocking != null) {
            stubObjectBlocking.addBlockingFunction(operationId, inputDataType, outputDataType, methodId);
        }
    }
    
    public void addNonBlockingFunction(String operationId, String inputDataType, String methodId) {
        if (stubObjectNonBlocking != null) {
            stubObjectNonBlocking.addNonBlockingFunction(operationId, inputDataType, methodId);
        }
    }
    
    public void addStreamingFunction(String operationId, String inputDataType, String methodId) {
        if (stubObjectNonBlocking != null) {
            stubObjectNonBlocking.addStreamingFunction(operationId, inputDataType, methodId);
        }
    }
    
    public boolean isFunctionsStreamingNotEmpty() {
        return stubObjectNonBlocking != null && stubObjectNonBlocking.isFunctionsStreamingNotEmpty();
    }
    
    public void addDescriptor(Descriptor descriptor) {
        descriptors.add(descriptor);
    }
    
    public List<Struct> getStructs() {
        return structs;
    }
    
    public void setStructs(List<Struct> structs) {
        this.structs = structs;
    }
    
    public String getRootDescriptorKey() {
        return rootDescriptorKey;
    }
    
    public void setRootDescriptorKey(String rootDescriptorKey) {
        this.rootDescriptorKey = rootDescriptorKey;
    }
    
    public List<Descriptor> getDescriptors() {
        return descriptors;
    }
    
    public void setDescriptors(List<Descriptor> descriptors) {
        this.descriptors = descriptors;
    }
    
    public List<StubFunctionBuilder> getStubFunctionBuilder() {
        return stubFunctions;
    }
    
    public void setStubFunctionBuilder(List<StubFunctionBuilder> stubFunctions) {
        this.stubFunctions = stubFunctions;
    }
    
}
