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

import ballerina/lang.value;

type StringRestRec record {|
    string...;
|};

type SMS error <record {| string message; error cause?; string detail; string extra?; |}>;
type SMA error <record {| string message; error cause?; boolean fatal; boolean|string detail?; boolean|string extra?; |}>;
type CMS error <record {| string message; error cause?; string detail; string extra?; |}>;
type CMA error <record {| string message; error cause?; anydata fatal; anydata detail?; anydata extra?; |}>;

const ERROR1 = "Some Error One";
const ERROR2 = "Some Error Two";

function testBasicErrorVariableWithMapDetails() returns [string, string, string, string, map<string|error>, string?,
                                                            string?, map<any|error>, any, any] {
    SMS err1 = error SMS("Error One", message = "Msg One", detail = "Detail Msg");
    SMA err2 = error SMA("Error Two", message = "Msg Two", fatal = true );

    string reason11;
    map<string|error> detail11;
    string reason12;
    string? message12;
    string? detail12;

    error (reason11, ... detail11) = err1;
    error (reason12, message = message12, detail = detail12) = err1;

    string reason21;
    map<any|error> detail21;
    string reason22;
    any message22;
    any fatal22;

    error (reason21, ... detail21) = err2;
    error (reason22, message = message22, fatal = fatal22) = err2;

    return [reason11, reason12, reason21, reason22, detail11, message12, detail12, detail21, message22,
    fatal22];
}

function testBasicErrorVariableWithConstAndMap() returns [string, string, string, string, map<string|error>, string?,
                                                             string?, map<any|error>, any, any, error?] {
    CMS err3 = error CMS(ERROR1, message = "Msg Three", detail = "Detail Msg");
    CMA err4 = error CMA(ERROR2, err3, message = "Msg Four", fatal = true);

    string reason31;
    map<string|error> detail31;
    string reason32;
    string? message32;
    string? detail32;

    error (reason31, ... detail31) = err3;
    error (reason32, message = message32, detail = detail32) = err3;

    string reason41;
    map<any|error> detail41;
    string reason42;
    any message42;
    any fatal42;
    error? cause;

    error (reason41, cause, ... detail41) = err4;
    error (reason42, message = message42, fatal = fatal42) = err4;

    return [reason31, reason32, reason41, reason42, detail31, message32, detail32, detail41, message42,
    fatal42, cause];
}

type Foo record {
    string message;
    boolean fatal;
    error cause?;
};

type FooError error <Foo>;

function testBasicErrorVariableWithRecordDetails() returns [string, string, string, boolean, map<anydata|error>] {
    FooError err1 = error FooError("Error One", message = "Something Wrong", fatal = true);
    FooError err2 = error FooError("Error One", message = "Something Wrong", fatal = true);

    string res1;
    map<anydata|error> mapRec;
    string res2;
    string message;
    boolean fatal;

    error (res1, ... mapRec) = err1;
    error (res2, message = message, fatal = fatal) = err2;

    return [res1, res2, message, fatal, mapRec];
}

function testErrorInTuple() returns [int, string, string, value:Cloneable, boolean] {
    Foo f = { message: "fooMsg", fatal: true };
    [int, string, error, [error, Foo]] t1 = [12, "Bal", error("Err", message = "Something Wrong"),
                                                            [error("Err2", message = "Something Wrong2"), f]];

    int intVar;
    string stringVar;
    error errorVar;
    error errorVar2;
    Foo fooVar;

    [intVar, stringVar, errorVar, [errorVar2, fooVar]] = t1;

    return [intVar, stringVar, errorVar.message(), errorVar2.detail()["message"], fooVar.fatal];
}

// function testErrorInTupleWithDestructure() returns [int, string, string, map<anydata|readonly>, boolean] {
//     [int, string, [error, boolean]] t1 = [12, "Bal", [error("Err2", message = "Something Wrong2"), true]];

//     int intVar;
//     string stringVar;
//     string reasonVar;
//     map<anydata|readonly> detailVar;
//     boolean booleanVar;

//     [intVar, stringVar, [error(reasonVar, ...detailVar), booleanVar]] = t1;

//     return [intVar, stringVar, reasonVar, detailVar, booleanVar];
// }

// function testErrorInTupleWithDestructure2() returns [int, string, string, anydata|readonly, boolean] {
//     [int, string, [error, boolean]] t1 = [12, "Bal", [error("Err2", message = "Something Wrong2"), true]];

//     int intVar;
//     string stringVar;
//     string reasonVar;
//     anydata|readonly message;
//     boolean booleanVar;

//     [intVar, stringVar, [error(reasonVar, message = message), booleanVar]] = t1;

//     return [intVar, stringVar, reasonVar, message, booleanVar];
// }

type Bar record {
    int x;
    error e;
};

// function testErrorInRecordWithDestructure() returns [int, string, anydata|readonly] {
//     Bar b = { x: 1000, e: error("Err3", message = "Something Wrong3") };

//     int x;
//     string reason;
//     map<anydata|readonly> detail;
//     { x, e: error (reason, ... detail) } = b;

//     return [x, reason, detail["message"]];
// }

