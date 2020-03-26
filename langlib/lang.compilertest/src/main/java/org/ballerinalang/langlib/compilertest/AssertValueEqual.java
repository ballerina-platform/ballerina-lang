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

package org.ballerinalang.langlib.compilertest;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Native implementation of assertValueEqual(anydata expected, anydata actual).
 *
 * @since 2.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.compilertest", functionName = "assertValueEqual",
        args = {@Argument(name = "expected", type = TypeKind.ANYDATA),
                @Argument(name = "actual", type = TypeKind.ANYDATA)},
        isPublic = true
)
public class AssertValueEqual {
    public static void assertValueEqual(Strand strand, Object expected, Object actual) {
        if (TypeChecker.isEqual(expected, actual)) {
        } else {
            throw BallerinaErrors.createError("Not Equal");
        }
    }
}
