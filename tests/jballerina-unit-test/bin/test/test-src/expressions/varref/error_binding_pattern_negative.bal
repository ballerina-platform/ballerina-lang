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

type FooError error<record {|string message?; string cause; boolean fatal?; string...;|}>;

FooError err = error("Error One", message = "Msg One", cause = "Cause One", fatal = false);

function testNonRequiredFieldBindingNegative() {
    FooError error(reason1, message = message1, cause = cause1, fatal = fatal1) = err;
    FooError error FooError(reason2, message = message2, cause = cause2, fatal = fatal2) = err;

    error error(reason3, message = message3, cause = cause3, fatal = fatal3) = err;
    error error FooError(reason4, message = message4, cause = cause4, fatal = fatal4) = err;

    var error(reason5, message = message5, cause = cause5, fatal = fatal5) = err;
    var error FooError(reason6, message = message6, cause = cause6, fatal = fatal6) = err;
}

function testUndefinedErrorDetailsNegative() {
    FooError error(reason1, msg = msg1, cause = cause1, extra = extra1) = err;
    FooError error FooError(reason2, msg = msg2, cause = cause2, extra = extra2) = err;

    error error(reason3, msg = msg3, cause = cause3, extra = extra3) = err;
    error error FooError(reason4, msg = msg4, cause = cause4, extra = extra4) = err;

    var error(reason5, msg = msg5, cause = cause5, extra = extra5) = err;
    var error FooError(reason6, msg = msg6, cause = cause6, extra = extra6) = err;
}

function testNestedNonRequiredFieldBinding() {
    [int, FooError] t = [12, error("Error One", message = "Msg One", cause = "Cause One", fatal = false)];

    int i;
    string m;
    any|error n;

    error(m, identifier = n) = t[1];
    [i, error(m, identifier = n)] = t;

    error(m, message = n) = t[1];
    [i, error(m, message = n)] = t;
}

type SampleErrorData record {|
    int code;
    string reason;
|};

type SampleError error<SampleErrorData>;

type TestRecord record {|
    int errorNum2;
    string errorString2;
    SampleError err;
|};

type errorData record {|
    int errNum;
    string errReason;
|};

type MyError error<errorData>;

function testWildcardBindingPatternWithErrorCause() {
    SampleError sampleErr = error("Transaction Failure", error("Database Error"), code = 20,
                            reason = "deadlock condition");

    var error(message1, _, code = code1, reason = reason1) = sampleErr;

    var error(_, _, code = code2, reason = reason2) = sampleErr;

    SampleError error(message3, _, code = code3, reason = reason3) = sampleErr;

    SampleError error(_, _, code = code4, reason = reason4) = sampleErr;

    [int, [string, SampleError]] [errorNum1, [errorString1, error(message5, _, code = code5, reason = reason5)]] = [1234, ["ERROR", sampleErr]];

    TestRecord testRecord = {errorNum2: 1223, errorString2: "ERROR", err: sampleErr};
    TestRecord {errorNum2: _, errorString2: firstName, err: error(message6, _, code = code6, reason = reason6)} = testRecord;

    MyError myErr = error("Illegal Return", sampleErr, errNum = 20, errReason = "empty content");
    var error(message7, error(message8, _, code = code8, reason = reason8), errNum = code7, errReason = reason7) = myErr;

    MyError error(message9, error(message10, _, code = code10, reason = reason10), errNum = code9, errReason = reason9) = myErr;
}
