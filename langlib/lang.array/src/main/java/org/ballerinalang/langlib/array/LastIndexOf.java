/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.array;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.utils.GetFunction;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.jvm.values.utils.ArrayUtils.getElementAccessFunction;
import static org.ballerinalang.util.BLangCompilerConstants.ARRAY_VERSION;

/**
 * Native implementation of lang.array:lastIndexOf((anydata|error)[], anydata|error, int).
 *
 * @since 1.2.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", version = ARRAY_VERSION, functionName = "lastIndexOf",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY), @Argument(name = "val", type = TypeKind.UNION),
                @Argument(name = "startIndex", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.UNION)},
        isPublic = true
)
public class LastIndexOf {

    public static Object lastIndexOf(Strand strand, ArrayValue arr, Object val, long startIndex) {
        BType arrType = arr.getType();
        int size = arr.size();
        GetFunction getFn = getElementAccessFunction(arrType, "lastIndexOf()");

        for (long i = startIndex; i >= 0; i--) {
            if (TypeChecker.isEqual(val, getFn.get(arr, i))) {
                return i;
            }
        }

        return null;
    }
}
