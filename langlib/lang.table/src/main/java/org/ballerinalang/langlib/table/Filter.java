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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BTable;

/**
 * Native implementation of lang.table:filter(table&lt;Type&gt;, function).
 *
 * @since 1.3.0
 */
public final class Filter {

    private Filter() {
    }

    public static BTable<Object, Object> filter(Environment env, BTable<?, ?> tbl, BFunctionPointer func) {
        TableType tableType = (TableType) TypeUtils.getImpliedType(tbl.getType());
        BTable<Object, Object> newTable = (BTable<Object, Object>)
                ValueCreator.createTableValue(TypeCreator.createTableType(tableType.getConstrainedType(),
                        tableType.getFieldNames(), false));
        int size = tbl.size();
        Object[] keys = tbl.getKeys();
        for (int i = 0; i < size; i++) {
            Object key = keys[i];
            Object value = tbl.get(key);
            boolean isFiltered = (boolean) func.call(env.getRuntime(), value);
            if (isFiltered) {
                newTable.put(key, value);
            }
        }
        return newTable;
    }
}
