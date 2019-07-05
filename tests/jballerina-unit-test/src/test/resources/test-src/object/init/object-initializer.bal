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

import initializers as inp;

public type person object {

    public int age = 0;
    public string name = "";
    public string address = "";

    function __init (int a = 10, string n = "Charles") {
        self.name = n;
        self.age = a;
    }

    function getAge();
};

function person.getAge() {
    self.age = 12;
}

function testObjectInitializerInSamePackage1() returns [int, string]{
    person p = new(n = "Peter");
    return [p.age, p.name];
}

function testObjectInitializerInAnotherPackage() returns [int, string]{
    inp:employee e = new("Peter");
    return [e.age, e.name];
}

type employee object {

    public int age = 0;
    public string name = "A";

    function __init (int a = 30, string name) {
        self.name = self.name + name;
        self.age = a;
    }
};

function testObjectInitializerOrder() returns [int, string]{
    employee p = new (a = 40, "B");
    return [p.age, p.name];
}

function testObjectInitializerUsedAsAFunction() returns [int, string, int, string] {
    person p = new(n = "Peter");
    int age1 = p.age;
    string name1 = p.name;
    p.age = 15;
    p.name = "Jack";

    p.__init(a = 20, n = "James");
    return [p.age, p.name, age1, name1];
}

type Person object {
    string name;
    int age;

    function __init() returns error? {
        self.name = check getError();
        self.age = 25;
    }
};

function getError() returns string|error {
    error e = error("failed to create Person object", f = "foo");
    return e;
}

function testErrorReturningInitializer() returns Person|error {
    Person|error p = new();
    return p;
}

function testReturnedValWithTypeGuard() returns string {
    Person|error p = new Person();

    if (p is error) {
        return "error";
    } else {
        return "Person";
    }
}

function testAssigningToVar() returns error? {
    var v = new Person();
    if (v is error) {
        return v;
    }
    return ();
}

function testTypeInitInReturn() returns Person|error {
    return new();
}

type Person2 object {
    string name;
    int age = 27;
    string profession;
    anydata[] misc;

    function __init(string name, string profession = "", anydata... misc) {
        self.name = name;
        self.profession = profession;
        self.misc = misc;
    }
};

function testInitializerWithRestArgs() returns Person2 {
    json adr = {city: "Colombo", country: "Sri Lanka"};
    Person2 p = new("Pubudu", profession = "Software Engineer", adr);
    return p;
}

type Person3 object {
    string name;
    int age;

    function __init(string name, int age) returns Err? {
        self.name = check getError2(100);
        self.age = 25;
    }
};

type ErrorDetails record {
    int id;
};

type Err error<string, ErrorDetails>;

function getError2(int errId) returns string|Err {
    Err e = error("Failed to create object", id = errId);
    return e;
}

function testCustomErrorReturn() returns [Person3|Err, Person3|error] {
    Person3|Err p1 = new("Pubudu", 27);
    Person3|error p2 = new("Pubudu", 27);
    return [p1, p2];
}

type Person4 object {
    string name;
    int age;

    function __init(boolean isFoo) returns FooErr|BarErr|() {
        self.name = check getMultipleErrors(isFoo);
        self.age = 25;
    }
};

type FooErrData record {
    string f;
};

type FooErr error<string, FooErrData>;

type BarErrData record {
    string b;
};

type BarErr error<string, BarErrData>;

function getMultipleErrors(boolean isFoo) returns string|FooErr|BarErr {
    if (isFoo) {
        FooErr e = error("Foo Error", f = "foo");
        return e;
    } else {
        BarErr e = error("Bar Error", b = "bar");
        return e;
    }
}

function testMultipleErrorReturn() returns [Person4|FooErr|BarErr, Person4|FooErr|BarErr] {
    Person4|FooErr|BarErr p1 = new(true);
    Person4|FooErr|BarErr p2 = new(false);
    return [p1, p2];
}
