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

import ballerina/io;

type ClosedFoo record {|
    string s;
|};

function testErrorPattern1() returns string {
    any|error a = 13;
    match a {
        var error (var reason, ...var rest) => return "A";
        var error (var reason, detail = detail) => return "A"; // unreachable
    }

    match a {
        var error (var reason, detail = detail) => return "A";
        var error (var reason) => return "A"; // unreachable
    }
    return "Default";
}

function testErrorPattern2() returns string {
    error <string, ClosedFoo> err = error("Err Code 1", s = "error");

    match err {
        var error (var reason, ...var rest) => return "A";
        var error (var reason, detail = detail) => return "A"; // unreachable
    }

    match err {
        var error (var reason, detail = detail) => return "A";
        var error (var reason, detail = detail, ...var rest) => return "A"; // unknown field 'detail' in record type 'ClosedFoo'
        var error (var reason, s = s) => return "A"; // unreachable
    }
    return "Default";
}

function testErrorPattern3() returns string {
    error <string, ClosedFoo> err = error("Err Code 1", s = "error");

    match err {
        var error (var reason, ...var rest) => return "A";
        var error (var reason, s = _) => return "A"; // unreachable
    }

    match err {
        var error (var reason, ...var rest) => return "A";
        var error (var reason) => return "A"; // unreachable
    }
    return "Default";
}

type OpenedFoo record {
    string message;
};

function testErrorPattern5() returns string {
    error <string, OpenedFoo> err = error("Err Code 1", message = "error");
    any|error a = err;
    match a {
        var error (var reason, message = message, ...var rest) => return "A";
        var error (var reason, extra = extra) => return "A"; // unreachable
    }

    match a {
        var error (var reason, message = message, ...var rest) => return "A";
        var error (var reason, message = message) => return "A"; // unreachable
    }
    return "Default";
}
