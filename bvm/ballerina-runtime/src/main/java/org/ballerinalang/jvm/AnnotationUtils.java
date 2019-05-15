/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.AnnotatableType;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.MapValueImpl;

import java.util.Arrays;

/**
 * Utility methods related to services and resources annotation processing.
 *
 * @since 0.995.0
 */
public class AnnotationUtils {

    public static void processObjectAnnotations(MapValueImpl globalValueMap, BObjectType objectType) {
        processAnnotations(globalValueMap, objectType);
        Arrays.stream(objectType.getAttachedFunctions()).forEach(function -> {
            processAnnotations(globalValueMap, function);
        });
    }

    private static void processAnnotations(MapValueImpl globalValueMap, AnnotatableType annotatableType) {
        if (!globalValueMap.containsKey(annotatableType.getAnnotationKey())) {
            return;
        }

        final Object map = globalValueMap.get(annotatableType.getAnnotationKey());

        if (map == null || TypeChecker.getType(map).getTag() != BTypes.typeMap.getTag()) {
            return;
        }

        MapValueImpl<String, Object> annotationMap = (MapValueImpl<String, Object>) map;
        for (String key : annotationMap.getKeys()) {
            final MapValueImpl<String, Object> annotationData = (MapValueImpl<String, Object>) annotationMap.get(key);
            final String annotationQName = key.split("\\$")[0];
            final String[] qNameParts = annotationQName.split(":");
            annotatableType.addAnnotation(qNameParts[0] + ":" + qNameParts[1], annotationData);
        }
    }
}
