/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.debugadapter.runtime;

import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.values.MapValueImpl;

/**
 * Contains debugger runtime utility functions related to Ballerina runtime variables.
 *
 * @since 2.0.0
 */
@SuppressWarnings("unused")
public class VariableUtils {

    private static final String MAP_TYPE_TEMPLATE = "map<%s>";
    private static final String UNKNOWN = "unknown";

    /**
     * Extract map type with constraint from a given BMapValue.
     *
     * @param mapObject mapObject
     * @return map type with constraint from a given BMapValue
     */
    public static String getBMapType(Object mapObject) {
        if (!(mapObject instanceof MapValueImpl<?, ?> mapValue)) {
            return String.format(MAP_TYPE_TEMPLATE, UNKNOWN);
        }

        if (!(mapValue.getType() instanceof BMapType type)) {
            return String.format(MAP_TYPE_TEMPLATE, UNKNOWN);
        }

        return String.format(MAP_TYPE_TEMPLATE, type.getConstrainedType().toString());
    }
}
