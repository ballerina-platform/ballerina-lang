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
package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.MemberFunctionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.types.BMemberFunctionType;
import io.ballerina.runtime.internal.types.BObjectType;
import io.ballerina.runtime.internal.types.BServiceType;
import io.ballerina.runtime.internal.values.FPValue;
import io.ballerina.runtime.internal.values.MapValue;

/**
 * Utility methods related to annotation loading.
 *
 * @since 0.995.0
 */
public class AnnotationUtils {

    /**
     * Method to retrieve annotations of the type from the global annotation map and set it to the type.
     *
     * @param globalAnnotMap The global annotation map
     * @param bType          The type for which annotations need to be set
     */
    public static void processAnnotations(MapValue globalAnnotMap, Type bType, Strand strand) {
        if (!(bType instanceof BAnnotatableType)) {
            return;
        }

        BAnnotatableType type = (BAnnotatableType) bType;

        BString annotationKey = StringUtils.fromString(type.getAnnotationKey());
        if (globalAnnotMap.containsKey(annotationKey)) {
            type.setAnnotations((MapValue<BString, Object>) globalAnnotMap.get(annotationKey));
        }

        if (type.getTag() != TypeTags.OBJECT_TYPE_TAG) {
            return;
        }
        BObjectType objectType = (BObjectType) type;
        for (MemberFunctionType attachedFunction : objectType.getAttachedFunctions()) {
            annotationKey = StringUtils.fromString(attachedFunction.getAnnotationKey());
            if (globalAnnotMap.containsKey(annotationKey)) {
                ((BMemberFunctionType) attachedFunction).setAnnotations((MapValue<BString, Object>)
                                                                             globalAnnotMap.get(annotationKey));
            }
        }
    }

    public static void processObjectCtorAnnotations(MapValue globalAnnotMap, BObjectType bType,
                                                    Strand strand) {
        BString annotationKey = StringUtils.fromString(bType.getAnnotationKey());

        if (globalAnnotMap.containsKey(annotationKey)) {
            Object annot = globalAnnotMap.get(annotationKey);
            // If annotations are already set via desugard service-decl, skip.
            Object annotValue = ((FPValue) annot).call(new Object[]{strand});
            bType.setAnnotations((MapValue<BString, Object>) annotValue);
        }
        for (MemberFunctionType attachedFunction : bType.getAttachedFunctions()) {
            processObjectMethodLambdaAnnotation(globalAnnotMap, strand, attachedFunction);
        }
        if (bType instanceof BServiceType) {
            var serviceType = (BServiceType) bType;
            for (var resourceFunction : serviceType.getResourceFunctions()) {
                processObjectMethodLambdaAnnotation(globalAnnotMap, strand, resourceFunction);
            }
        }
    }

    private static void processObjectMethodLambdaAnnotation(MapValue globalAnnotMap, Strand strand,
                                                            MemberFunctionType attachedFunction) {
        BString annotationKey = StringUtils.fromString(attachedFunction.getAnnotationKey());

        if (globalAnnotMap.containsKey(annotationKey)) {
            ((BMemberFunctionType) attachedFunction)
                    .setAnnotations((MapValue<BString, Object>) ((FPValue) globalAnnotMap.get(annotationKey))
                            .call(new Object[]{strand}));
        }
    }

    /**
     * Method to retrieve annotations of a function type from the global annotation map and set it to the type.
     *
     * @param fpValue        The {@link FPValue} representing the function reference
     * @param globalAnnotMap The global annotation map
     * @param name           The function name that acts as the annotation key
     */
    public static void processFPValueAnnotations(FPValue fpValue, MapValue globalAnnotMap, String name) {
        BAnnotatableType type = (BAnnotatableType) fpValue.getType();
        BString nameKey = StringUtils.fromString(name);
        if (globalAnnotMap.containsKey(nameKey)) {
            type.setAnnotations((MapValue<BString, Object>) globalAnnotMap.get(nameKey));
        }
    }

    /**
     * Returns true if given {@link FPValue} is annotated to be run concurrently.
     *
     * @param fpValue function pointer to be invoked
     * @return true if should run concurrently
     */
    public static boolean isConcurrent(FPValue fpValue) {
        return fpValue.isConcurrent;
    }

    /**
     * Returns strand name of given {@link FPValue}.
     *
     * @param fpValue     function pointer to be invoked
     * @param defaultName default strand name
     * @return annotated strand name
     */
    public static String getStrandName(FPValue fpValue, String defaultName) {
        if (fpValue.strandName != null) {
            return fpValue.strandName;
        }
        return defaultName;
    }
}
