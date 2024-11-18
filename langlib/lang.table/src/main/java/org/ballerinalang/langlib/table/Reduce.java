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
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BTable;

/**
 * Native implementation of lang.table:reduce(table&lt;Type&gt;, function).
 *
 * @since 1.3.0
 */
public final class Reduce {


    private Reduce() {
    }

    public static Object reduce(Environment env, BTable<?, ?> tbl, BFunctionPointer func, Object initial) {
        int size = tbl.values().size();
        Object[] values = tbl.values().toArray();
        for (int i = 0; i < size; i++) {
            initial = func.call(env.getRuntime(), initial, values[i]);
        }
        return initial;
    }
}
