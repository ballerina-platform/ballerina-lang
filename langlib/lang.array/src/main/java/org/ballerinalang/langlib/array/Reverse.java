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
import org.ballerinalang.langlib.array.utils.ArrayUtils;

import static org.ballerinalang.langlib.array.utils.ArrayUtils.createOpNotSupportedError;

/**
 * Native implementation of lang.array:reverse((any|error)[]).
 *
 * @since 1.0
 */
public class Reverse {

    public static BArray reverse(BArray arr) {
        Type arrType = TypeUtils.getReferredType(arr.getType());
        BArray reversedArr;
        switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG:
                reversedArr = ValueCreator.createArrayValue(TypeCreator.createArrayType(arr.getElementType()));
                break;
            case TypeTags.TUPLE_TAG:
                reversedArr = ArrayUtils.createEmptyArrayFromTuple(arr);
                break;
            default:
                throw createOpNotSupportedError(arrType, "reverse()");
        }
        for (int i = arr.size() - 1, j = 0; i >= 0; i--, j++) {
            reversedArr.add(j, arr.get(i));
        }
        return reversedArr;
    }

    private Reverse() {
    }
}
