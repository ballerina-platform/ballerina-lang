// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// under the License.package internal;

import ballerina/test;

public class Window {
    public string v = "hello";
    public int a = 1;

    public function process() returns int {
        return 5 + self.a;
    }
}

public class LengthWindow {
    public string v = "hello1";
    public int a = 2;

    public function process() returns int {
        return 10 + self.a;
    }
}

public class SampleWindow {
    public string v = "hello2";
    public int a = 3;

    public function process() returns int {
        return 15 + self.a;
    }
}

function testObjectFunctionPointer() {
    Window win = new LengthWindow();
    (function () returns int) pointer = () => win.process();
    win = new SampleWindow();
    test:assertTrue(pointer() is int);
    test:assertEquals(pointer(), 18);
}

class ABC {
    function foo(int a, int b) returns int {
        return a + b;
    }

    function (int x, int y) returns (int) bar;

    function init() {
        self.bar = self.foo;
    }

    function baz() returns int {
        return self.bar(4, 7) - self.foo(2, 1);
    }
}

function testObjectFunctionPointerFieldAccess() {
    ABC a = new();
    test:assertEquals(a.baz(), 8);
}
