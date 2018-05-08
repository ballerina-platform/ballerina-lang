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

import org.ballerinalang.bre.bvm.GlobalMemoryArea;
import org.ballerinalang.connector.impl.ServerConnectorRegistry;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.LockableStructureType;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;
import org.ballerinalang.util.codegen.attributes.VarTypeCountAttributeInfo;
import org.ballerinalang.util.codegen.cpentries.ConstantPool;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;
import org.ballerinalang.util.debugger.Debugger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.util.BLangConstants.MAGIC_NUMBER;
import static org.ballerinalang.util.BLangConstants.VERSION_NUMBER;

/**
 * {@code ProgramFile} is the runtime representation of a compiled Ballerina program.
 *
 * @since 0.87
 */
public class ProgramFile implements ConstantPool, AttributeInfoPool {
    // Entry point flags
    public static final int EP_MAIN_FLAG = 1;
    public static final int EP_SERVICE_FLAG = 2;

    private int magicValue = MAGIC_NUMBER;
    // TODO Finalize the version number;
    private short version = VERSION_NUMBER;

    private List<ConstantPoolEntry> constPool = new ArrayList<>();
    private Map<String, PackageInfo> packageInfoMap = new LinkedHashMap<>();

    private int entryPkgCPIndex;
    private String entryPkgName;
    private PackageInfo entryPackage;
    private ServerConnectorRegistry serverConnectorRegistry;
    private boolean mainFucAvailable = false;
    private boolean servicesAvailable = false;

    private Debugger debugger;
    private boolean distributedTransactionEnabled = false;

    // Cached values.
    // This is the actual path given by the user and this is used primarily for error reporting
    private Path programFilePath;

    private LockableStructureType globalMemoryBlock;
    public GlobalMemoryArea globalMemArea;

    private Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();

    private List<AnnAttributeValue> unresolvedAnnAttrValues = new ArrayList<>();

    public int getMagicValue() {
        return magicValue;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public int getEntryPkgCPIndex() {
        return entryPkgCPIndex;
    }

    public void setEntryPkgCPIndex(int entryPkgCPIndex) {
        this.entryPkgCPIndex = entryPkgCPIndex;
    }

    public String getEntryPkgName() {
        return entryPkgName;
    }

    public void setEntryPkgName(String entryPkgName) {
        this.entryPkgName = entryPkgName;
    }

    public PackageInfo getEntryPackage() {
        return entryPackage;
    }

    public void setEntryPackage(PackageInfo entryPackage) {
        this.entryPackage = entryPackage;
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

    public void setDistributedTransactionEnabled(boolean distributedTransactionEnabled) {
        this.distributedTransactionEnabled = distributedTransactionEnabled;
    }

    public boolean isDistributedTransactionEnabled() {
        return distributedTransactionEnabled;
    }

    public void setServiceEPAvailable(boolean servicesAvailable) {
        this.servicesAvailable = servicesAvailable;
    }

    @Deprecated
    public ServerConnectorRegistry getServerConnectorRegistry() {
        return serverConnectorRegistry;
    }

    @Deprecated
    public void setServerConnectorRegistry(ServerConnectorRegistry serverConnectorRegistry) {
        this.serverConnectorRegistry = serverConnectorRegistry;
    }

    public void initializeGlobalMemArea() {
        this.globalMemArea = new GlobalMemoryArea(this.getPackageInfoEntries());
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

    public LockableStructureType getGlobalMemoryBlock() {
        return globalMemoryBlock;
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

            // TODO Introduce an abstraction for memory blocks
            // Initialize global memory block
            BStructType dummyType = new BStructType(null, "", "", 0);
            dummyType.setFieldTypeCount(globalVarCount);
            this.globalMemoryBlock = new BStruct(dummyType);
        }
    }

    @Override
    public AttributeInfo[] getAttributeInfoEntries() {
        return attributeInfoMap.values().toArray(new AttributeInfo[0]);
    }

    public void setDebugger(Debugger debugManager) {
        this.debugger = debugManager;
    }

    public Debugger getDebugger() {
        return debugger;
    }

}
