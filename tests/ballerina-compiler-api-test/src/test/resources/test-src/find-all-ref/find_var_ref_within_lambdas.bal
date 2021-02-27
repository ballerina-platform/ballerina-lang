// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

string greeting = "Hello";

var fn = function () returns string {
    return greeting + " from a module level anon func";
};

function foo() {
    string name = "Pubudu";

    var fn = function () returns string {
        return greeting + " " + name;
    };

    var fn2 = function () returns string => greeting + " " + name;

    function (string) returns string fn3 = (lname) => "Hello " + name + " " + lname;
}

function testNestedLambdas() {
    int a = 10;

    var fn1 = function (int p1) returns int {
        int b = 20;

        var fn2 = function (int p2) returns int {
            return a + p1 + b + p2;
        };
    };
}

function testLambdaInParam() {
    int a = 10;

    var fn1 = function () {
        var fn2 = function (function (int) returns int p = function (int x = a) returns int => x * 2) {
            // do something
        };
    };
}
