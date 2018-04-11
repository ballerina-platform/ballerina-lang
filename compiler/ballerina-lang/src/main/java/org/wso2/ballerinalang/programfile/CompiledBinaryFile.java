/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.ballerinalang.programfile.cpentries.ConstantPool;
import org.wso2.ballerinalang.programfile.cpentries.ConstantPoolEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.programfile.ProgramFileConstants.VERSION_NUMBER;

/**
 * {@code CompiledBinaryFile} is the super class of {@link ProgramFile} and {@link PackageFile}.
 *
 * @since 0.963.0
 */
public class CompiledBinaryFile implements ConstantPool, AttributeInfoPool {

    private List<ConstantPoolEntry> constPool = new ArrayList<>();
    private Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();

    // ConstantPool interface methods

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

    // AttributeInfoPool interface methods

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

    /**
     * {@code ProgramFile} is the runtime representation of a compiled Ballerina program (BALX).
     *
     * @since 0.87
     */
    public static class ProgramFile extends CompiledBinaryFile {
        // Entry point flags
        public static final int EP_MAIN_FLAG = 1;
        public static final int EP_SERVICE_FLAG = 2;

        // TODO Finalize the version number;
        private short version = VERSION_NUMBER;

        public Map<String, PackageInfo> packageInfoMap = new LinkedHashMap<>();

        public int entryPkgCPIndex;
        private boolean mainFucAvailable = false;
        private boolean servicesAvailable = false;

        public int getMagicValue() {
            return ProgramFileConstants.MAGIC_NUMBER;
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

        public PackageInfo[] getPackageInfoEntries() {
            return packageInfoMap.values().toArray(new PackageInfo[0]);
        }
    }

    /**
     * {@code PackageFile} is the representation of a compiled Ballerina package (BALO).
     *
     * @since 0.963.0
     */
    public static class PackageFile extends CompiledBinaryFile {

        public static final int MAGIC_VALUE = 0xFFFFFFFF;
        public static final int LANG_VERSION = VERSION_NUMBER;

        // TODO Temp
        public PackageInfo packageInfo = new PackageInfo();
    }
}
