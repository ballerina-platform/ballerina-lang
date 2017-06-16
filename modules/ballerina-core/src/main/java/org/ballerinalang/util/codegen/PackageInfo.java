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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code PackageInfo} contains metadata of a Ballerina package entry in the program file.
 *
 * @since 0.87
 */
public class PackageInfo {

    private String pkgPath;
    private FunctionInfo initFunctionInfo;

    private ConstantPoolEntry[] constPool;
    private List<ConstantPoolEntry> constantPoolEntries = new ArrayList<>();

    private Instruction[] instructions;
    private List<Instruction> instructionList = new ArrayList<>();

    private Map<String, FunctionInfo> functionInfoMap = new LinkedHashMap<>();

    private Map<String, ConnectorInfo> connectorInfoMap = new HashMap<>();

    private Map<String, StructInfo> structInfoMap = new HashMap<>();

    private Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

    // TODO : Move this into CallableUnitInfo
    private List<LineNumberInfo> lineNumberInfoList = new ArrayList<>();

    // TODO : Move this into CallableUnitInfo
    private List<ErrorTableEntry> errorTableEntriesList = new ArrayList<>();

    // Package level variable count
    protected int[] plvCount;

    // cache values.
    ProgramFile programFile;

    public PackageInfo(String packageName) {
        this.pkgPath = packageName;
    }

    // CP

    public int addCPEntry(ConstantPoolEntry cpEntry) {
        if (constantPoolEntries.contains(cpEntry)) {
            return constantPoolEntries.indexOf(cpEntry);
        }

        constantPoolEntries.add(cpEntry);
        return constantPoolEntries.size() - 1;
    }

    public ConstantPoolEntry getCPEntry(int index) {
        return constantPoolEntries.get(index);
    }

    public int getCPEntryIndex(ConstantPoolEntry cpEntry) {
        return constantPoolEntries.indexOf(cpEntry);
    }

    public ConstantPoolEntry[] getConstPool() {
        return constPool;
    }

    public FunctionInfo getFunctionInfo(String functionName) {
        return functionInfoMap.get(functionName);
    }

    public FunctionInfo[] getFunctionInfoCollection() {
        return functionInfoMap.values().toArray(new FunctionInfo[0]);
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
        connectorInfo.setPackageInfo(this);
        connectorInfoMap.put(connectorName, connectorInfo);
    }

    public ServiceInfo[] getServiceInfoList() {
        return serviceInfoMap.values().toArray(new ServiceInfo[0]);
    }

    public ServiceInfo getServiceInfo(String serviceName) {
        return serviceInfoMap.get(serviceName);
    }

    public void addServiceInfo(String serviceName, ServiceInfo serviceInfo) {
        serviceInfo.setPackageInfo(this);
        serviceInfoMap.put(serviceName, serviceInfo);
    }

    public int addInstruction(Instruction instruction) {
        instructionList.add(instruction);
        return instructionList.size() - 1;
    }

    public Instruction[] getInstructions() {
        return instructions;
    }

    public int getInstructionCount() {
        return instructionList.size();
    }

    // LineNumberInfo

    public List<LineNumberInfo> getLineNumberInfoList() {
        return lineNumberInfoList;
    }

    public void addLineNumberInfo(LineNumberInfo lineNumberInfo) {
        lineNumberInfoList.add(lineNumberInfo);
    }

    public LineNumberInfo getLineNumberInfo(LineNumberInfo lineNumberInfo) {
        int index = lineNumberInfoList.indexOf(lineNumberInfo);
        if (index >= 0) {
            return lineNumberInfoList.get(index);
        }
        return null;
    }

    public LineNumberInfo getLineNumberInfo(int currentIP) {
        LineNumberInfo old = null;
        for (LineNumberInfo lineNumberInfo : lineNumberInfoList) {
            if (currentIP == lineNumberInfo.getIp()) {
                // best case.
                return lineNumberInfo;
            }
            if (old != null && currentIP > old.getIp() && currentIP < lineNumberInfo.getIp()) {
                // TODO : Check condition currentIP > lineNumberInfo.getIP() in different scopes.
                return old;
            }
            old = lineNumberInfo;
        }
        return null;
    }

    public List<ErrorTableEntry> getErrorTableEntriesList() {
        return errorTableEntriesList;
    }

    public void addErrorTableEntry(ErrorTableEntry errorTableEntry) {
        errorTableEntriesList.add(errorTableEntry);
    }

    public ProgramFile getProgramFile() {
        return programFile;
    }

    public void setProgramFile(ProgramFile programFile) {
        this.programFile = programFile;
    }

    public FunctionInfo getInitFunctionInfo() {
        return initFunctionInfo;
    }

    public void setInitFunctionInfo(FunctionInfo initFunctionInfo) {
        this.initFunctionInfo = initFunctionInfo;
    }

    public void complete() {
        this.constPool = constantPoolEntries.toArray(new ConstantPoolEntry[0]);
        this.instructions = instructionList.toArray(new Instruction[0]);
    }

    public String getPkgPath() {
        return pkgPath;
    }
}
