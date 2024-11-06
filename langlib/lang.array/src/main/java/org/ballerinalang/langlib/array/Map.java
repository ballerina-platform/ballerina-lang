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

package org.ballerinalang.langlib.array;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFunctionPointer;
import org.ballerinalang.langlib.array.utils.GetFunction;

import static org.ballerinalang.langlib.array.utils.ArrayUtils.createOpNotSupportedError;

/**
 * Native implementation of lang.array:map(Type[]).
 *
 * @since 1.0
 */
public class Map {

    public static BArray map(Environment env, BArray arr, BFunctionPointer func) {
        Type elemType = ((FunctionType) TypeUtils.getImpliedType(func.getType())).getReturnType();
        ArrayType retArrType = TypeCreator.createArrayType(elemType);
        BArray retArr = ValueCreator.createArrayValue(retArrType);
        Type arrType = TypeUtils.getImpliedType(arr.getType());
        int size = arr.size();
        GetFunction getFn = switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG -> BArray::get;
            case TypeTags.TUPLE_TAG -> BArray::getRefValue;
            default -> throw createOpNotSupportedError(arrType, "map()");
        };
        for (int i = 0; i < size; i++) {
            retArr.add(i, func.call(env.getRuntime(), getFn.get(arr, i)));
        }
        return retArr;
    }
}
