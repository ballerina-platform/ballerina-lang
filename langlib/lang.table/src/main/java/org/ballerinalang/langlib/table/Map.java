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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.scheduling.AsyncUtils;
import io.ballerina.runtime.internal.scheduling.Scheduler;

import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static io.ballerina.runtime.api.constants.RuntimeConstants.TABLE_LANG_LIB;
import static org.ballerinalang.util.BLangCompilerConstants.TABLE_VERSION;

/**
 * Native implementation of lang.table:map(table&lt;Type&gt;, function).
 *
 * @since 1.3.0
 */
public class Map {

    private static final StrandMetadata METADATA = new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, TABLE_LANG_LIB,
                                                                      TABLE_VERSION, "map");

    public static BTable map(BTable tbl, BFunctionPointer<Object, Object> func) {
        Type newConstraintType = ((FunctionType) TypeUtils.getReferredType(func.getType())).getReturnType();
        TableType tblType = (TableType) TypeUtils.getReferredType(tbl.getType());
        TableType newTableType =
                TypeCreator.createTableType(newConstraintType, PredefinedTypes.TYPE_NEVER, tblType.isReadOnly());

        BTable newTable = ValueCreator.createTableValue(newTableType);
        int size = tbl.size();
        Object[] tableValues = tbl.values().toArray();
        AtomicInteger index = new AtomicInteger(-1);
        AsyncUtils.invokeFunctionPointerAsyncIteratively(func, null, METADATA, size,
                () -> new Object[]{tableValues[index.incrementAndGet()], true}, newTable::add, () -> newTable,
                Scheduler.getStrand().scheduler);
        return newTable;
    }
}
