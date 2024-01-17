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

int i;
string s;
function (int a) t;
Foo u;

function init() {
    Foo lf;
    lf.value();

    t(10);

    i = foo();

    u.value();

    s = bar();

    t(10);

    t = function (int a) {};

    getFoo().value();
    u = new;
}

function foo() returns int {
    return 0;
}

function bar() returns string {
    return "";
}

class Foo {
    function value() {
    }
}

function getFoo() returns Foo => new;
