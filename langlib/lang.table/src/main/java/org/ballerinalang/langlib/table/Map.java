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

import io.ballerina.jvm.api.types.Type;
import io.ballerina.jvm.runtime.AsyncUtils;
import io.ballerina.jvm.scheduling.Scheduler;
import io.ballerina.jvm.scheduling.Strand;
import io.ballerina.jvm.scheduling.StrandMetadata;
import io.ballerina.jvm.types.BFunctionType;
import io.ballerina.jvm.types.BTableType;
import io.ballerina.jvm.values.FPValue;
import io.ballerina.jvm.values.TableValue;
import io.ballerina.jvm.values.TableValueImpl;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.jvm.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.jvm.util.BLangConstants.TABLE_LANG_LIB;
import static org.ballerinalang.util.BLangCompilerConstants.TABLE_VERSION;

/**
 * Native implementation of lang.table:map(table&lt;Type&gt;, function).
 *
 * @since 1.3.0
 */
public class Map {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, TABLE_LANG_LIB,
                                                                      TABLE_VERSION, "map");

    public static TableValueImpl map(TableValueImpl tbl, FPValue<Object, Object> func) {
        Type newConstraintType = ((BFunctionType) func.getType()).retType;
        BTableType tblType = (BTableType) tbl.getType();
        BTableType newTableType = new BTableType(newConstraintType, tblType.getFieldNames(), tblType.isReadOnly());

        TableValueImpl newTable = new TableValueImpl(newTableType);
        int size = tbl.size();
        AtomicInteger index = new AtomicInteger(-1);
        Strand parentStrand = Scheduler.getStrand();
        AsyncUtils
                .invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                        () -> new Object[]{parentStrand,
                                tbl.get(tbl.getKeys()[index.incrementAndGet()]), true},
                        result -> newTable
                                .put(tbl.getKeys()[index.get()], result),
                                                       () -> newTable, Scheduler.getStrand().scheduler);
        return newTable;
    }

    public static TableValue map_bstring(Strand strand, TableValueImpl tbl, FPValue<Object, Object> func) {
        return map(tbl, func);
    }
}
