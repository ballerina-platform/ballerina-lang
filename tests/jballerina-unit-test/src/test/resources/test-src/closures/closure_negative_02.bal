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

function testTypeNarrowingWithClosure() {
    string? optionalName = "Ballerina";
    var lambdaFunc = function () {
        if optionalName == "Ballerina" {
            // Type of optionalName should not be narrowed since it's not a local var or param
            "Ballerina" bal = optionalName;
        }
    };
}

function testTypeNarrowingWithClosure2() returns int|string {
    int|string x = 5;
    var addFunc1 = function () {
        if (x is int && x > 3) {
            x = x + 1;
        } else {
            x = 0;
        }
    };
    return x;
}

function testBasicClosureWithInvalidTypeNarrowing() {
    int|string a = "32";
    var fn = function () {
        int b;
        if a is int {
            b = a;
        }
    };
}

function testMultiLevelClosureWithInvalidTypeNarrowing() {
    int|string a = "32";
    var fn1 = function() {
        int|boolean|error b = 32;
        var fn2 = function(int|string|boolean c) {
            int d;
            if a is int && b is int && c is int {
                d = a + b + c;
            }
        };
    };
}
