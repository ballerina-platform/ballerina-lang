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

import org.ballerinalang.model.types.BType;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code CallableUnitInfo} contains common metadata of a Ballerina function/resource/action in the program file.
 *
 * @since 0.87
 */
public class CallableUnitInfo {

    protected String pkgPath;
    protected String name;
    protected boolean isNative;

    // Index to the PackageCPEntry
    protected int pkgCPIndex;
    protected int nameCPIndex;

    protected BType[] paramTypes;
    protected BType[] retParamTypes;

    protected CodeAttributeInfo codeAttributeInfo;

    protected Map<String, AttributeInfo> attributeInfoMap = new HashMap<>();

   // protected Map<String, WorkerInfo> workerInfoMap = new HashMap<>();

    private PackageInfo packageInfo;

    protected WorkerInfo defaultWorkerInfo;
    protected Map<String, WorkerInfo> workerInfoMap = new HashMap<>();

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
    }

    public BType[] getRetParamTypes() {
        return retParamTypes;
    }

    public void setRetParamTypes(BType[] retParamType) {
        this.retParamTypes = retParamType;
    }

    public WorkerInfo getDefaultWorkerInfo() {
        return defaultWorkerInfo;
    }

    public void setDefaultWorkerInfo(WorkerInfo defaultWorkerInfo) {
        this.defaultWorkerInfo = defaultWorkerInfo;
    }

    public WorkerInfo getWorkerInfo(String workerName) {
        return workerInfoMap.get(workerName);
    }

    public void addWorkerInfo(String attributeName, WorkerInfo workerInfo) {
        workerInfoMap.put(attributeName, workerInfo);
    }

    public Map<String, WorkerInfo> getWorkerInfoMap() {
        return workerInfoMap;
    }

    public CodeAttributeInfo getCodeAttributeInfo() {
        return codeAttributeInfo;
    }

    public AttributeInfo getAttributeInfo(String attributeName) {
        return attributeInfoMap.get(attributeName);
    }

    public void addAttributeInfo(String attributeName, AttributeInfo attributeInfo) {
        attributeInfoMap.put(attributeName, attributeInfo);
    }

//    public WorkerInfo getWorkerInfo(String workerName) {
//        return workerInfoMap.get(workerName);
//    }
//
//    public void addWorkerInfo(String attributeName, WorkerInfo workerInfo) {
//        workerInfoMap.put(attributeName, workerInfo);
//    }

    public AnnotationAttachmentInfo getAnnotationAttachmentInfo(String packageName, String annotationName) {
        AnnotationAttributeInfo attributeInfo = (AnnotationAttributeInfo) getAttributeInfo(
                AttributeInfo.ANNOTATIONS_ATTRIBUTE);
        if (attributeInfo == null || packageName == null || annotationName == null) {
            return null;
        }
        for (AnnotationAttachmentInfo annotationInfo : attributeInfo.getAnnotationAttachmentInfo()) {
            if (packageName.equals(annotationInfo.getPkgPath()) && annotationName.equals(annotationInfo.getName())) {
                return annotationInfo;
            }
        }
        return null;
    }
}
