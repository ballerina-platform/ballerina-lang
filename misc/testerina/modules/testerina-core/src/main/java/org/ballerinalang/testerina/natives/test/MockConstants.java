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
package org.ballerinalang.testerina.natives.test;

/**
 * Constants related to mocking.
 */
public class MockConstants {

    public static final String DEFAULT_MOCK_OBJ_ANON = "$anonType$";

    // constants to represent error messages
    public static final String FUNCTION_NOT_FOUND_ERROR = "{ballerina/test}FunctionNotFoundError";
    public static final String INVALID_MOCK_OBJECT_ERROR = "{ballerina/test}InvalidObjectError";
    public static final String FUNCTION_SIGNATURE_MISMATCH_ERROR = "{ballerina/test}FunctionSignatureMismatchError";
    public static final String INVALID_MEMBER_FIELD_ERROR = "{ballerina/test}InvalidMemberFieldError";

}
