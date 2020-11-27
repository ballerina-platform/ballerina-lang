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

type Qux object {
    isolated function qux() returns int;
};

function testNonIsolatedMethodAsIsolatedMethodNegative() {
    object {
        int i;

        function qux() returns int;
    } obj = object {
        int i;

        function init() {
            self.i = 123;
        }

        function qux() returns int {
            return self.i;
        }
    };
    Qux q = obj;
}

function () returns int foo = () => 1;

type IsolatedFunction isolated function () returns int;

IsolatedFunction bar = foo;

int val = 2;

class Foo {
    isolated function func(int i) returns int => i;
}

isolated class Bar {
    function func() returns int => val;
}

class Baz {
    function func() returns int => val;
}

isolated class Quux {
    isolated function func(int i) returns int => i;
}

public function testNonIsolatedBoundMethods() {
    Foo foo = new;
    isolated function (int) returns int fooFunc = foo.func;

    Bar bar = new;
    isolated function () returns int barFunc = bar.func;

    Baz baz = new;
    isolated function () returns int bazFunc = baz.func;

    Foo|Quux fooQuux = new Quux();
    isolated function (int i) returns int fooQuuxFunc = fooQuux.func;
}
