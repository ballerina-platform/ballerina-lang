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
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import static org.ballerinalang.util.BLangCompilerConstants.TEST_VERSION;

/**
 * Native implementation of assertFalse(boolean condition, string message? = ()).
 *
 * @since 2.0.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.test", version = TEST_VERSION, functionName = "assertFalse",
        args = {@Argument(name = "value", type = TypeKind.BOOLEAN),
                @Argument(name = "message", type = TypeKind.UNION)},
        isPublic = true
)
public class AssertFalse {
    public static void assertFalse(Strand strand, boolean value, Object message) {
        if (value) {
            String msg = " expected a false value";
            msg = message != null ? message.toString() + msg : msg;
            strand.setProperty(NativeImpConstants.STRAND_PROPERTY_NAME,
                    BallerinaErrors.createError(NativeImpConstants.TEST_FAIL_REASON, msg));
            throw BallerinaErrors.createError(NativeImpConstants.TEST_FAIL_REASON, msg);
        }
    }
}
