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
package org.wso2.ballerinalang.programfile;

import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfoPool;
import org.wso2.ballerinalang.programfile.attributes.LineNumberTableAttributeInfo;
import org.wso2.ballerinalang.programfile.cpentries.ConstantPool;
import org.wso2.ballerinalang.programfile.cpentries.ConstantPoolEntry;

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

    public int nameCPIndex;
    public int versionCPIndex;

    private ConstantPoolEntry[] constPool;
    private List<ConstantPoolEntry> constantPoolEntries = new ArrayList<>();

    public List<Instruction> instructionList = new ArrayList<>();

    private Map<String, PackageVarInfo> constantInfoMap = new LinkedHashMap<>();

    public Map<String, PackageVarInfo> pkgVarInfoMap = new LinkedHashMap<>();

    public Map<String, FunctionInfo> functionInfoMap = new LinkedHashMap<>();

    public Map<String, ConnectorInfo> connectorInfoMap = new HashMap<>();

    private Map<String, StructInfo> structInfoMap = new HashMap<>();

    public Map<String, EnumInfo> enumInfoMap = new HashMap<>();

    public Map<String, ServiceInfo> serviceInfoMap = new HashMap<>();

    private Map<String, StructureTypeInfo> structureTypeInfoMap = new HashMap<>();

    private Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();
    
    public Map<String, TransformerInfo> transformerInfoMap = new LinkedHashMap<>();

    public PackageInfo(int nameCPIndex, int versionCPIndex) {
        this.nameCPIndex = nameCPIndex;
        this.versionCPIndex = versionCPIndex;
    }

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
        return constantPoolEntries.toArray(new ConstantPoolEntry[0]);
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

    public PackageVarInfo[] getPackageInfoEntries() {
        return pkgVarInfoMap.values().toArray(new PackageVarInfo[0]);
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

    public EnumInfo getEnumInfo(String enumName) {
        return enumInfoMap.get(enumName);
    }

    public void addEnumInfo(String enumName, EnumInfo enumInfo) {
        enumInfoMap.put(enumName, enumInfo);
        structureTypeInfoMap.put(enumName, enumInfo);
    }

    public EnumInfo[] getEnumInfoEntries() {
        return enumInfoMap.values().toArray(new EnumInfo[0]);
    }

    public ConnectorInfo getConnectorInfo(String connectorName) {
        return connectorInfoMap.get(connectorName);
    }

    public void addConnectorInfo(String connectorName, ConnectorInfo connectorInfo) {
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
        serviceInfoMap.put(serviceName, serviceInfo);
        structureTypeInfoMap.put(serviceName, serviceInfo);
    }

    public StructureTypeInfo getStructureTypeInfo(String structureTypeName) {
        return structureTypeInfoMap.get(structureTypeName);
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

    public void complete() {
        this.constPool = constantPoolEntries.toArray(new ConstantPoolEntry[0]);
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
