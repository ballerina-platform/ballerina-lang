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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;

/**
 * Native implementation of lang.map:reduce(map&lt;Type&gt;, function, Type1).
 *
 * @since 1.0
 */
public final class Reduce {

    public static Object reduce(Environment env, BMap<?, ?> m, BFunctionPointer func, Object initial) {
        int size = m.values().size();
        Object[] keys = m.getKeys();
        for (int i = 0; i < size; i++) {
            initial = func.call(env.getRuntime(), initial, m.get(keys[i]));
        }
        return initial;
    }

    private Reduce() {
    }
}
