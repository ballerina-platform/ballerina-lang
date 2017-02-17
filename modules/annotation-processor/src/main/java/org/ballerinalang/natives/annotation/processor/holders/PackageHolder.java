/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.natives.annotation.processor.holders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DTO to hold Ballerina package. Contains the native functions, native connectors and Native type 
 * convertors belongs to a package.
 */
public class PackageHolder {
    
    private String packageName;
    private List<FunctionHolder> nativeFunctions;
    private List<TypeMapperHolder> nativeTypeMapper;
    private Map<String, ConnectorHolder> connectors;
    
    public PackageHolder(String packageName) {
        this.packageName = packageName;
        this.connectors = new HashMap<String, ConnectorHolder>();
        this.nativeFunctions = new ArrayList<FunctionHolder>();
        this.nativeTypeMapper = new ArrayList<TypeMapperHolder>();
    }

    public void addFunction(FunctionHolder function) {
        this.nativeFunctions.add(function);
    }

    public void addConnector(String connectorName, ConnectorHolder connector) {
        this.connectors.put(connectorName, connector);
    }

    public void addTypeMapper(TypeMapperHolder typeConvertor) {
        this.nativeTypeMapper.add(typeConvertor);
    }
    
    public FunctionHolder[] getFunctions() {
        return nativeFunctions.toArray(new FunctionHolder[0]);
    }
    
    public ConnectorHolder[] getConnectors() {
        return connectors.values().toArray(new ConnectorHolder[0]);
    }
    
    public ConnectorHolder getConnector(String connectorName) {
        return connectors.get(connectorName);
    }
    
    public TypeMapperHolder[] getTypeMapper() {
        return nativeTypeMapper.toArray(new TypeMapperHolder[0]);
    }

    public String getPackageName() {
        return packageName;
    }
    
    @Override
    public String toString() {
        return "package " + packageName + ";\n\n"
                + nativeFunctions.stream().map(k -> k.toString()).collect(Collectors.joining("\n\n"))
                + (nativeFunctions.size() > 0 ? "\n\n" : "")
                + nativeTypeMapper.stream().map(k -> k.toString()).collect(Collectors.joining("\n\n"))
                + (nativeTypeMapper.size() > 0 ? "\n\n" : "")
                + connectors.values().stream().map(k -> k.toString()).collect(Collectors.joining("\n\n"));
    }
}
