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
import org.wso2.ballerinalang.programfile.attributes.VarTypeCountAttributeInfo;
import org.wso2.ballerinalang.programfile.cpentries.ConstantPool;
import org.wso2.ballerinalang.programfile.cpentries.ConstantPoolEntry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.programfile.ProgramFileConstants.MAGIC_NUMBER;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.VERSION_NUMBER;

/**
 * {@code ProgramFile} is the runtime representation of a compiled Ballerina program.
 *
 * @since 0.87
 */
@Deprecated
public class ProgramFile implements ConstantPool, AttributeInfoPool {
    // Entry point flags
    public static final int EP_MAIN_FLAG = 1;
    public static final int EP_SERVICE_FLAG = 2;

    private int magicValue = MAGIC_NUMBER;
    // TODO Finalize the version number;
    private short version = VERSION_NUMBER;

    private List<ConstantPoolEntry> constPool = new ArrayList<>();
    public Map<String, PackageInfo> packageInfoMap = new LinkedHashMap<>();

    public int entryPkgCPIndex;
    private boolean mainFucAvailable = false;
    private boolean servicesAvailable = false;

    // This is the actual path given by the user and this is used primarily for error reporting
    private Path programFilePath;

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

    public boolean isMainEPAvailable() {
        return mainFucAvailable;
    }

    public void setMainEPAvailable(boolean mainFuncAvailable) {
        this.mainFucAvailable = mainFuncAvailable;
    }

    public boolean isServiceEPAvailable() {
        return servicesAvailable;
    }

    public void setServiceEPAvailable(boolean servicesAvailable) {
        this.servicesAvailable = servicesAvailable;
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

//            // Initialize global memory block
//            BStructType dummyType = new BStructType("", "");
//            dummyType.setFieldTypeCount(globalVarCount);
//            this.globalMemoryBlock = new BStruct(dummyType);
        }
    }

    @Override
    public AttributeInfo[] getAttributeInfoEntries() {
        return attributeInfoMap.values().toArray(new AttributeInfo[0]);
    }

}