// function testErrorInRecordWithDestructure2() returns [int, string, anydata|readonly, anydata|readonly] {
//     Bar b = { x: 1000, e: error("Err3", message = "Something Wrong3") };

//     int x;
//     string reason;
//     anydata|readonly message;
//     anydata|readonly extra;

//     { x, e: error (reason, message = message, extra = extra) } = b;

//     return [x, reason, message, extra];
// }

type StrError error<map<string|readonly>>;

function testErrorWithRestParam() returns map<string|readonly> {
    StrError errWithMap = error StrError("Error", message = "Fatal", fatal = "true");

    string reason;

    map<string|readonly> detailMap;
    error(reason, ...detailMap) = errWithMap;
    detailMap["extra"] = "extra";

    return detailMap;
}

function testErrorWithUnderscore() returns [string, map<string|readonly>] {
    StrError errWithMap = error StrError("Error", message = "Fatal", fatal = "true");

    string reason;
    map<string|readonly> detail;

    error(reason) = errWithMap;
    error(_, ... detail) = errWithMap;

    return [reason, detail];
}

type SampleError error;

function testDefaultErrorRefBindingPattern() returns string {
    SampleError e = error("the reason");
    string reason;
    error(reason) = e;
    return reason;
}

function testIndirectErrorRefBindingPattern() returns [value:Cloneable, boolean] {
    FooError e = error("the reason", message="msg", fatal = false);
    value:Cloneable message;
    boolean fatal;
    map<value:Cloneable> rest;
    error FooError(_, message=message, fatal=fatal, ...rest) = e;
    return [message, fatal];
}

const FILE_OPN = "FILE-OPEN";
type FileOpenErrorDetail record {|
    string message;
    error cause?;
    string targetFileName;
    int errorCode;
    int flags;
|};
type FileOpenError error<FileOpenErrorDetail>;

function testIndirectErrorRefMandatoryFields() {
    error causeErr = error("c");
    FileOpenError e = error FileOpenError(FILE_OPN, message="file open failed",
                                targetFileName="/usr/bhah/a.log",
                                errorCode=45221,
                                flags=128,
                                cause=causeErr);
    string message;
    string fileName;
    int errorCode;
    int flags;
    error  FileOpenError(_, message=message,
                    targetFileName=fileName,
                    errorCode=errorCode,
                    flags=flags) = e;

    error upcast = e;
    map<value:Cloneable> rest;
    error(_, ...rest) = upcast;

    string messageX;
    map<string|int|error> rest2;
    error(_, message=messageX, ...rest2) = e;

    assertEquality("file open failed", message);
    assertEquality("/usr/bhah/a.log", fileName);
    assertEquality(45221, errorCode);
    assertEquality(128, flags);
    assertEquality("file open failed", rest["message"]);
    assertEquality(causeErr, rest["cause"]);
    assertEquality("/usr/bhah/a.log", rest["targetFileName"]);
    assertEquality(45221, rest["errorCode"]);
    assertEquality(128, rest["flags"]);
    assertEquality("file open failed", messageX);
    assertEquality(causeErr, rest2["cause"]);
    assertEquality("/usr/bhah/a.log", rest2["targetFileName"]);
    assertEquality(45221, rest2["errorCode"]);
    assertEquality(128, rest2["flags"]);
}

public function testNoErrorReasonGiven() returns string? {
    FooError e = error("errorCode", message = "message", fatal = false);

    value:Cloneable message;
    anydata|error other;

    error(_, message = message) = e; // no simple-binding-pattern here
    return <string?>checkpanic message;
}

// public function testErrorDestructuringInATupleDestructuring() returns [string, anydata|readonly] {
//     string r;
//     anydata|readonly message;
//     int i;

//     error e = error("r2", message = "msg");
//     [i, error(r, message = message)] = [1, e];

//     return [r, message];
// }

// function testIndirectErrorVarRefInTuppleRef() returns [string?, string?, int] {
//     SMS err1 = error SMS("Error One", message = "Msg One", detail = "Detail Msg");

//     string? message;
//     string? detail;
//     int i;

//     [i, error SMS(_, message = message, detail = detail)] = [1, err1];
//     return [message, detail, i];
// }

// public function testErrorRefAndCtorInSameStatement() returns [string, anydata|readonly, int] {
//     string r;
//     anydata|readonly message;
//     int i;

//     [i, error(r, message = message)] = [1, error("r2", message = "Detail Msg")];
//     return [r, message, i];
// }

type ReadOnlyIntersectionError readonly & error<record { string[2] info; }>;

// function testErrorDestructureWithErrorDeclaredWithReadOnlyIntersection() {
//     ReadOnlyIntersectionError e = error("Sample Error", info = ["Detail Info 1", "Detail Info 2"]);
//     string message;
//     error? cause;
//     string info1;
//     string info2;
//     error ReadOnlyIntersectionError (message, cause, info = [info1, info2]) = e;
//     assertEquality(e.message(), message);
//     assertTrue(cause is ());
//     assertEquality("Detail Info 1", info1);
//     assertEquality("Detail Info 2", info2);
// }

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error("expected '" + expectedValAsString + "', found '" + actualValAsString + "'");

}
