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
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.AsyncUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import org.ballerinalang.langlib.map.util.MapLibUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.MAP_LANG_LIB;
import static io.ballerina.runtime.internal.MapUtils.createOpNotSupportedError;
import static org.ballerinalang.util.BLangCompilerConstants.MAP_VERSION;

/**
 * Native implementation of lang.map:filter(map&lt;Type&gt;, function).
 *
 * @since 1.0
 */
public class Filter {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, MAP_LANG_LIB,
                                                                      MAP_VERSION, "filter");

    public static BMap filter(BMap<?, ?> m, BFunctionPointer<Object, Boolean> func) {
        Type mapType = TypeUtils.getReferredType(m.getType());
        Type constraint;
        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
                MapType type = (MapType) mapType;
                constraint = type.getConstrainedType();
                break;
            case TypeTags.RECORD_TYPE_TAG:
                constraint = MapLibUtils.getCommonTypeForRecordField((RecordType) mapType);
                break;
            default:
                throw createOpNotSupportedError(mapType, "filter()");
        }
        BMap<BString, Object> newMap = ValueCreator.createMapValue(TypeCreator.createMapType(constraint));
        int size = m.size();
        AtomicInteger index = new AtomicInteger(-1);
        Object[] keys = m.getKeys();
        AsyncUtils.invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                () -> new Object[]{m.get(keys[index.incrementAndGet()]), true},
                result -> {
                    if ((Boolean) result) {
                        Object key = keys[index.get()];
                        Object value = m.get(key);
                        newMap.put((BString) key, value);
                    }
                }, () -> newMap, Scheduler.getStrand().scheduler);
        return newMap;
    }
}
