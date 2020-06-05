/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.test;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import static org.ballerinalang.util.BLangCompilerConstants.TEST_VERSION;


/**
 * Native implementation of assertNotEqual(any|error actual, any|error expected, string message? = ()).
 *
 * @since 2.0.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.test", version = TEST_VERSION, functionName = "assertNotEquals",
        args = {@Argument(name = "actual", type = TypeKind.ANYDATA),
                @Argument(name = "expected", type = TypeKind.ANYDATA),
                @Argument(name = "message", type = TypeKind.UNION)},
        isPublic = true
)

public class AssertNotEquals {
    public static void assertNotEquals(Strand strand, Object actual, Object expected, Object message) {
        if (TypeChecker.isEqual(expected, actual)) {
            String msg = " expected the actual value not to be [" + expected + "]";
            msg = message != null ? message.toString() + msg : msg;
            strand.setProperty(NativeImpConstants.STRAND_PROPERTY_NAME,
                    BallerinaErrors.createError(NativeImpConstants.TEST_FAIL_REASON, msg));
            throw BallerinaErrors.createError(NativeImpConstants.TEST_FAIL_REASON, msg);
        }
    }
}
