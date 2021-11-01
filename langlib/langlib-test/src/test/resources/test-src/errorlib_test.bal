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

public function getAnotherDetail(error e) returns readonly & AnotherDetail {
    return <AnotherDetail & readonly>e.detail();
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
    return e.stackTrace().map(x => x.toString());
}

public function testErrorStackTrace() returns [int, string] {
        error e = stack0();
        string[] ar = e.stackTrace().map(function (errorLib:StackFrame elem) returns string {
            return elem.toString();
        });
        return [e.stackTrace().length(), ar.toString()];
}

public function testErrorCallStack() {
    error e = error("error!");
    error:StackFrame[] stackTrace = e.stackTrace();

    any|error res = stackTrace;
    test:assertFalse(res is error);

    string s = "";
    if (res is error) {
        s = "error!";
    } else {
        s = res.toString();
    }
    test:assertValueEqual("[callableName: testErrorCallStack  fileName: errorlib_test.bal lineNumber: 105]", s);
}

public function testRetriableTest() {
    string s = "";
    error err = error error:Retriable("Custom error");
    if (err is error:Retriable) {
        s = err.message();
    }
    test:assertValueEqual("Custom error", s);
}

public type tempErrorDetail1 record {
    string? functionName;
};
public type tempErrorDetail3 record {
    string? functionName;
    string? codeName;
};
public type tempErrorDetail2 record {
    string? codeName;
};

public type Any A|B;
type Diff D|C;

public type A error<tempErrorDetail1> & distinct error;
public type B error<tempErrorDetail1> & distinct error;
public type C error<tempErrorDetail2> & distinct error;
public type D error<tempErrorDetail3> & distinct error;

function testErrorUnionDetailType() {
    Any? err1 = error A("ERROR", functionName="testFunction");
    test:assertValueEqual(false, err1 is B);
    if err1 is Any {
        tempErrorDetail1 detailRecord1 = err1.detail();
        string? stringFuncName= err1.detail().functionName;
        test:assertValueEqual("testFunction", detailRecord1.functionName);
    }

    Diff err2 = error C("ERROR", codeName="WTC_68");
    test:assertValueEqual(true, err2 is C);
    if err2 is Diff {
        tempErrorDetail2 detailRecord2 = err2.detail();
        string? stringCodeName =  err2.detail().codeName;
        test:assertValueEqual("WTC_68", detailRecord2.codeName);
    }
}
