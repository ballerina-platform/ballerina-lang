// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testCheckingExprWithNoErrorType() {
    testCheckingExprWithNoErrorType1();
    testCheckingExprWithNoErrorType2();
}

function testCheckingExprWithNoErrorType1() {
    if check true {
        return;
    }

    panic error("You are not supposed to reach this line");
}

type TRUE true;

type FALSE false;

function testCheckingExprWithNoErrorType2() {
    boolean b1 = true;
    error err = error("Error!");
    error err1 = check b1 ? err : err;
    assertEquality(err1.message(), "Error!");

    true b2 = true;
    error err2 = check b2 ? err : err;
    assertEquality(err2.message(), "Error!");

    boolean|true b3 = true;
    error err3 = check b3 ? err : err;
    assertEquality(err3.message(), "Error!");

    TRUE|FALSE b4 = true;
    error err4 = check b4 ? err : err;
    assertEquality(err4.message(), "Error!");
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(anydata actual, anydata expected) {
    if (actual == expected) {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
