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

import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.scheduling.AsyncUtils;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.util.BLangConstants.TABLE_LANG_LIB;
import static org.ballerinalang.util.BLangCompilerConstants.TABLE_VERSION;

/**
 * Native implementation of lang.table:filter(table&lt;Type&gt;, function).
 *
 * @since 1.3.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.table", functionName = "filter",
//        args = {@Argument(name = "tbl", type = TypeKind.TABLE), @Argument(name = "func", type = TypeKind.FUNCTION)},
//        returnType = {@ReturnType(type = TypeKind.TABLE)},
//        isPublic = true
//)
public class Filter {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, TABLE_LANG_LIB,
                                                                      TABLE_VERSION, "filter");

    public static BTable filter(BTable tbl, BFunctionPointer<Object, Boolean> func) {
        Type newTableType = tbl.getType();
        BTable newTable = ValueCreator.createTableValue((TableType) newTableType);
        int size = tbl.size();
        AtomicInteger index = new AtomicInteger(-1);
        // accessing the parent strand here to use it with each iteration
        Strand parentStrand = Scheduler.getStrand();

        AsyncUtils
                .invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                        () -> new Object[]{parentStrand,
                                tbl.get(tbl.getKeys()[index.incrementAndGet()]), true},
                        result -> {
                            if ((Boolean) result) {
                                Object key = tbl.getKeys()[index.get()];
                                Object value = tbl.get(key);
                                newTable.put(key, value);
                            }
                        }, () -> newTable, Scheduler.getStrand().scheduler);
        return newTable;
    }

    public static BTable filter_bstring(Strand strand, BTable tbl, BFunctionPointer<Object, Boolean> func) {
        return filter(tbl, func);
    }
}
