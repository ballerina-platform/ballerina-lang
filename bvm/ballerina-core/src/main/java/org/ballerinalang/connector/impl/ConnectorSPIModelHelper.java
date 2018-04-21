/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.connector.impl;

import org.ballerinalang.bre.bvm.GlobalMemoryArea;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.PackageVarInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;

import java.util.Arrays;

/**
 * {@code StructHelper} Utilities related to connector processing.
 *
 * @since 0.94
 */
public class ConnectorSPIModelHelper {

    private static final String ANNOTATION_DATA = "$annotation_data";

    public static ServiceImpl createService(ProgramFile programFile, ServiceInfo serviceInfo) {
        ServiceImpl service = new ServiceImpl(serviceInfo);
        processAnnotations(serviceInfo.getPackagePath(), programFile, service);
        Arrays.stream(serviceInfo.getResourceInfoEntries()).forEach(resourceInfo -> {
            ResourceImpl resource = new ResourceImpl(resourceInfo.getName(), resourceInfo);
            processAnnotations(resourceInfo.getPkgPath(), programFile, resource);
            service.addResource(resource.getName(), resource);
        });
        return service;
    }

    public static StructImpl createStruct(BStruct struct) {
        return new StructImpl(struct);
    }

    public static BMap getAnnotationVariable(String pkgPath, ProgramFile programFile) {
        PackageInfo packageInfo = programFile.getPackageInfo(pkgPath);
        PackageVarInfo annotationData = packageInfo.getPackageVarInfo(ANNOTATION_DATA);
        final GlobalMemoryArea globalMemArea = programFile.globalMemArea;
        return (BMap) globalMemArea.getRefField(packageInfo.pkgIndex, annotationData.getGlobalMemIndex());
    }

    private static void processAnnotations(String pkgPath, ProgramFile programFile, AnnotatableNode annotatableNode) {
        final BMap bMap = getAnnotationVariable(pkgPath, programFile);
        if (!bMap.hasKey(annotatableNode.getAnnotationEntryKey())) {
            return;
        }
        final BValue map = bMap.get(annotatableNode.getAnnotationEntryKey());
        if (map == null || map.getType().getTag() != BTypes.typeMap.getTag()) {
            return;
        }
        BMap<String, BValue> annotationMap = (BMap<String, BValue>) map;
        for (String key : annotationMap.keySet()) {
            final BStruct annotationData = (BStruct) annotationMap.get(key);
            StructImpl struct = null;
            if (annotationData != null) {
                struct = new StructImpl(annotationData);
            }
            final String annotaionQName = key.split("\\$")[0];
            final String[] qNameParts = annotaionQName.split(":");
            final AnnotationImpl annotation = new AnnotationImpl(qNameParts[1], qNameParts[0], struct);
            annotatableNode.addAnnotation(annotaionQName, annotation);
        }
    }
}
