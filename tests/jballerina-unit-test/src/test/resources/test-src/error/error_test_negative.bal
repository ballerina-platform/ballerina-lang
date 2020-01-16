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

type UserDefErrorOne error<ErrorReasons>;
type UserDefErrorTwo error<ERROR_REASON_ONE>;

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

type InvalidErrorOne error<int, map<any>>;
type InvalidErrorTwo error<string, boolean>;

const FLOAT = 1.0;
error<FLOAT, Foo> e1 = error(1.0, message = "string val", one = 1);

function testInvalidErrorTypeInFunc() {
    error<boolean> e = error(true);
}

type MyError error<string>;

function testSelfReferencingErrorConstructor() {
    error e3 = error(e3.reason(), cause = e3);
    MyError e4 = error("reason", cause = e4);
    UserDefErrorOne ue1 = UserDefErrorOne();
    MyError me1 = MyError();
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

type BeeError error <R, Bee>;
type RNError error <RN, Bee>;
type RNStrError error <RNStr, Bee>;

function testIndirectErrorDestructuring() {
    BeeError e = BeeError(message="Msg", fatal=false, other="k");
    RNError e2 = RNError(message="Msg", fatal=false, other="k");
    RNStrError e3 = RNStrError(message="Msg", fatal=false, other="k");
}

type TrxErrorData2 record {|
    string message = "";
    error cause?;
    map<string> data = {};
|};

const string reasonA = "ErrNo-1";
type UserDefErrorTwoA error<reasonA, TrxErrorData2>;

public function errorReasonInference() returns [error, error] {
    UserDefErrorTwoA er1 = error();
    map<string> data = {"arg1":"arg1-1", "arg2":"arg2-2"};
    UserDefErrorTwoA er2 = error(message = "message", data = data);
    return [er1, er2];
}

function panicOnNonErrorMemberUnion() {
    error|int e = 5;
    panic e;
}

function errorDefinitionNegative() {
    error<string, record { string message?; error cause?; int i;}> e  = 1;
}
