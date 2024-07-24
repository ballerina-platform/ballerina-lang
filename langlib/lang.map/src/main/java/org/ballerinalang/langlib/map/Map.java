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

import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

/**
 * Native implementation of lang.map:map(map&lt;Type&gt;, function).
 *
 * @since 1.0
 */
public class Map {

    public static BMap<BString, ?> map(BMap<BString, ?> m, BFunctionPointer func) {
        MapType newMapType = TypeCreator.createMapType(((FunctionType) TypeUtils.getImpliedType(func.getType()))
                .getReturnType());
        BMap<BString, Object> newMap = ValueCreator.createMapValue(newMapType);
        int size = m.size();
        BString[] keys = m.getKeys();
        for (int i = 0; i < size; i++) {
            newMap.put(keys[i], func.call(m.get(keys[i])));
        }
        return newMap;
    }
}
