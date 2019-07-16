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
type UserDefErrorTwo error<ERROR_REASON_ONE, TrxErrorData>;

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
    int...;
|};

type InvalidErrorOne error<int, map<any>>;
type InvalidErrorTwo error<string, boolean>;

const FLOAT = 1.0;
error<FLOAT, Foo> e1 = error(1.0, message = "string val", one = 1);

function testInvalidErrorTypeInFunc() {
    error<boolean> e = error(true);
}

type MyError error<string, MyErrorErrorData>;

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
