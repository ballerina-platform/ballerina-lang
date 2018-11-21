// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type MyError error<string, map<string>>;

type Employee record {
    string name;
    int id;
};

type Lead record {
    string name;
    int id;
    float rating;
};

type Person record {
    string name;
};

type TableEmployee record {
    int id;
    string name;
    !...
};

type TableEmployeeTwo record {
    int id;
    string name;
    !...
};

type EmployeeObject object {
    string name;
    int id = 10000;

    new(name) {}

    function getName() returns string {
        return self.name;
    }
};

type LeadObject object {
    string name;
    int id = 10000;
    float rating = 100.0;

    new(name) {}

    function getName() returns string {
        return self.name;
    }
};

type PersonObject object {
    string name;

    new(name) {}

    function getName() returns string {
        return self.name;
    }
};

function testNilAssertionPositive() returns boolean {
    () a = ();
    string|int|boolean? b = a;
    json c = a;
    anydata d = a;
    any e = a;

    () f = <()> b;
    () g = <()> c;
    () h = <()> d;
    () i = <()> e;

    return a == f && f == g && h == g && h == i;
}

function testNilAssertionNegative() {
    string a = "hello world";
    anydata b = a;
    () c = <()> b;
}

function testNilValueAssertionAsSimpleBasicTypeNegative() {
    () a = ();
    anydata b = a;
    string c = <string> b;
}

function testNilValueAssertionAsStructuredTypeNegative() {
    () a = ();
    anydata b = a;
    map<string> c = <map<string>> b;
}

function testStringAssertionPositive() returns boolean {
    string a = "hello world";
    string|int|boolean b = a;
    json c = a;
    anydata d = a;
    any e = a;

    string f = <string> b;
    string g = <string> c;
    string h = <string> d;
    string i = <string> e;

    return a == f && f == g && h == g && h == i;
}

function testIntAssertionPositive() returns boolean {
    int a = 400;
    string|int|boolean b = a;
    json c = a;
    anydata d = a;
    any e = a;

    int f = <int> b;
    int g = <int> c;
    int h = <int> d;
    int i = <int> e;

    return a == f && f == g && h == g && h == i;
}

function testFloatAssertionPositive() returns boolean {
    float a = 40.12301;
    string|int|boolean|float b = a;
    json c = a;
    anydata d = a;
    any e = a;

    float f = <float> b;
    float g = <float> c;
    float h = <float> d;
    float i = <float> e;

    return a == f && f == g && h == g && h == i;
}

function testDecimalAssertionPositive() returns boolean {
    decimal a = 12340.124340414;
    string|int|boolean|decimal b = a;
    json c = a;
    anydata d = a;
    any e = a;

    decimal f = <decimal> b;
    decimal g = <decimal> c;
    decimal h = <decimal> d;
    decimal i = <decimal> e;

    return a == f && f == g && h == g && h == i;
}

function testBooleanAssertionPositive() returns boolean {
    boolean a = true;
    string|int|boolean|float b = a;
    json c = a;
    anydata d = a;
    any e = a;

    boolean f = <boolean> b;
    boolean g = <boolean> c;
    boolean h = <boolean> d;
    boolean i = <boolean> e;

    boolean assertionSuccessful = a == f && f == g && h == g && h == i;

    a = false;
    b = a;
    c = a;
    d = a;
    e = a;

    f = <boolean> b;
    g = <boolean> c;
    h = <boolean> d;
    i = <boolean> e;

    return assertionSuccessful && a == f && f == g && h == g && h == i;
}

function testArrayAssertionPositive() returns boolean {
    string[3] s = ["this is an array", "of length", "three"];
    any a = s;
    string[] s2 = <string[3]> a;

    (string|int)[3] s3 = ["this is an array", "of length", "three"];
    anydata b = s3;
    (string|int)[3] s4 = <(string|int)[3]> b;

    return s === s2 && s3 === s4;
}

function testArrayAssertionNegative() {
    (string|int)[2] s1 = ["this is an array", "of length"];
    anydata b = s1;
    any[2] s2 = <string[2]> b;
}

function testTupleAssertionPositive() returns boolean {
    (string, int, float) s = ("this is an array", 1, 3.0);
    any a = s;
    (string, int, float) s2 = <(string, int, float)> a;

    (string, int|string, float) s3 = ("this is an array", 1, 3.0);
    anydata a2 = s3;
    (string, int|string, float) s4 = <(string, int|string, float)> a2;

    return s === s2 && s3 === s4;
}

function testTupleAssertionNegative() {
    (string, int|string, float) s = ("this is an array", 1, 3.0);
    any a = s;
    (string, int|string, float) s2 = <(string, int, float)> a;
}

