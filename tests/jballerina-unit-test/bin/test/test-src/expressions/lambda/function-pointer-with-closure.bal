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

function testArrayFunctionInfer() returns string {
    string word = "";
    var concat2 = function (string value) {
        word = word + value;
    };
    function (string) lambda = (ss) => concat2(ss);
    lambda("abc");
    lambda("cde");
    return word;
}

function apply((function (int) returns int) f1, (function (int) returns any) f2) returns (function (int) returns any) {
    return function (int arg) returns any {
        return f2(f1(arg));
    };
}

public function invokeApplyFunction() returns any {
    function (int) returns int add1 = a => a + 1;
    function (int) returns int mult2 = a => a * 2;
    function (int) returns any composed = apply(add1, mult2);
    any res = composed(2);
    return res;
}
