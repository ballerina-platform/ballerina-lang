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

import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.scheduling.AsyncUtils;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.util.BLangConstants.MAP_LANG_LIB;
import static org.ballerinalang.util.BLangCompilerConstants.MAP_VERSION;

/**
 * Native implementation of lang.map:map(map&lt;Type&gt;, function).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.map", functionName = "map",
//        args = {@Argument(name = "m", type = TypeKind.MAP), @Argument(name = "func", type = TypeKind.FUNCTION)},
//        returnType = {@ReturnType(type = TypeKind.MAP)},
//        isPublic = true
//)
public class Map {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, MAP_LANG_LIB,
                                                                      MAP_VERSION, "map");

    public static BMap map(BMap<?, ?> m, BFunctionPointer<Object, Object> func) {
        MapType newMapType = TypeCreator.createMapType(((FunctionType) func.getType()).getReturnType());
        BMap<BString, Object> newMap = ValueCreator.createMapValue(newMapType);
        int size = m.size();
        AtomicInteger index = new AtomicInteger(-1);
        Strand parentStrand = Scheduler.getStrand();
        AsyncUtils
                .invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                                                       () -> new Object[]{parentStrand,
                                                               m.get(m.getKeys()[index.incrementAndGet()]), true},
                                                       result -> newMap
                                                               .put((BString) m.getKeys()[index.get()], result),
                                                       () -> newMap, Scheduler.getStrand().scheduler);
        return newMap;
    }
}
