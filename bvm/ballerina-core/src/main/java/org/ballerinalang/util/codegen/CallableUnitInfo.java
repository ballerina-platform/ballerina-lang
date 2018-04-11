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

import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BSingletonType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.util.codegen.attributes.AnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;
import org.ballerinalang.util.codegen.cpentries.WorkerInfoPool;
import org.ballerinalang.util.program.WorkerDataIndex;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code CallableUnitInfo} contains common metadata of a Ballerina function/resource/action in the program file.
 *
 * @since 0.87
 */
public class CallableUnitInfo implements AttributeInfoPool, WorkerInfoPool {

    protected String pkgPath;
    protected String name;
    protected boolean isNative;

    // Index to the PackageCPEntry
    protected int pkgCPIndex;
    protected int nameCPIndex;

    protected BType[] paramTypes;
    protected BType[] retParamTypes;

    protected int signatureCPIndex;
    protected String signature;

    public int attachedToTypeCPIndex;
    public BType attachedToType;

    protected Map<AttributeInfo.Kind, AttributeInfo> attributeInfoMap = new HashMap<>();
    
    // Key - data channel name
    private Map<String, WorkerDataChannelInfo> dataChannelInfoMap = new HashMap<>();

    private PackageInfo packageInfo;
    protected WorkerInfo defaultWorkerInfo;
    protected Map<String, WorkerInfo> workerInfoMap = new HashMap<>();
    
    public WorkerDataIndex paramWorkerIndex;
    public WorkerDataIndex retWorkerIndex;
    
    private NativeCallableUnit nativeCallableUnit;
    
    private WorkerSet workerSet = new WorkerSet();

    private BType resolveToSuperType(BType bType) {
        if (bType.getTag() == TypeTags.SINGLETON_TAG) {
            return ((BSingletonType) bType).superSetType;
        }
        return bType;
    }

    private WorkerDataIndex calculateWorkerDataIndex(BType[] retTypes) {
        WorkerDataIndex index = new WorkerDataIndex();
        index.retRegs = new int[retTypes.length];
        for (int i = 0; i < retTypes.length; i++) {
            BType retType = resolveToSuperType(retTypes[i]);
            switch (retType.getTag()) {
            case TypeTags.INT_TAG:
                index.retRegs[i] = index.longRegCount++;
                break;
            case TypeTags.FLOAT_TAG:
                index.retRegs[i] = index.doubleRegCount++;
                break;
            case TypeTags.STRING_TAG:
                index.retRegs[i] = index.stringRegCount++;
                break;
            case TypeTags.BOOLEAN_TAG:
                index.retRegs[i] = index.intRegCount++;
                break;
            case TypeTags.BLOB_TAG:
                index.retRegs[i] = index.byteRegCount++;
                break;
            default:
                index.retRegs[i] = index.refRegCount++;
                break;
            }
        }
        return index;
    }
    
    public String getName() {
        return name;
    }

    public String getPkgPath() {
        return pkgPath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPackageCPIndex() {
        return pkgCPIndex;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public int getNameCPIndex() {
        return nameCPIndex;
    }

    public void setNameCPIndex(int nameCPIndex) {
        this.nameCPIndex = nameCPIndex;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public boolean isNative() {
        return isNative;
    }

    public void setNative(boolean aNative) {
        isNative = aNative;
    }

    public BType[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(BType[] paramTypes) {
        this.paramTypes = paramTypes;
        this.paramWorkerIndex = this.calculateWorkerDataIndex(this.paramTypes);
    }

    public BType[] getRetParamTypes() {
        return retParamTypes;
    }

    public void setRetParamTypes(BType[] retParamType) {
        this.retParamTypes = retParamType;
        this.retWorkerIndex = this.calculateWorkerDataIndex(this.retParamTypes);
    }

    public String getSignature() {
        if (signature != null) {
            return signature;
        }

        StringBuilder strBuilder = new StringBuilder("(");
        for (BType paramType : paramTypes) {
            strBuilder.append(paramType.getSig());
        }
        strBuilder.append(")(");

        for (BType retType : retParamTypes) {
            strBuilder.append(retType.getSig());
        }
        strBuilder.append(")");

        signature = strBuilder.toString();
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getSignatureCPIndex() {
        return signatureCPIndex;
    }

    public void setSignatureCPIndex(int signatureCPIndex) {
        this.signatureCPIndex = signatureCPIndex;
    }

    public WorkerInfo getDefaultWorkerInfo() {
        return defaultWorkerInfo;
    }

    public void setDefaultWorkerInfo(WorkerInfo defaultWorkerInfo) {
        this.defaultWorkerInfo = defaultWorkerInfo;
        this.pupulateWorkerSet();
    }

    public WorkerInfo getWorkerInfo(String workerName) {
        return workerInfoMap.get(workerName);
    }

    public void addWorkerInfo(String workerName, WorkerInfo workerInfo) {
        workerInfoMap.put(workerName, workerInfo);
        this.pupulateWorkerSet();
    }

    public Map<String, WorkerInfo> getWorkerInfoMap() {
        return workerInfoMap;
    }
    
    private void pupulateWorkerSet() {
        this.workerSet.generalWorkers = this.workerInfoMap.values().toArray(new WorkerInfo[0]);
        if (this.workerSet.generalWorkers.length == 0) {
            this.workerSet.generalWorkers = new WorkerInfo[] { this.getDefaultWorkerInfo() };
        } else {
            this.workerSet.initWorker = this.getDefaultWorkerInfo();
        }
    }
    
    public WorkerSet getWorkerSet() {
        return workerSet;
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

    @Deprecated
    public AnnAttachmentInfo getAnnotationAttachmentInfo(String packageName, String annotationName) {
        AnnotationAttributeInfo attributeInfo = (AnnotationAttributeInfo) getAttributeInfo(
                AttributeInfo.Kind.ANNOTATIONS_ATTRIBUTE);
        if (attributeInfo == null || packageName == null || annotationName == null) {
            return null;
        }
        for (AnnAttachmentInfo annotationInfo : attributeInfo.getAttachmentInfoEntries()) {
            if (packageName.equals(annotationInfo.getPkgPath()) && annotationName.equals(annotationInfo.getName())) {
                return annotationInfo;
            }
        }
        return null;
    }

    @Override
    public void addWorkerDataChannelInfo(WorkerDataChannelInfo workerDataChannelInfo) {
        dataChannelInfoMap.put(workerDataChannelInfo.getChannelName(), workerDataChannelInfo);
    }

    @Override
    public WorkerDataChannelInfo getWorkerDataChannelInfo(String name) {
        return dataChannelInfoMap.get(name);
    }

    @Override
    public WorkerDataChannelInfo[] getWorkerDataChannelInfo() {
        return dataChannelInfoMap.values().toArray(new WorkerDataChannelInfo[0]);
    }
    
    public NativeCallableUnit getNativeCallableUnit() {
        return nativeCallableUnit;
    }
    
    public void setNativeCallableUnit(NativeCallableUnit nativeCallableUnit) {
        this.nativeCallableUnit = nativeCallableUnit;
    }
    
    /**
     * This represents a worker set with different execution roles.
     */
    public static class WorkerSet {

        public WorkerInfo initWorker;

        public WorkerInfo[] generalWorkers;

    }
    
}
