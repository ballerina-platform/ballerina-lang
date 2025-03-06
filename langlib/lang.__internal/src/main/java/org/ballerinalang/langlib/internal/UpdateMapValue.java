/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org)
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.MapValue;

/**
 * Native implementation of lang.internal:putAll(map, map).
 *
 * @since 2201.12.0
 */
public class UpdateMapValue {

    /**
     * Inserts all the keys and values from the source map to target map.
     * @param target map being updated
     * @param source map from which the keys and values are inserted into target
     */
    public static void putAll(MapValue<BString, Object> target, MapValue<BString, Object> source) {
        source.entrySet().forEach(entry -> target.put(entry.getKey(), entry.getValue()));
    }

    private UpdateMapValue() {

    }
}
