/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.BIntersectionType;

/**
 * This class contains the internal utility methods related to ballerina types.
 *
 * @since 2.0.0
 */
public class TypeUtils {

    private TypeUtils() {
    }

    /**
     * Provides the mutable type used to create the intersection type.
     * @param intersectionType intersection type
     * @return mutable type
     */
    public static Type getMutableType(BIntersectionType intersectionType) {
        for (Type type : intersectionType.getConstituentTypes()) {
            if (type != PredefinedTypes.TYPE_READONLY) {
                return type;
            }
        }
        return intersectionType.getEffectiveType();
    }

}
