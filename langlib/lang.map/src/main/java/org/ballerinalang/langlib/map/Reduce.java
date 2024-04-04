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

import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.scheduling.AsyncUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.MAP_LANG_LIB;
import static org.ballerinalang.langlib.map.util.Constants.MAP_VERSION;

/**
 * Native implementation of lang.map:reduce(map&lt;Type&gt;, function, Type1).
 *
 * @since 1.0
 */
public class Reduce {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, MAP_LANG_LIB,
                                                                      MAP_VERSION, "reduce");

    public static Object reduce(BMap<?, ?> m, BFunctionPointer<Object, Object> func, Object initial) {
        int size = m.values().size();
        AtomicReference<Object> accum = new AtomicReference<>(initial);
        AtomicInteger index = new AtomicInteger(-1);
        Object[] keys = m.getKeys();
        AsyncUtils.invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                () -> new Object[]{accum.get(), true, m.get(keys[index.incrementAndGet()]), true}, accum::set,
                accum::get, Scheduler.getStrand().scheduler);
        return accum.get();
    }

    private Reduce() {
    }
}
