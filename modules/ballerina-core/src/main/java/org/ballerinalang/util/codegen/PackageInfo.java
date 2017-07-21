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

import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;
import org.ballerinalang.util.codegen.attributes.LineNumberTableAttributeInfo;
import org.ballerinalang.util.codegen.cpentries.ConstantPool;
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
public class PackageInfo implements ConstantPool, AttributeInfoPool {

    private int pkgNameCPIndex;
    private String pkgPath;
    private FunctionInfo initFunctionInfo;

    private ConstantPoolEntry[] constPool;
    private List<ConstantPoolEntry> constantPoolEntries = new ArrayList<>();

    private Instruction[] instructions;
    private List<Instruction> instructionList = new ArrayList<>();

    private Map<String, PackageVarInfo> constantInfoMap = new LinkedHashMap<>();

    private Map<String, PackageVarInfo> globalVarInfoMap = new LinkedHashMap<>();

    private Map<String, FunctionInfo> functionInfoMap = new LinkedHashMap<>();

    private Map<String, ConnectorInfo> connectorInfoMap = new HashMap<>();

    private Map<String, StructInfo> structInfoMap = new HashMap<>();

    private Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

    private Map<String, StructureTypeInfo> structureTypeInfoMap = new HashMap<>();

    private Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();

    // cache values.
    ProgramFile programFile;

    public PackageInfo(int packageNameCPIndex, String packageName) {
        this.pkgNameCPIndex = packageNameCPIndex;
        this.pkgPath = packageName;
    }

    public int getPkgNameCPIndex() {
        return pkgNameCPIndex;
    }

    public String getPkgPath() {
        return pkgPath;
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

    public ConstantPoolEntry[] getConstPoolEntries() {
        return constPool;
    }

    public PackageVarInfo getConstantInfo(String constantName) {
        return constantInfoMap.get(constantName);
    }

    public void addConstantInfo(String constantName, PackageVarInfo constantInfo) {
        constantInfoMap.put(constantName, constantInfo);
    }

    public PackageVarInfo[] getConstantInfoEntries() {
        return constantInfoMap.values().toArray(new PackageVarInfo[0]);
    }

    public PackageVarInfo getPackageVarInfo(String globalVarName) {
        return globalVarInfoMap.get(globalVarName);
    }

    public void addPackageVarInfo(String globalVarName, PackageVarInfo packageVarInfo) {
        globalVarInfoMap.put(globalVarName, packageVarInfo);
    }

    public PackageVarInfo[] getPackageInfoEntries() {
        return globalVarInfoMap.values().toArray(new PackageVarInfo[0]);
    }

    public FunctionInfo getFunctionInfo(String functionName) {
        return functionInfoMap.get(functionName);
    }

    public void addFunctionInfo(String functionName, FunctionInfo functionInfo) {
        functionInfoMap.put(functionName, functionInfo);
    }

    public FunctionInfo[] getFunctionInfoEntries() {
        return functionInfoMap.values().toArray(new FunctionInfo[0]);
    }

    public StructInfo getStructInfo(String structName) {
        return structInfoMap.get(structName);
    }

    public void addStructInfo(String structName, StructInfo structInfo) {
        structInfoMap.put(structName, structInfo);
        structureTypeInfoMap.put(structName, structInfo);
    }

    public StructInfo[] getStructInfoEntries() {
        return structInfoMap.values().toArray(new StructInfo[0]);
    }

    public ConnectorInfo getConnectorInfo(String connectorName) {
        return connectorInfoMap.get(connectorName);
    }

    public void addConnectorInfo(String connectorName, ConnectorInfo connectorInfo) {
        connectorInfo.setPackageInfo(this);
        connectorInfoMap.put(connectorName, connectorInfo);
        structureTypeInfoMap.put(connectorName, connectorInfo);
    }

    public ConnectorInfo[] getConnectorInfoEntries() {
        return connectorInfoMap.values().toArray(new ConnectorInfo[0]);
    }

    public ServiceInfo[] getServiceInfoEntries() {
        return serviceInfoMap.values().toArray(new ServiceInfo[0]);
    }

    public ServiceInfo getServiceInfo(String serviceName) {
        return serviceInfoMap.get(serviceName);
    }

    public void addServiceInfo(String serviceName, ServiceInfo serviceInfo) {
        serviceInfo.setPackageInfo(this);
        serviceInfoMap.put(serviceName, serviceInfo);
        structureTypeInfoMap.put(serviceName, serviceInfo);
    }

    public StructureTypeInfo getStructureTypeInfo(String structureTypeName) {
        return structureTypeInfoMap.get(structureTypeName);
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

    public LineNumberInfo getLineNumberInfo(LineNumberInfo lineNumberInfo) {
        LineNumberTableAttributeInfo lineNumberTableAttributeInfo = (LineNumberTableAttributeInfo) attributeInfoMap
                .get(AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE);
        List<LineNumberInfo> lineNumberInfos = lineNumberTableAttributeInfo.getLineNumberInfoList();
        int index = lineNumberInfos.indexOf(lineNumberInfo);
        if (index >= 0) {
            return lineNumberInfos.get(index);
        }
        return null;
    }

    public LineNumberInfo getLineNumberInfo(int currentIP) {
        LineNumberInfo old = null;
        LineNumberTableAttributeInfo lineNumberTableAttributeInfo = (LineNumberTableAttributeInfo) attributeInfoMap
                .get(AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE);
        List<LineNumberInfo> lineNumberInfos = lineNumberTableAttributeInfo.getLineNumberInfoList();
        for (LineNumberInfo lineNumberInfo : lineNumberInfos) {
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

    @Override
    public AttributeInfo getAttributeInfo(AttributeInfo.Kind attributeKind) {
        return attributeInfoMap.get(attributeKind);
    }

    @Override
    public void addAttributeInfo(AttributeInfo.Kind attributeKind, AttributeInfo attributeInfo) {
        attributeInfoMap.put(attributeKind, attributeInfo);
    }

    @Override
    public AttributeInfo[] getAttributeInfoEntries() {
        return attributeInfoMap.values().toArray(new AttributeInfo[0]);
    }
}
