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

import io.ballerina.jvm.api.TypeTags;
import io.ballerina.jvm.api.types.Type;
import io.ballerina.jvm.runtime.AsyncUtils;
import io.ballerina.jvm.scheduling.Scheduler;
import io.ballerina.jvm.scheduling.Strand;
import io.ballerina.jvm.scheduling.StrandMetadata;
import io.ballerina.jvm.types.BMapType;
import io.ballerina.jvm.types.BRecordType;
import io.ballerina.jvm.values.FPValue;
import io.ballerina.jvm.values.MapValue;
import io.ballerina.jvm.values.MapValueImpl;
import org.ballerinalang.langlib.map.util.MapLibUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.jvm.MapUtils.createOpNotSupportedError;
import static io.ballerina.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.jvm.util.BLangConstants.MAP_LANG_LIB;
import static org.ballerinalang.util.BLangCompilerConstants.MAP_VERSION;

/**
 * Native implementation of lang.map:filter(map&lt;Type&gt;, function).
 *
 * @since 1.0
 */
public class Filter {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, MAP_LANG_LIB,
                                                                      MAP_VERSION, "filter");

    public static MapValue filter(MapValue<?, ?> m, FPValue<Object, Boolean> func) {
        Type mapType = m.getType();
        Type newMapType;
        switch (mapType.getTag()) {
            case TypeTags.MAP_TAG:
                newMapType = mapType;
                break;
            case TypeTags.RECORD_TYPE_TAG:
                Type newConstraint = MapLibUtils.getCommonTypeForRecordField((BRecordType) mapType);
                newMapType = new BMapType(newConstraint);
                break;
            default:
                throw createOpNotSupportedError(mapType, "filter()");
        }
        MapValue<Object, Object> newMap = new MapValueImpl<>(newMapType);
        int size = m.size();
        AtomicInteger index = new AtomicInteger(-1);
        Strand parentStrand = Scheduler.getStrand();
        AsyncUtils.invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                                                         () -> new Object[]{parentStrand,
                                                                 m.get(m.getKeys()[index.incrementAndGet()]), true},
                                                         result -> {
                                                             if ((Boolean) result) {
                                                                 Object key = m.getKeys()[index.get()];
                                                                 Object value = m.get(key);
                                                                 newMap.put(key, value);
                                                             }
                                                         }, () -> newMap, Scheduler.getStrand().scheduler);
        return newMap;
    }
}
