// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// helpers

class ErrorField {
    public error 'error;
    public int 'int;

    function init(error er, int value = 20) {
        self.'error = er;
        self.'int = value;
    }
}

type ErrorDataWithErrorField record {
    error 'error;
};

function getError() returns error {
    return error("Generated Error");
}

function functionWithErrorNamedDefaultArgument(error 'error = getError()) {
    assertEquality('error.message(), "Generated Error");
}

function functionWithErrorNamedIncludedParam(*ErrorDataWithErrorField errorData) {
    assertEquality(errorData.'error.message(), "Generated Error");
}

function functionWithErrorNamedRequiredParam(ErrorDataWithErrorField 'error) {
    assertEquality('error.'error.message(), "Generated Error");
}

function functionWithErrorNamedRestParam(ErrorDataWithErrorField... 'error) {
    assertEquality('error[0].'error.message(), "Generated Error");
}

type ErrorDataWithRestError record {|
    error...;
|};

// test cases

function testErrorAsObjectField() {
    error newError = error("bam", message = "new error");
    ErrorField p = new ErrorField(newError);
    assertEquality(p.'error.toString(), "error(\"bam\",message=\"new error\")");
    assertEquality(p.'error.message(), "bam");
    assertEquality(p.'error.detail()["message"], "new error");
    assertEquality(p.'int, 20);
}

function testErrorDataWithErrorField() {
    error newError = error("bam", message = "new error");
    ErrorDataWithErrorField ef = {'error: newError};
    assertEquality(ef.'error.message(), "bam");
    assertEquality(ef.'error.detail()["message"], "new error");
}

function testErrorConstructorWithErrorField() {
    error e = error("test message", 'error = error("error as a detail"), message = "new message");
    ErrorDataWithErrorField ef = {'error: e};
    assertEquality(ef.'error.message(), "test message");
    error errorInCtor = <error>ef.'error.detail()["error"];
    assertEquality(errorInCtor.message(), "error as a detail");
}

function testErrorNamedDefaultArgument() {
    functionWithErrorNamedDefaultArgument();
}

function testErrorNamedIncludedParam() {
    error newError = getError();
    ErrorDataWithErrorField er = {'error: newError};
    functionWithErrorNamedIncludedParam(er);
}

function testErrorNamedRequiredParam() {
    error newError = getError();
    ErrorDataWithErrorField er = {'error: newError};
    functionWithErrorNamedRequiredParam(er);
}

function testErrorNamedRestParam() {
    error newError = getError();
    ErrorDataWithErrorField er = {'error: newError};
    functionWithErrorNamedRestParam(er);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON, message =
    "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
