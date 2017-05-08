/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.codegen;

import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code PackageInfo} contains metadata of a Ballerina package entry in the program file.
 *
 * @since 0.87
 */
public class PackageInfo {

    private String pkgPath;

    private List<ConstantPoolEntry> constPool = new ArrayList<>();

    private List<Instruction> instructionList = new ArrayList<>();

    private Map<String, FunctionInfo> functionInfoMap = new HashMap<>();

    private Map<String, ConnectorInfo> connectorInfoMap = new HashMap<>();

    private Map<String, StructInfo> structInfoMap = new HashMap<>();

    private Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

    // Package level variable count
    protected int[] plvCount;

    public PackageInfo(String packageName) {
        this.pkgPath = packageName;
    }

    // CP

    public int addCPEntry(ConstantPoolEntry cpEntry) {
        if (constPool.contains(cpEntry)) {
            return constPool.indexOf(cpEntry);
        }

        constPool.add(cpEntry);
        return constPool.size() - 1;
    }

    public int getCPEntryIndex(ConstantPoolEntry cpEntry) {
        return constPool.indexOf(cpEntry);
    }

    public List<ConstantPoolEntry> getConstPool() {
        return constPool;
    }

    public FunctionInfo getFunctionInfo(String functionName) {
        return functionInfoMap.get(functionName);
    }

    public void addFunctionInfo(String functionName, FunctionInfo functionInfo) {
        functionInfoMap.put(functionName, functionInfo);
    }

    public StructInfo getStructInfo(String structName) {
        return structInfoMap.get(structName);
    }

    public void addStructInfo(String structName, StructInfo structInfo) {
        structInfoMap.put(structName, structInfo);
    }

    public ConnectorInfo getConnectorInfo(String connectorName) {
        return connectorInfoMap.get(connectorName);
    }

    public void addConnectorInfo(String connectorName, ConnectorInfo connectorInfo) {
        connectorInfoMap.put(connectorName, connectorInfo);
    }

    public ServiceInfo getServiceInfo(String serviceName) {
        return serviceInfoMap.get(serviceName);
    }

    public void addServiceInfo(String serviceName, ServiceInfo serviceInfo) {
        serviceInfoMap.put(serviceName, serviceInfo);
    }

    public int addInstruction(Instruction instruction) {
        instructionList.add(instruction);
        return instructionList.size() - 1;
    }

    public List<Instruction> getInstructionList() {
        return instructionList;
    }

    public void setPackageLevelVarCount(int[] pkgLevelVarCount) {
        this.plvCount = pkgLevelVarCount;
    }

    public int[] getPackageLevelVarCount() {
        return plvCount;
    }
}
