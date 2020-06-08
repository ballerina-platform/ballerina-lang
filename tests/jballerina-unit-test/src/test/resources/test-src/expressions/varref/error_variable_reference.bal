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

type StringRestRec record {|
    string...;
|};

type SMS error <record {| string message?; error cause?; string...; |}>;
type SMA error <record {| string message?; error cause?; string|boolean...; |}>;
type CMS error <record {| string message?; error cause?; string...; |}>;
type CMA error <record {| string message?; error cause?; anydata...; |}>;

const ERROR1 = "Some Error One";
const ERROR2 = "Some Error Two";

function testBasicErrorVariableWithMapDetails() returns [string, string, string, string, map<string|error>, string?,
                                                            string?, string?, map<any|error>, any, any, any] {
    SMS err1 = SMS("Error One", message = "Msg One", detail = "Detail Msg");
    SMA err2 = SMA("Error Two", message = "Msg Two", fatal = true );

    string reason11;
    map<string|error> detail11;
    string reason12;
    string? message12;
    string? detail12;
    string? extra12;

    error (reason11, ... detail11) = err1;
    error (reason12, message = message12, detail = detail12, extra = extra12) = err1;

    string reason21;
    map<any|error> detail21;
    string reason22;
    any message22;
    any detail22;
    any extra22;

    error (reason21, ... detail21) = err2;
    error (reason22, message = message22, detail = detail22, extra = extra22) = err2;

    return [reason11, reason12, reason21, reason22, detail11, message12, detail12, extra12, detail21, message22,
    detail22, extra22];
}

function testBasicErrorVariableWithConstAndMap() returns [string, string, string, string, map<string|error>, string?, string?,
                                                             string?, map<any|error>, any, any, any, error?] {
    CMS err3 = CMS(ERROR1, message = "Msg Three", detail = "Detail Msg");
    CMA err4 = CMA(ERROR2, err3, message = "Msg Four", fatal = true);

    string reason31;
    map<string|error> detail31;
    string reason32;
    string? message32;
    string? detail32;
    string? extra32;

    error (reason31, ... detail31) = err3;
    error (reason32, message = message32, detail = detail32, extra = extra32) = err3;

    string reason41;
    map<any|error> detail41;
    string reason42;
    any message42;
    any detail42;
    any extra42;
    error? cause;

    error (reason41, cause, ... detail41) = err4;
    error (reason42, message = message42, detail = detail42, extra = extra42) = err4;

    return [reason31, reason32, reason41, reason42, detail31, message32, detail32, extra32, detail41, message42,
    detail42, extra42, cause];
}

type Foo record {
    string message;
    boolean fatal;
    error cause?;
};

type FooError error <Foo>;

function testBasicErrorVariableWithRecordDetails() returns [string, string, string, boolean, map<anydata|error>] {
    FooError err1 = FooError("Error One", message = "Something Wrong", fatal = true);
    FooError err2 = FooError("Error One", message = "Something Wrong", fatal = true);

    string res1;
    map<anydata|error> mapRec;
    string res2;
    string message;
    boolean fatal;

    error (res1, ... mapRec) = err1;
    error (res2, message = message, fatal = fatal) = err2;

    return [res1, res2, message, fatal, mapRec];
}

