/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.map;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.langlib.map.util.MapLibUtils;

import static io.ballerina.runtime.internal.MapUtils.createOpNotSupportedError;

/**
 * Native implementation of lang.map:filter(map&lt;Type&gt;, function).
 *
 * @since 1.0
 */
public class Filter {

    public static BMap<?, ?> filter(BMap<?, ?> m, BFunctionPointer func) {
        Type mapType = TypeUtils.getImpliedType(m.getType());
        Type constraint = switch (mapType.getTag()) {
            case TypeTags.MAP_TAG -> {
                MapType type = (MapType) mapType;
                yield type.getConstrainedType();
            }
            case TypeTags.RECORD_TYPE_TAG -> MapLibUtils.getCommonTypeForRecordField((RecordType) mapType);
            default -> throw createOpNotSupportedError(mapType, "filter()");
        };
        BMap<BString, Object> newMap = ValueCreator.createMapValue(TypeCreator.createMapType(constraint));
        int size = m.size();
        Object[] keys = m.getKeys();
        for (int i = 0; i < size; i++) {
            Object key = keys[i];
            boolean isFiltered = (boolean) func.call(m.get(keys[i]));
            if (isFiltered) {
                Object value = m.get(key);
                newMap.put((BString) key, value);
            }
        }
        return newMap;
    }
}
