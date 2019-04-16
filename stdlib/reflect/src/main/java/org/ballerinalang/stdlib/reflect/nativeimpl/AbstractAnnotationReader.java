/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.reflect.nativeimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.impl.ConnectorSPIModelHelper;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

/**
 * Common logic for reading Global level annotation.
 *
 * @since 0.965.0
 */
abstract class AbstractAnnotationReader extends BlockingNativeCallableUnit {

    private static final String PKG_INTERNAL = "ballerina/internal";
    private static final String PKG_REFELCT = "ballerina/reflect";
    private static final String STRUCT_ANNOTATION = "annotationData";
    static final String DOT = ".";

    BValue getAnnotationValue(Context context, String pkgPath, String key) {
        final BMap bMap = ConnectorSPIModelHelper.getAnnotationVariable(pkgPath, context.getProgramFile());
        return createAnnotationStructArray(context, bMap.get(key));
    }

    private BValueArray createAnnotationStructArray(Context context, BValue map) {
        final PackageInfo packageInfo = context.getProgramFile().getPackageInfo(PKG_REFELCT);
        final StructureTypeInfo structInfo = packageInfo.getStructInfo(STRUCT_ANNOTATION);
        BValueArray annotationArray = new BValueArray(structInfo.getType());
        if (map == null || map.getType().getTag() != BTypes.typeMap.getTag()) {
            return annotationArray;
        }
        BMap<String, BValue> annotationMap = (BMap<String, BValue>) map;
        long index = 0;
        for (String key : annotationMap.keys()) {
            final String annotationQName = key.split("\\$")[0];
            int endIndex = annotationQName.lastIndexOf(":");
            final String annotationName = annotationQName.substring(endIndex + 1);
            final String pkgQName = endIndex > -1 ? annotationQName.substring(0, endIndex) : "";
            final String[] pkgQNameParts = pkgQName.split(":");
            final String pkgVersion = pkgQNameParts.length > 1 ? pkgQNameParts[1] : "";
            final BMap<String, BValue> annotation =
                    BLangVMStructs.createBStruct(structInfo, annotationName, pkgQNameParts[0], pkgVersion,
                            annotationMap.get(key));
            annotationArray.add(index++, annotation);
        }
        return annotationArray;
    }
}
