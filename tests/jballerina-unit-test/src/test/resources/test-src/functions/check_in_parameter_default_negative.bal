// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

isolated function intOrError() returns int|error => error("err!");

function f1(int a = check intOrError()) returns int => a;

function f2(any a = function (int intResult = check intOrError()) returns int|error {
    return intResult + 1;
}) returns any => a;

function (int a = check intOrError()) returns int f3 = a => a;

function f4() returns error? {
    var _ = function (int a = check intOrError()) returns int => a;

    function (string, int, int) _ =
        function (string s, int i = check intOrError(), int j = i + check intOrError()) {
        };

    record {|
        function fn = function (int v = check intOrError()) {};
    |} _ = {};

    function (int a = check intOrError()) returns int _ = a => a;
}

type Foo record {|
    function fm = function (int v = check intOrError()) {};
    function fn = function (int v = check intOrError(), record {| int x = check intOrError(); |} u = {}) {};
|};

class Bar {
    function fn = function (int v = check intOrError(), int[] w = [12, check intOrError()]) {};
}

isolated function intFunc() returns int => 1;

function f5(int a = check intFunc()) returns int => a;
