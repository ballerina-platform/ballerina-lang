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

class Foo {
    public int age = 0;
    public string name = "";

    function init() {}

    function init() {}
}

class Bar {
   private function init() {}
}

class Person {
    string name;
    int age;

    function init() returns error? {
        self.name = check getError();
        self.age = 25;
    }
}

function getError() returns string|error {
    map<string> m = {f: "foo"};
    error e = error("failed to create Person object", f = "foo");
    return e;
}

function testInit() {
    Person p1 = new;
    Person p2 = new Person();
}

class Person2 {
    string name;

    function init() returns string? {
        self.name = "";
        return "foo";
    }
}

class Person3 {
    string name;

    function init() returns error {
        self.name = "";
        error e = error("failed to create Person3");
        return e;
    }
}

type FooErrData record {
    string f;
    string message?;
    error cause?;
};

type FooErr error<FooErrData>;

type BarErrData record {
    string b;
    string message?;
    error cause?;
};

type BarErr error<BarErrData>;

class Person4 {
    string name;

    function init() returns FooErr|BarErr {
        self.name = "";
        FooErr e = FooErr("Foo Error", f = "foo");
        return e;
    }
}

class Too {
    public function init() {
    }
    function name() {
        self.init(); // valid
    }
}

function callInitFunction() {
    Too t = new;
    t.init(); // invalid
}
