/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.math.nativeimpl;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.stdlib.math.nativeimpl.Constant.DIVIDE_BY_ZERO_ERROR_MSG;
import static org.ballerinalang.stdlib.math.nativeimpl.Constant.MATH_ERROR_CODE;

/**
 * Extern function ballerina.math:floorMod.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "math",
        functionName = "floorMod",
        args = {@Argument(name = "a", type = TypeKind.INT),
                @Argument(name = "b", type = TypeKind.INT)},
        returnType = {@ReturnType(type = TypeKind.INT)},
        isPublic = true
)
public class FloorMod {

    public static Object floorMod(Strand strand, long a, long b) {
        try {
            return Math.floorMod(a, b);
        } catch (ArithmeticException ex) {
            return BallerinaErrors.createError(MATH_ERROR_CODE, DIVIDE_BY_ZERO_ERROR_MSG);
        }
    }
}
