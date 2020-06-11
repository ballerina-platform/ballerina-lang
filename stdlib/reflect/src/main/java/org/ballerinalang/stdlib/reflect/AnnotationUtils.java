/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.reflect;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;

/**
 * Utility class represents annotation related functionality.
 *
 * @since 1.0.0
 */
public class AnnotationUtils {

    /**
     * Returns resource annotation value.
     *
     * @param service service name.
     * @param resourceName resource name.
     * @param annot annotation name.
     * @return annotation value object.
     */
    public static Object externGetResourceAnnotations(ObjectValue service, BString resourceName, BString annot) {
        AttachedFunction[] functions = service.getType().getAttachedFunctions();

        for (AttachedFunction function : functions) {
            if (function.funcName.equals(resourceName.getValue())) {
                Object resourceAnnotation = function.getAnnotation(annot);
                if (resourceAnnotation instanceof String) {
                    return StringUtils.fromString((String) resourceAnnotation);
                }
                return resourceAnnotation;
            }
        }
        return null;
    }

    /**
     * Returns service annotation value.
     *
     * @param service service name.
     * @param annot annotation name.
     * @return annotation value object.
     */
    public static Object externGetServiceAnnotations(ObjectValue service, BString annot) {
        Object serviceAnnotation = service.getType().getAnnotation(annot);
        if (serviceAnnotation instanceof String) {
            return StringUtils.fromString((String) serviceAnnotation);
        }
        return serviceAnnotation;
    }
}
