/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.natives.mock;

import io.ballerina.runtime.api.Module;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;

/**
 * Constants related to mocking.
 */
public class MockConstants {

    public static final String DEFAULT_MOCK_OBJ_ANON = "$anonType$";
    public static final String FUNCTION_CALL_PLACEHOLDER = "__CALL__";
    public static final String FUNCTION_CALLORIGINAL_PLACEHOLDER = "__ORIGINAL__";
    public static final String MOCK_STRAND_NAME = "mock";

    // constants to represent error messages
    public static final Module TEST_PACKAGE_ID = new Module(BALLERINA_BUILTIN_PKG_PREFIX, "test", "0.0.0");
    public static final String FUNCTION_NOT_FOUND_ERROR = "FunctionNotFoundError";
    public static final String INVALID_MOCK_OBJECT_ERROR = "InvalidObjectError";
    public static final String FUNCTION_SIGNATURE_MISMATCH_ERROR = "FunctionSignatureMismatchError";
    public static final String INVALID_MEMBER_FIELD_ERROR = "InvalidMemberFieldError";

    public static final String FUNCTION_CALL_ERROR = "FunctionCallError";
}
