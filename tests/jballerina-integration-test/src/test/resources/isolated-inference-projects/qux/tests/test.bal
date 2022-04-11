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

import ballerina/test;

function fn2() {
}

@test:Config
function testFn1() {
    test:assertFalse(<any> fn1 is isolated function);
}

@test:Config
function testFn2() {
    test:assertTrue(<any> fn2 is isolated function);
}

class Baz {
    private int i = 1;

    function getI() returns int {
        lock {
            return self.i;
        }
    }
}

class Qux {
    private int i = 1;

    function getI() returns int => self.i;
}

@test:Config
function testFn3() {
    test:assertTrue(<any> new Foo() is isolated object {});
}

@test:Config
function testFn4() {
    test:assertFalse(<any> new Bar() is isolated object {});
}

@test:Config
function testFn5() {
    test:assertTrue(<any> new Baz() is isolated object {});
}

@test:Config
function testFn6() {
    test:assertFalse(<any> new Qux() is isolated object {});
}

int c = 1; // inferred isolated
int d = 1; // not inferred isolated

function fn6() {
    lock {
        c = 2;
    }
}

function fn7() {
    lock {
        d = 2;
    }
}

function fn8() {
    d = 3;
}

@test:Config
function testFn7() {
    test:assertTrue(<any> fn3 is isolated function);
}

@test:Config
function testFn8() {
    test:assertFalse(<any> fn4 is isolated function);
}

@test:Config
function testFn9() {
    test:assertFalse(<any> fn5 is isolated function);
}

@test:Config
function testFn10() {
    test:assertTrue(<any> fn6 is isolated function);
}

@test:Config
function testFn11() {
    test:assertFalse(<any> fn7 is isolated function);
}

@test:Config
function testFn12() {
    test:assertFalse(<any> fn8 is isolated function);
}
