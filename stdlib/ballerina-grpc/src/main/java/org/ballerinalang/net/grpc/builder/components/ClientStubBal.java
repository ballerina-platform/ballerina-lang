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
public class ClientStubBal {
    private String packageName;
    private String rootDescriptorKey;
    private List<Connector> connectors = new ArrayList<>();
    private List<Struct> struct = new ArrayList<>();
    private List<Descriptor> descriptors = new ArrayList<>();
    
    public ClientStubBal(String packageName) {
        this.packageName = packageName;
    }
    
    public void addStruct(String structId, String[] attributesNameArr, String[] attributesTypeArr) {
        Struct structObj = new Struct(structId);
        for (int i = 0; i < attributesNameArr.length; i++) {
            structObj.addAttribute(attributesNameArr[i], attributesTypeArr[i]);
        }
        struct.add(structObj);
    }
    
    public void addDescriptor(Descriptor descriptor) {
        descriptors.add(descriptor);
    }
    
    public void addConnector(Connector connector) {
        connectors.add(connector);
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
    
    public List<Connector> getConnectors() {
        return connectors;
    }
    
    public void setConnectors(List<Connector> connectors) {
        this.connectors = connectors;
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
}
