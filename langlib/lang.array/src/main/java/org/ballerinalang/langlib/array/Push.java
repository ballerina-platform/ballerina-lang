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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.jvm.values.utils.ArrayUtils.createOpNotSupportedError;
import static org.ballerinalang.util.BLangCompilerConstants.ARRAY_VERSION;

/**
 * Native implementation of lang.array:push((any|error)[], (any|error)...).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", version = ARRAY_VERSION, functionName = "push",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY), @Argument(name = "vals", type = TypeKind.ARRAY)},
        returnType = {@ReturnType(type = TypeKind.ANY)},
        isPublic = true
)
public class Push {

    private static final String FUNCTION_SIGNATURE = "push()";

    public static void push(Strand strand, ArrayValue arr, ArrayValue vals) {
        BType arrType = arr.getType();
        int nVals = vals.size();
        switch (arrType.getTag()) {
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                for (int i = arr.size(), j = 0; j < nVals; i++, j++) {
                    arr.add(i, vals.get(j));
                }
                break;
            default:
                throw createOpNotSupportedError(arrType, FUNCTION_SIGNATURE);
        }
    }
}
