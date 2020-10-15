/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.table;

import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.scheduling.AsyncUtils;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.util.BLangConstants.TABLE_LANG_LIB;
import static org.ballerinalang.util.BLangCompilerConstants.TABLE_VERSION;

/**
 * Native implementation of lang.table:reduce(table&lt;Type&gt;, function).
 *
 * @since 1.3.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.table", functionName = "reduce",
//        args = {@Argument(name = "tbl", type = TypeKind.TABLE), @Argument(name = "func", type = TypeKind.FUNCTION),
//                @Argument(name = "initial", type = TypeKind.ANY)},
//        returnType = {@ReturnType(type = TypeKind.ANY)},
//        isPublic = true
//)
public class Reduce {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, TABLE_LANG_LIB,
                                                                      TABLE_VERSION, "reduce");

    public static Object reduce(BTable tbl, BFunctionPointer<Object, Object> func, Object initial) {
        int size = tbl.values().size();
        AtomicReference<Object> accum = new AtomicReference<>(initial);
        AtomicInteger index = new AtomicInteger(-1);
        Strand parentStrand = Scheduler.getStrand();
        AsyncUtils
                .invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                        () -> new Object[]{parentStrand, accum.get(), true,
                                tbl.get(tbl.getKeys()[index.incrementAndGet()]), true},
                                                       accum::set, accum::get, Scheduler.getStrand().scheduler);
        return accum.get();
    }

    public static Object reduce_bstring(Strand strand, BTable tbl,
                                        BFunctionPointer<Object, Object> func, Object initial) {
        return reduce(tbl, func, initial);
    }
}