function testJsonAssertionPositive() returns boolean {
    json j = { jsonType: "object" };
    any a = j;
    json j2 = <json> a;

    anydata b = j;
    json j3 = <json> b;

    return j === j2 && j === j3;
}

function testJsonAssertionNegative() {
    json j = 5;
    any a = j;
    json j2 = <json> a;
}

function testMapAssertionPositive() returns boolean {
    map<string> m = { mapType: "constrained", elementType: "string" };
    anydata a = m;
    map m2 = <map<string>> a;

    map m3 = { mapType: "unconstrained", elementType: "any", elementCount: 3 };
    any a2 = m3;
    map m4 = <map> a2;

    return m === m2 && m3 === m4;
}

function testMapAssertionNegative() {
    map m1 = { mapType: "unconstrained", elementType: "any" };
    map<string> m2 = <map<string>> m1;
}

function testRecordAssertionPositive() returns boolean {
    Employee e = { name: "Em Zee", id: 1100 };
    Person p = e;
    Employee e2 = <Employee> p;
    return e === e2;
}

function testRecordAssertionNegative() {
    Employee e = { name: "Em Zee", id: 1100 };
    Person p = e;
    Lead e2 = <Lead> p;
}

//function testTableAssertionPositive() returns boolean {
//    table<TableEmployee> t1 = table {
//        { key id, name },
//        [
//            { 1, "Mary" },
//            { 2, "John" },
//            { 3, "Jim" }
//        ]
//    };
//    anydata a = t1;
//    table<TableEmployee> t2 = <table<TableEmployee>> a;
//    return t1 === t2;
//}
//
//function testTableAssertionNegative() {
//    table<TableEmployee> t1 = table {
//        { key id, name },
//        [
//            { 1, "Mary" },
//            { 2, "John" },
//            { 3, "Jim" }
//        ]
//    };
//    anydata a = t1;
//    table<TableEmployeeTwo> t2 = <table<TableEmployeeTwo>> a;
//}

function testXmlAssertionPositive() returns boolean {
    xml x1 = xml `<book>The Lost World</book>`;
    anydata a = x1;
    xml x2 = <xml> a;
    return x1 === x2;
}

function testXmlAssertionNegative() {
    string|xml x1 = "<book>The Lost World</book>";
    any a = x1;
    xml x2 = <xml> a;
}

//function testErrorAssertionPositive() returns boolean {
//    error e1 = error("test error");
//    anydata|error a = e1;
//    error e2 = <error> a;
//
//    MyError e3 = error("test my error");
//    any|error a2 = e3;
//    error e4 = <MyError> a2;
//    return e1 === e2 && e3 === e4;
//}
//
//function testErrorAssertionNegative() {
//    MyError e1 = error("test my error");
//    any|error e2 = e1;
//    error e3 = <error> e2;
//}

function testFunctionAssertionPositive() returns boolean {
    function (string, int) returns string f = testFunc;
    any a = f;
    function (string, int) returns string f1 = <function (string, int) returns string> a;
    return f === f1;
}

function testFunctionAssertionNegative() {
    function (string, int) returns string f = testFunc;
    any a = f;
    function (string) returns string f1 = <function (string) returns string> a;
}

//function testFutureAssertionPositive() returns boolean {
//    future<int> s1 = start testFutureFunc();
//    any a = s1;
//    future<int> s2 = <future<int>> a;
//    return s1 === s2;
//}
//
//function testFutureAssertionNegative() {
//    future<int> s1 = start testFutureFunc();
//    any a = s1;
//    future<json> s2 = <future<json>> a;
//}

function testObjectAssertionPositive() returns boolean {
    EmployeeObject e = new("Em Zee");
    PersonObject p = e;
    EmployeeObject e2 = <EmployeeObject> p;
    return e === e2;
}

function testObjectAssertionNegative() {
    EmployeeObject e = new("Em Zee");
    PersonObject p = e;
    LeadObject e2 = <LeadObject> p;
}

function testStreamAssertionPositive() returns boolean {
    stream<int> s1;
    any a = s1;
    stream s2 = <stream<int>> a;
    return s1 === s2;
}

function testStreamAssertionNegative() {
    stream<int> s1;
    any a = s1;
    stream s2 = <stream<json>> a;
}

function testTypedescAssertionPositive() returns boolean {
    typedesc t1 = int;
    any a = t1;
    typedesc t2 = <typedesc> a;
    return t1 === t2;
}

function testTypedescAssertionNegative() {
    typedesc t1 = int;
    any a = t1;
    int t2 = <int> a;
}

function testFunc(string s, int i) returns string {
    return <string> i + s;
}

function testFutureFunc() returns int {
    return 1;
}
