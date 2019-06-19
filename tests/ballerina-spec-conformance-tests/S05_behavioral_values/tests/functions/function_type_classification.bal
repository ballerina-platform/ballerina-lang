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

const EXPECTED_FUNCTION_TO_BE_OF_EXPECTED_TYPES = "expected function to be of expected type";

// The type system classifies functions based only on the arguments they are declared to
// accept and the values they are declared to return. Other aspects of a function value, such as
// how the return value depends on the argument, or what the side-effects of calling the
// function are, are not considered as part of the function’s type.
@test:Config {}
function testFunctionTypeClassification() {
    any func1 = funcRetFloatOne;
    any func2 = funcRetFloatTwo;

    test:assertTrue(func1 is function (float f) returns float, msg = EXPECTED_FUNCTION_TO_BE_OF_EXPECTED_TYPES);
    test:assertTrue(func2 is function (float f) returns float, msg = EXPECTED_FUNCTION_TO_BE_OF_EXPECTED_TYPES);
}

float globalFloatVar = 0.0;

function funcRetFloatOne(float f1) returns float {
    return globalFloatVar + 1.0;
}

function funcRetFloatTwo(float fl) returns float {
    globalFloatVar = fl;
    return 0.0;
}
