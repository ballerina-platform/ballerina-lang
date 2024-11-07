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
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;

/**
 * Native implementation of lang.table:forEach(table&lt;Type&gt;, function).
 *
 * @since 1.3.0
 */
public class Foreach {

    public static void forEach(Environment env, BTable<BString, Object> tbl, BFunctionPointer func) {
        int size = tbl.size();
        Object[] values = tbl.values().toArray();
        for (int i = 0; i < size; i++) {
            func.call(env.getRuntime(), values[i]);
        }
    }
}
