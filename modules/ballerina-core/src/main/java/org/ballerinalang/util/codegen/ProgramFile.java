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

import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.StructureType;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;
import org.ballerinalang.util.codegen.attributes.VarTypeCountAttributeInfo;
import org.ballerinalang.util.codegen.cpentries.ConstantPool;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code ProgramFile} is the runtime representation of a compiled Ballerina program.
 *
 * @since 0.87
 */
public class ProgramFile implements ConstantPool, AttributeInfoPool {
    // TODO Finalize the magic value;
    private int magicValue = 0xBBBBBBBB;
    private short version = (short) 10;

    private List<ConstantPoolEntry> constPool = new ArrayList<>();
    private Map<String, PackageInfo> packageInfoMap = new LinkedHashMap<>();

    private int mainPkgCPIndex;
    private String mainPkgName = "";
    private List<String> servicePackageNameList = new ArrayList<>();

    // Cached values.
    // This is the actual path given by the user and this is used primarily for error reporting
    private Path programFilePath;

    private StructureType globalMemoryBlock;

    private Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();


    public int getMagicValue() {
        return magicValue;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    // CP
    @Override
    public int addCPEntry(ConstantPoolEntry cpEntry) {
        if (constPool.contains(cpEntry)) {
            return constPool.indexOf(cpEntry);
        }

        constPool.add(cpEntry);
        return constPool.size() - 1;
    }

    @Override
    public ConstantPoolEntry getCPEntry(int index) {
        return constPool.get(index);
    }

    @Override
    public int getCPEntryIndex(ConstantPoolEntry cpEntry) {
        return constPool.indexOf(cpEntry);
    }

    @Override
    public ConstantPoolEntry[] getConstPoolEntries() {
        return constPool.toArray(new ConstantPoolEntry[0]);
    }

    // PackageInfo

    public PackageInfo getPackageInfo(String packageName) {
        return packageInfoMap.get(packageName);
    }

    public PackageInfo[] getPackageInfoEntries() {
        return packageInfoMap.values().toArray(new PackageInfo[0]);
    }

    public void addPackageInfo(String packageName, PackageInfo packageInfo) {
        packageInfoMap.put(packageName, packageInfo);
    }

    public StructureType getGlobalMemoryBlock() {
        return globalMemoryBlock;
    }

    // Main package.

    public int getMainPkgCPIndex() {
        return mainPkgCPIndex;
    }

    public void setMainPkgCPIndex(int mainPkgCPIndex) {
        this.mainPkgCPIndex = mainPkgCPIndex;
    }

    public void setMainPkgName(String mainPackageName) {
        this.mainPkgName = mainPackageName;
    }

    public String[] getServicePackageNameList() {
        return servicePackageNameList.toArray(new String[0]);
    }

    // Service package.

    public String getMainPackageName() {
        return mainPkgName;
    }

    public void addServicePackage(String servicePackageName) {
        this.servicePackageNameList.add(servicePackageName);
    }

    // Information about ProgramFile, which are set from outside.

    public Path getProgramFilePath() {
        return programFilePath;
    }

    public void setProgramFilePath(Path programFilePath) {
        this.programFilePath = programFilePath;
    }

    @Override
    public AttributeInfo getAttributeInfo(AttributeInfo.Kind attributeKind) {
        return attributeInfoMap.get(attributeKind);
    }

    @Override
    public void addAttributeInfo(AttributeInfo.Kind attributeKind, AttributeInfo attributeInfo) {
        attributeInfoMap.put(attributeKind, attributeInfo);
        if (attributeKind == AttributeInfo.Kind.VARIABLE_TYPE_COUNT_ATTRIBUTE) {
            // TODO Move this out of the program file to a program context.. Runtime representation of a program.
            // TODO ProgramFile is the static program data.
            VarTypeCountAttributeInfo varTypeCountAttribInfo = (VarTypeCountAttributeInfo) attributeInfo;
            int[] globalVarCount = varTypeCountAttribInfo.getVarTypeCount();
            
            // Initialize global memory block
            BStructType dummyType = new BStructType("", "");
            dummyType.setFieldTypeCount(globalVarCount);
            this.globalMemoryBlock = new BStruct(dummyType);
        }
    }

    @Override
    public AttributeInfo[] getAttributeInfoEntries() {
        return attributeInfoMap.values().toArray(new AttributeInfo[0]);
    }
}
