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

import ballerina/lang.'error as errorLib;
import ballerina/lang.'value as valueLib;
import ballerina/lang.test as test;

type Detail record {|
    string message?;
    error cause?;
    valueLib:Cloneable...;
|};

public const CONNECTION_TIMED_OUT = "ConnectionTimedOut";
public type TimeOutError distinct error<Detail>;

public const GENERIC_ERROR = "GenericError";
public type GenericError distinct error<Detail>;

public type Error GenericError|TimeOutError;

function testTypeTestingErrorUnion() returns [string, map<valueLib:Cloneable>]? {
    Error? err = getError();

    if (err is Error) {
        map<valueLib:Cloneable> dt = err.detail();
        return [err.message(), dt];
    }
}

function getError() returns Error? {
    GenericError err = error GenericError(GENERIC_ERROR, message = "Test union of errors with type test");
    return err;
}


public type AnotherDetail record {
    string message;
    error cause?;
};

public const REASON_1 = "Reason1";
public type FirstError distinct error<AnotherDetail>;

public const REASON_2 = "Reason2";
public type SecondError distinct error<AnotherDetail>;

public type ErrorUnion FirstError|SecondError;

public function testPassingErrorUnionToFunction() returns AnotherDetail? {
    var result = funcFoo();
    if (result is error) {
        return getAnotherDetail(result);
    }
}

public function funcFoo() returns int|ErrorUnion {
    FirstError e = error FirstError(REASON_1, message = "Test passing error union to a function");
    return e;
}

public function getAnotherDetail(error e) returns AnotherDetail {
    return <AnotherDetail>e.detail();
}

function stack0() returns error {
    return stack1();
}

function stack1() returns error {
    return stack2();
}

function stack2() returns error {
    return <error>getError();
}

function getErrorStackTrace() returns any {
    error e = stack0();
    return e.stackTrace();
}

public function testErrorStackTrace() returns [int, string] {
        error e = stack0();
        string[] ar = e.stackTrace().callStack.map(function (errorLib:CallStackElement elem) returns string {
            return elem.callableName + ":" + elem.fileName;
        });
        return [e.stackTrace().callStack.length(), ar.toString()];
}

public function testErrorCallStack() {
    error e = error("error!");
    error:CallStack stackTrace = e.stackTrace();

    any|error res = stackTrace;
    test:assertFalse(res is error);

    string s = "";
    if (res is error) {
        s = "error!";
    } else {
        s = res.toString();
    }
    test:assertValueEqual("object lang.error:CallStack", s);
}

public function testRetriableTest() {
    string s = "";
    error err = error error:Retriable("Custom error");
    if (err is error:Retriable) {
        s = err.message();
    }
    test:assertValueEqual("Custom error", s);
}
