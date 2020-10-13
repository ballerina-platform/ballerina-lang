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

import io.ballerina.jvm.api.BValueCreator;
import io.ballerina.jvm.api.values.BFunctionPointer;
import io.ballerina.jvm.api.values.BMap;
import io.ballerina.jvm.api.values.BString;
import io.ballerina.jvm.runtime.AsyncUtils;
import io.ballerina.jvm.scheduling.Scheduler;
import io.ballerina.jvm.scheduling.Strand;
import io.ballerina.jvm.scheduling.StrandMetadata;
import io.ballerina.jvm.types.BFunctionType;
import io.ballerina.jvm.types.BMapType;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.jvm.util.BLangConstants.MAP_LANG_LIB;
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
        BMapType newMapType = new BMapType(((BFunctionType) func.getType()).retType);
        BMap<BString, Object> newMap = BValueCreator.createMapValue(newMapType);
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
