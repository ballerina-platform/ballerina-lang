// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/test;

const EXPECTED_RETURN_TYPE_TO_BE_OF_EXPECTED_TYPE_FAILURE_MESSAGE = "expected return type to be of expected type";

// For return values, typing is straightforward: returns T means that the value returned by
// the function is always of type T.
@test:Config {}
function testFunctionReturnType() {
    any res = funcWithUnionReturnType("string");
    test:assertTrue(res is string|int, msg = EXPECTED_RETURN_TYPE_TO_BE_OF_EXPECTED_TYPE_FAILURE_MESSAGE);

    res = funcWithUnionReturnType("int");
    test:assertTrue(res is string|int, msg = EXPECTED_RETURN_TYPE_TO_BE_OF_EXPECTED_TYPE_FAILURE_MESSAGE);
}

type STRING_OR_INT "string"|"int";

function funcWithUnionReturnType(STRING_OR_INT s) returns string|int {
    match s {
        "string" => return "string";
        "int" => return 1;
    }
    return "won't reach here";
}
