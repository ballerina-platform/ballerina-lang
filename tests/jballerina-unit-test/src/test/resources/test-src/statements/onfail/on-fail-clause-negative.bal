// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testVariableScope() returns error?{
        do {
            int i = 0;
            fail getError();
        } on fail error e {
            string errMsg = "This is inner on-fail" + i.toString();
        }
}

function getError() returns error {
    error err = error("Custom Error");
    return err;
}

type SampleErrorData record {|
    int code;
    string reason;
|};

type SampleError error<SampleErrorData>;

type SampleComplexErrorData record {|
    int code;
    int[2] pos;
    record {string moreInfo;} infoDetails;
|};

type SampleComplexError error<SampleComplexErrorData>;

function testOnFailWithErrorBPHavingMismatchedTypes1() {
    do {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail error<record {string code;}> error(code = errCode) {
    }
}

function testOnFailWithErrorBPHavingMismatchedTypes2() {
    do {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail SampleComplexError error(code = errCode) {
    }
}

function testOnFailWithErrorBPHavingMismatchedTypes3() {
    do {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail int error(code = errCode) {
    }
}

function testOnFailWithErrorBPHavingMismatchedTypes4() {
    do {
        error<record {string code;}> errVar = error("error", code = "23");
        fail errVar;
    } on fail error<record {int code;}> error(code = errorCode) {
    }
}

function testOnFailWithErrorBPHavingMismatchedTypes5() {
    do {
        fail error("error!");
    } on fail anydata error(msg) {
    }
}

function testOnFailWithErrorBPHavingInvalidListBP1() {
    do {
        fail error("error!");
    } on fail [error] [err] {
    }
}

function testOnFailWithErrorBPHavingInvalidListBP2() {
    do {
        fail error("error!");
    } on fail error [err] {
    }
}

function testOnFailWithErrorBPHavingInvalidWildcardBP() {
    do {
        fail error("error!");
    } on fail error _ {
    }
}

function testOnFailWithErrorBPHavingInvalidMappingBP() {
    do {
        fail error("error!");
    } on fail error {failError: err} {
    }
}

function testOnFailWithMultipleErrors() {
    boolean isPositiveState = false;
    do {
        if isPositiveState {
            fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
        fail error SampleError("Transaction Failure", code = 50, reason = "deadlock condition");
    } on fail var error(msg) {
    }
}

type Error1 distinct error;
type Error2 distinct error;

function functionWithCheckpanicInDoClauseNegative() {
    do {
        _ = checkpanic fn();

        Error2? x = ();
        check x;

        fail error("Error");
    } on fail Error1 e {
    }
}

function fn() returns int|Error1 => 1;

function f1() {
    int? e = 10;
    do {
        fail e;
    } on fail var m {

    }
}

function f2() {
    int? e = 10;
    do {
        fail e;
    } on fail var error(m) {

    }
}

function f3() {
    do {
        fail e;
    } on fail var m {

    }
}

function f4() {
    do {
        fail e;
    } on fail error error(m) {

    }
}

function f5() {
    string m = "str";
    do {
        fail error(m);
    } on fail var error(m) {

    }
}