function testErrorInTuple() returns [int, string, string, anydata|readonly, boolean] {
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

function testErrorInTupleWithDestructure() returns [int, string, string, map<anydata|readonly>, boolean] {
    [int, string, [error, boolean]] t1 = [12, "Bal", [error("Err2", message = "Something Wrong2"), true]];

    int intVar;
    string stringVar;
    string reasonVar;
    map<anydata|readonly> detailVar;
    boolean booleanVar;

    [intVar, stringVar, [error(reasonVar, ...detailVar), booleanVar]] = t1;

    return [intVar, stringVar, reasonVar, detailVar, booleanVar];
}

function testErrorInTupleWithDestructure2() returns [int, string, string, anydata|readonly, boolean] {
    [int, string, [error, boolean]] t1 = [12, "Bal", [error("Err2", message = "Something Wrong2"), true]];

    int intVar;
    string stringVar;
    string reasonVar;
    anydata|readonly message;
    boolean booleanVar;

    [intVar, stringVar, [error(reasonVar, message = message), booleanVar]] = t1;

    return [intVar, stringVar, reasonVar, message, booleanVar];
}

type Bar record {
    int x;
    error e;
};

function testErrorInRecordWithDestructure() returns [int, string, anydata|readonly] {
    Bar b = { x: 1000, e: error("Err3", message = "Something Wrong3") };

    int x;
    string reason;
    map<anydata|readonly> detail;
    { x, e: error (reason, ... detail) } = b;

    return [x, reason, detail["message"]];
}

function testErrorInRecordWithDestructure2() returns [int, string, anydata|readonly, anydata|readonly] {
    Bar b = { x: 1000, e: error("Err3", message = "Something Wrong3") };

    int x;
    string reason;
    anydata|readonly message;
    anydata|readonly extra;

    { x, e: error (reason, message = message, extra = extra) } = b;

    return [x, reason, message, extra];
}

type StrError error<map<string|readonly>>;

function testErrorWithRestParam() returns map<string|readonly> {
    StrError errWithMap = StrError("Error", message = "Fatal", fatal = "true");

    string reason;
    string|readonly message;
    map<string|readonly> detailMap;
    error(reason, message = message, ...detailMap) = errWithMap;
    detailMap["extra"] = "extra";

    return detailMap;
}

function testErrorWithUnderscore() returns [string, map<string|readonly>] {
    StrError errWithMap = StrError("Error", message = "Fatal", fatal = "true");

    string reason;
    map<string|readonly> detail;

    error(reason, ... _) = errWithMap;
    error(_, ... detail) = errWithMap;

    return [reason, detail];
}

type SampleError error;

function testDefaultErrorRefBindingPattern() returns string {
    SampleError e = error("the reason");
    string reason;
    error(reason, ... _) = e;
    return reason;
}

function testIndirectErrorRefBindingPattern() returns [anydata|readonly, any|readonly] {
    SampleError e = error("the reason", message="msg");
    anydata|readonly message;
    any|readonly other;
    map<anydata|readonly> rest;
    SampleError(_, message=message, other=other, ...rest) = e;
    return [message, other];
}

const FILE_OPN = "FILE-OPEN";
type FileOpenErrorDetail record {|
    string message;
    error cause?;
    string targetFileName;
    int errorCode;
    int flags?;
|};
type FileOpenError error<FileOpenErrorDetail>;

function testIndirectErrorRefMandatoryFields() returns
        [string, string, int, int?, map<anydata|readonly>,
            string, map<string|int|error>] {
    FileOpenError e = FileOpenError(FILE_OPN, message="file open failed",
                                targetFileName="/usr/bhah/a.log",
                                errorCode=45221,
                                flags=128,
                                cause=error("c"));
    string message;
    string fileName;
    int errorCode;
    int? flags;
    FileOpenError(_, message=message,
                    targetFileName=fileName,
                    errorCode=errorCode,
                    flags=flags) = e;

    error upcast = e;
    map<anydata|readonly> rest;
    error(_, ...rest) = upcast;

    string messageX;
    map<string|int|error> rest2;
    error(_, message=messageX, ...rest2) = e;

    return [message, fileName, errorCode, flags,
                rest, messageX, rest2];
}

public function testNoErrorReasonGiven() returns string? {
    error e = error("errorCode", message = "message");

    anydata|readonly message;
    anydata|error other;

    error(_, message = message) = e; // no simple-binding-pattern here
    return <string?>message;
}

public function testErrorDestructuringInATuppleDestructuring() returns [string, anydata|readonly] {
    string r;
    anydata|readonly message;
    int i;

    error e = error("r2", message = "msg");
    [i, error(r, message = message)] = [1, e];

    return [r, message];
}

function testIndirectErrorVarRefInTuppleRef() returns [string?, string?, int] {
    SMS err1 = SMS("Error One", message = "Msg One", detail = "Detail Msg");

    string? message;
    string? detail;
    int i;

    [i, SMS(_, message = message, detail = detail)] = [1, err1];
    return [message, detail, i];
}

public function testErrorRefAndCtorInSameStatement() returns [string, anydata|readonly, int] {
    string r;
    anydata|readonly message;
    int i;

    [i, error(r, message = message)] = [1, error("r2", message = "Detail Msg")];
    return [r, message, i];
}
