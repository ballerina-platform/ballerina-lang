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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.AnnotatableType;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BServiceType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.MapValue;

/**
 * Utility methods related to annotation loading.
 *
 * @since 0.995.0
 */
public class AnnotationUtils {

    /**
     * Method to retrieve annotations of the type from the global annotation map and set it to the type.
     *
     * @param globalAnnotMap    The global annotation map
     * @param bType             The type for which annotations need to be set
     */
    public static void processAnnotations(MapValue globalAnnotMap, BType bType) {
        if (!(bType instanceof AnnotatableType)) {
            return;
        }

        AnnotatableType type = (AnnotatableType) bType;
        String annotationKey = type.getAnnotationKey();
        if (globalAnnotMap.containsKey(annotationKey)) {
            type.setAnnotations((MapValue<String, Object>) globalAnnotMap.get(annotationKey));
        }

        if (type.getTag() == TypeTags.OBJECT_TYPE_TAG) {
            BObjectType objectType = (BObjectType) type;
            for (AttachedFunction attachedFunction : objectType.getAttachedFunctions()) {
                annotationKey = attachedFunction.getAnnotationKey();
                if (globalAnnotMap.containsKey(annotationKey)) {
                    attachedFunction.setAnnotations((MapValue<String, Object>) globalAnnotMap.get(annotationKey));
                }
            }
        }
    }

    public static void processServiceAnnotations(MapValue globalAnnotMap, BServiceType bType, Strand strand) {
        String annotationKey = bType.getAnnotationKey();
        if (globalAnnotMap.containsKey(annotationKey)) {
            bType.setAnnotations((MapValue<String, Object>)
                                         ((FPValue) globalAnnotMap.get(annotationKey)).call(new Object[]{strand}));
        }

        for (AttachedFunction attachedFunction : bType.getAttachedFunctions()) {
            annotationKey = attachedFunction.getAnnotationKey();
            if (globalAnnotMap.containsKey(annotationKey)) {
                attachedFunction.setAnnotations((MapValue<String, Object>)
                                                        ((FPValue) globalAnnotMap.get(annotationKey))
                                                                .call(new Object[]{strand}));
            }
        }
    }

    /**
     * Method to retrieve annotations of a function type from the global annotation map and set it to the type.
     *
     * @param fpValue           The {@link FPValue} representing the function reference
     * @param globalAnnotMap    The global annotation map
     * @param name              The function name that acts as the annotation key
     */
    public static void processFPValueAnnotations(FPValue fpValue, MapValue globalAnnotMap, String name) {
        AnnotatableType type = (AnnotatableType) fpValue.getType();
        if (globalAnnotMap.containsKey(name)) {
            type.setAnnotations((MapValue<String, Object>) globalAnnotMap.get(name));
        }
    }
}
