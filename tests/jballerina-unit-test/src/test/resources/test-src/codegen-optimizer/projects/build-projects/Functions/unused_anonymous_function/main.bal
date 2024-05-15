// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

public function main() {
    _ = foo(20);
    _ = bar();
    _ = baz();
}

function foo(int value) returns int {
    function (int) returns int increment_1 = x => x + 1;
    function (int, int) returns int add_1 = (x, y) => x + y;
    return increment_1(value);
}

function bar() returns (function (int) returns int) {
    function (int) returns int increment_2 = x => x + 1;
    function (int, int) returns int add_2 = (x, y) => x + y;
    return increment_2;
}

function baz() returns (function (int, int) returns int) {
    function (int) returns int increment_3 = x => x + 1;
    function (int) returns int increment_4 = x => x + 1;
    function (int, int) returns int add_3 = (x, y) => increment_3(x) - 1 + y;
    return add_3;
}
