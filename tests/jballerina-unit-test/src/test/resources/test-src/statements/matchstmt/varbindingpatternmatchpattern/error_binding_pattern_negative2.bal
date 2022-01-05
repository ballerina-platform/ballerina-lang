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

type FooError error<record {|string message?; string cause; boolean fatal?; string...; |}>;

FooError err = error("Error One", message = "Msg One", cause = "Cause One", fatal = false);

function testNonRequiredFieldBindingNegative() {
    FooError error(reason1, message = message1, cause = cause1, fatal = fatal1) = err;
    FooError error FooError(reason2, message = message2, cause = cause2, fatal = fatal2) = err;

    error error(reason3, message = message3, cause = cause3, fatal = fatal3) = err;
    error error FooError(reason4, message = message4, cause = cause4, fatal = fatal4) = err;

    var error(reason5, message = message5, cause = cause5, fatal = fatal5) = err;
    var error FooError(reason6, message = message6, cause = cause6, fatal = fatal6) = err;

    string? reason7;
    string? reason8;
    string? message7;
    string? message8;
    string? cause7;
    string? cause8;
    boolean? fatal7;
    boolean? fatal8;

    error(reason7, message = message7, cause = cause7, fatal = fatal7) = err;
    error FooError(reason8, message = message8, cause = cause8, fatal = fatal8) = err;
}

function testUndefinedErrorDetailsNegative() {
    FooError error(reason1, msg = msg1, cause = cause1, extra = extra1) = err;
    FooError error FooError(reason2, msg = msg2, cause = cause2, extra = extra2) = err;

    error error(reason3, msg = msg3, cause = cause3, extra = extra3) = err;
    error error FooError(reason4, msg = msg4, cause = cause4, extra = extra4) = err;

    var error(reason5, msg = msg5, cause = cause5, extra = extra5) = err;
    var error FooError(reason6, msg = msg6, cause = cause6, extra = extra6) = err;

    string? reason7;
    string? reason8;
    string? msg7;
    string? msg8;
    string? cause7;
    string? cause8;
    any? extra7;
    any? extra8;

    error(reason7, msg = msg7, cause = cause7, extra = extra7) = err;
    error FooError(reason8, msg = msg8, cause = cause8, extra = extra8) = err;
}
