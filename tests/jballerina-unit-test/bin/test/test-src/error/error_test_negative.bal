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

type ErrorReasons "reason one"|"reason two";

const ERROR_REASON_ONE = "reason one";
const ERROR_REASON_TWO = "reason two";

type UserDefErrorOne error;
type UserDefErrorTwo error;

function testInvalidErrorReasonWithUserDefinedReasonType() returns error {
    UserDefErrorOne e = error("");
    return e;
}

function testInvalidErrorReasonWithConstantAsReason() returns error {
    UserDefErrorTwo e = error(ERROR_REASON_TWO, message = "error detail message");
    return e;
}

type Foo record {|
    string message;
    error cause?;
    int...;
|};

type InvalidErrorOne error<map<any>>;
type InvalidErrorTwo error<boolean>;

const FLOAT = 1.0;
error<Foo> e1 = error("message", message = "string val", one = 1.0);

function testInvalidErrorTypeInFunc() {
    error<boolean> e = error(true);
}

type MyError error;

function testSelfReferencingErrorConstructor() {
    error e3 = error(e3.message(), e3);
    MyError e4 = error("reason", e4);
    UserDefErrorOne ue1 = error UserDefErrorOne();
    MyError me1 = error MyError();
}

type TrxErrorData record {|
    string message = "";
    error cause?;
    map<string> data = {};
|};

type MyErrorErrorData record {|
    string message = "";
    MyError cause?;
    map<string> data = {};
|};

type ER UserDefErrorOne|UserDefErrorTwo;

function contextuallyExpTypeIsAUnion() {
    ER e = error("OtherReason");
}

type Bee record {|
    string message?;
    boolean fatal;
    error cause?;
    anydata...;
|};

const R = "r";
const N = "r";
type RN R|N;
type RNStr R|N|string;

type BeeError error <Bee>;
type RNError error <Bee>;
type RNStrError error <Bee>;

function testIndirectErrorDestructuring() {
    BeeError e = error BeeError("Msg", fatal=false, other="k");
    RNError e2 = error RNError(message="Msg", fatal=false, other="k");
    RNStrError e3 = error RNStrError(message="Msg", fatal=false, other="k");
}

type TrxErrorData2 record {|
    string message = "";
    error cause?;
    map<string> data = {};
|};

const string reasonA = "ErrNo-1";
type UserDefErrorTwoA error<TrxErrorData2>;

public function errorReasonInference() returns [error, error] {
    UserDefErrorTwoA er1 = error(reasonA);
    map<string> data = {"arg1":"arg1-1", "arg2":"arg2-2"};
    UserDefErrorTwoA er2 = error(reasonA, message = "message", data = data);
    return [er1, er2];
}

function panicOnNonErrorMemberUnion() {
    error|int e = 5;
    panic e;
}

function errorDefinitionNegative() {
    error<record { string message?; error cause?; int i;}> e  = 1;
}

type Detail record {|
    int code;
|};

type CloseDetail record {|

|};

type OpenDetail record {

};

function errorDetailNegative() {
    error<Detail> err1 = error("Error!", code = 404); // valid
    error<string, Detail> err2 = error("Error!", code = 404); // invalid
    error<string> err3 = error("Error!"); // invalid
    error<int> err4 = error("Error!"); // invalid
    error<OpenDetail> err5 = error("Error!", id = 404); // valid
    error<CloseDetail> err6 = error("Error!", id = 404); // invalid
}
