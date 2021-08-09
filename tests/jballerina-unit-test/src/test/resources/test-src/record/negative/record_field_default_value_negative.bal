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

type Foo record {|
    function a = function () returns error? {
        // OK, since it is enclosed in the function, and not directly used in the default value.
        int q = check int:fromString("invalid");
        return;
    };
    int b = check int:fromString("invalid"); // error.
    int[]|error c = check f1(); // error.
    string[] d = ["", check f3()]; // error.
|};

type MyError distinct error;

isolated function f1() returns int[]|error => [];

isolated function f2() returns MyError|record {|
        function a = function () returns error? {
            // OK, since it is enclosed in the function, and not directly used in the default value.
            int q = check int:fromString("invalid");
            record {|
                any x = check f1(); // error.
            |} r = {};
            return;
        };
        int b = check int:fromString("invalid"); // error.
        int c = 0;
    |} => {};

isolated function f3() returns string|error|MyError => "";

record {|
    any|error a = check f1(); // error.
    record {|
        function a = function () returns error? {
            // OK, since it is enclosed in the function, and not directly used in the default value.
            int q = check int:fromString("invalid");
            return;
        };
        int b = check int:fromString("invalid"); // error.
        int? c = check trap <int> <any> 1; // error.
    |} b = check f2(); // error.
    string c = check f3(); // error.
|} rec = {a: 1};

isolated function func() {
    record {|
        any|error a = check f1(); // error.
        record {|
            function a = function () returns error? {
                // OK, since it is enclosed in the function, and not directly used in the default value.
                int q = check int:fromString("invalid");
                return;
            };
            int b = check int:fromString("invalid"); // error.
            int c = check int:fromString(check f3()); // error.
        |} b = check f2(); // error.
        object {} rec2 = object {
            any|error a = check f1(); // error.
        };
    |} rec2 = {a: 1};
}

type Baz record {
    any a = check new Qux(function () returns error? { // error
                              // OK, since it is enclosed in the function, and not directly used in the default value.
                              int q = check int:fromString("invalid");
                              return;
                          });
    any b = check new Qux(() => check int:fromString("invalid")); // error for outer check
};

class Qux {
    isolated function init(function () returns int|error? f) returns error? {
        return;
    }
}
