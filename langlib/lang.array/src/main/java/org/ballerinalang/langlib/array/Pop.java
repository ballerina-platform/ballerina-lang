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
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.values.ArrayValue;

import static org.ballerinalang.langlib.array.utils.ArrayUtils.checkIsClosedArray;

/**
 * Native implementation of lang.array:pop((any|error)[]).
 *
 * @since 1.0
 */
public class Pop {

    private static final String FUNCTION_SIGNATURE = "pop()";

    public static Object pop(BArray arr) {
        Type type = TypeUtils.getImpliedType(arr.getType());
        if (type.getTag() == TypeTags.ARRAY_TAG) {
            checkIsClosedArray((ArrayType) type, FUNCTION_SIGNATURE);
        }
        return ((ArrayValue) arr).pop(arr.size() - 1);
    }

    private Pop() {}
}
