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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFunctionPointer;
import org.ballerinalang.langlib.array.utils.ArrayUtils;

import static org.ballerinalang.langlib.array.utils.ArrayUtils.createOpNotSupportedError;

/**
 * Native implementation of lang.array:filter(Type[], function).
 *
 * @since 1.0
 */
public class Filter {

    public static BArray filter(BArray arr, BFunctionPointer func) {
        BArray newArr;
        Type arrType = TypeUtils.getImpliedType(arr.getType());
        newArr = switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG -> ValueCreator.createArrayValue(TypeCreator.createArrayType(arr.getElementType()));
            case TypeTags.TUPLE_TAG -> ArrayUtils.createEmptyArrayFromTuple(arr);
            default -> throw createOpNotSupportedError(arrType, "filter()");
        };
        int size = arr.size();
        int index = 0;
        for (int i = 0; i < size; i++) {
            Object value = arr.get(i);
            boolean isFiltered = (boolean) func.call(value);
             if (isFiltered) {
                 newArr.add(index++, value);
             }
        }
        return newArr;
    }

    private Filter() {
    }
}
