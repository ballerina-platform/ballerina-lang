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

const EXPECTED_RETURN_VALUE_TO_BE_NIL_FAILURE_MESSAGE = "expected return value to be ()";
const EXPECTED_GLOBAL_INT_VAR_TO_BE_UPDATED_FAILURE_MESSAGE = "expected globalIntVar to have been updated";

// A function that would in other programming
// languages not return a value is represented in Ballerina by a function returning (). (Note
// that the function definition does not have to explicitly return (); a return statement or falling of
// the end of the function body will implicitly return ().)
const int CONST_INT_VAL = 1000;
int globalIntVar = 0;

@test:Config {}
function testNilReturningFunctionsWithExplicitReturn() {
    globalIntVar = 0;
    var res1 = nilRetFuncOne();
    test:assertEquals(res1, (), msg = EXPECTED_RETURN_VALUE_TO_BE_NIL_FAILURE_MESSAGE);
    test:assertEquals(globalIntVar, CONST_INT_VAL, msg = EXPECTED_GLOBAL_INT_VAR_TO_BE_UPDATED_FAILURE_MESSAGE);
}

@test:Config {}
function testNilReturningFunctionsWithExplicitReturnAndNoParanthesis() {
    globalIntVar = 0;
    var res2 = nilRetFuncTwo();
    test:assertEquals(res2, (), msg = EXPECTED_RETURN_VALUE_TO_BE_NIL_FAILURE_MESSAGE);
    test:assertEquals(globalIntVar, CONST_INT_VAL, msg = EXPECTED_GLOBAL_INT_VAR_TO_BE_UPDATED_FAILURE_MESSAGE);
}

@test:Config {}
function testNilReturningFunctionsWithNoExplicitReturn() {
    globalIntVar = 0;
    var res3 = nilRetFuncThree();
    test:assertEquals(res3, (), msg = EXPECTED_RETURN_VALUE_TO_BE_NIL_FAILURE_MESSAGE);
    test:assertEquals(globalIntVar, CONST_INT_VAL, msg = EXPECTED_GLOBAL_INT_VAR_TO_BE_UPDATED_FAILURE_MESSAGE);
}

# A nil returning function that has `return ();` to return.
function nilRetFuncOne() {
    updateGloalVar();
    return ();
}

# A nil returning function that has `return;` to return.
function nilRetFuncTwo() {
    updateGloalVar();
    return;
}

# A nil returning function that has no return statement.
function nilRetFuncThree() {
    updateGloalVar();
}

function updateGloalVar() {
    globalIntVar = CONST_INT_VAL;    
}
