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

const ERR_REASON = "error reason";

type MyError error;
type MyErrorTwo error<ErrorDetails>;

type ErrorDetails record {
   string message;
   error cause?;
};

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

type TableEmployee record {|
    readonly int id;
    string name;
|};

type TableEmployeeTwo record {|
    readonly boolean id;
    string name;
|};

class EmployeeObject {
    string name;
    int id = 10000;

    function init(string name) {
        self.name = name;
    }

    function getName() returns string {
        return self.name;
    }
}

class LeadObject {
    string name;
    int id = 10000;
    float rating = 100.0;

    function init(string name) {
        self.name = name;
    }

    function getName() returns string {
        return self.name;
    }
}

class PersonObject {
    string name;

    function init(string name) {
        self.name = name;
    }

    function getName() returns string {
        return self.name;
    }
}

function testNilCastPositive() returns boolean {
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

function testNilCastNegative() {
    string a = "hello world";
    anydata b = a;
    () c = <()> b;
}

function testNilValueCastAsSimpleBasicTypeNegative() {
    () a = ();
    anydata b = a;
    string c = <string> b;
}

function testNilValueCastAsStructuredTypeNegative() {
    () a = ();
    anydata b = a;
    map<string> c = <map<string>> b;
}

function testStringCastPositive() returns boolean {
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

function testIntCastPositive() returns boolean {
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

function testFloatCastPositive() returns boolean {
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

function testDecimalCastPositive() returns boolean {
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

function testBooleanCastPositive() returns boolean {
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

function testArrayCastPositive() returns boolean {
    string[3] s = ["this is an array", "of length", "three"];
    any a = s;
    string[] s2 = <string[3]> a;

    (string|int)?[3] s3 = ["this is an array", "of length", "three"];
    anydata b = s3;
    (string|int)?[3] s4 = <(string|int)?[3]> b;

    return s === s2 && s3 === s4;
}

function testArrayCastNegative() {
    (string|int)?[2] s1 = ["this is an array", "of length"];
    anydata b = s1;
    any[2] s2 = <string[2]> b;
}

function testTupleCastPositive() returns boolean {
    [string, int, float] s = ["this is an array", 1, 3.0];
    any a = s;
    [string, int, float] s2 = <[string, int, float]> a;

    [string, int|string, float] s3 = ["this is an array", 1, 3.0];
    anydata a2 = s3;
    [string, int|string, float] s4 = <[string, int|string, float]> a2;

    return s === s2 && s3 === s4;
}

function testTupleCastNegative() {
    [string, int|string, float] s = ["this is an array", 1, 3.0];
    any a = s;
    [string, int|string, float] s2 = <[string, int, float]> a;
}

function testJsonCastPositive() returns boolean {
    json j = { jsonType: "object" };
    any a = j;
    json j2 = <json> a;

    anydata b = j;
    json j3 = <json> b;

    return j === j2 && j === j3;
}

function testJsonCastNegative() {
    xml x = xml `text`;
    any a = x;
    json j = <json> a;
}

function testMapCastPositive() returns boolean {
    map<string> m = { mapType: "constrained", elementType: "string" };
    anydata a = m;
    map<any> m2 = <map<string>> a;

    map<string|int> m3 = { mapType: "unconstrained", elementType: "any", elementCount: 3 };
    map<string|int|float> m4 = <map<string|int|float>> m3;

    any a2 = m3;
    map<any> m5 = <map<any>> a2;

    return m === m2 && m3 === m4 && m3 === m5;
}

function testMapCastNegative() {
    map<any> m1 = { mapType: "unconstrained", elementType: "any" };
    map<string> m2 = <map<string>> m1;
}

function testRecordCastPositive() returns boolean {
    Employee e = { name: "Em Zee", id: 1100 };
    Person p = e;
    Employee e2 = <Employee> p;
    Person p2 = <Person> e;
    return e === e2 && e2 == p2;
}

function testRecordCastNegative() {
    Employee e = { name: "Em Zee", id: 1100 };
    Person p = e;
    Lead e2 = <Lead> p;
}

function testTableCastPositive() returns boolean {
    table<TableEmployee> t1 = table key(id) [
            { id: 1, name: "Mary" },
            { id: 2, name: "John" },
            { id: 3, name: "Jim" }
        ];

    anydata a = t1;
    table<TableEmployee> t2 = <table<TableEmployee>> a;
    return t1 === t2;
}

function testTableCastNegative() {
    table<TableEmployee> t1 = table key(id)[
            { id: 1, name: "Mary" },
            { id: 2, name: "John" },
            { id: 3, name: "Jim" }
        ];

    anydata a = t1;
    table<TableEmployeeTwo> t2 = <table<TableEmployeeTwo>> a;
}

function testXmlCastPositive() returns boolean {
    xml x1 = xml `<book>The Lost World</book>`;
    anydata a = x1;
    xml x2 = <xml> a;
    return x1 === x2;
}

function testXmlCastNegative() {
    string|xml x1 = "<book>The Lost World</book>";
    any a = x1;
    xml x2 = <xml> a;
}

function testErrorCastPositive() returns boolean {
    error e1 = error("test error");
    anydata|error a = e1;
    error e2 = <error> a;

    MyError e3 = error("test my error");
    any|error a2 = e3;
    error e4 = <MyError> a2;

    MyErrorTwo e5 = MyErrorTwo(ERR_REASON, message = "error message");
    a2 = e5;
    MyErrorTwo e6 = <MyErrorTwo> a2;
    error e7 = <error> a2;

    return e1 === e2 && e3 === e4 && e5 === e6 && e5 === e7;
}

function testErrorCastNegative() {
    error e1 = error("test my error");
    any|error e2 = e1;
    MyErrorTwo e3 = <MyErrorTwo> e2;
}

function testFunctionCastPositive() returns boolean {
    function (string, int) returns string f = testFunc;
    any a = f;
    function (string, int) returns string f1 = <function (string, int) returns string> a;
    return f === f1;
}

function testFunctionCastNegative() {
    function (string, int) returns string f = testFunc;
    any a = f;
    function (string) returns string f1 = <function (string) returns string> a;
}

function testFutureCastPositive() returns boolean {
    future<int> s1 = start testFutureFunc();
    any a = s1;
    future<int> s2 = <future<int>> a;
    return s1 === s2;
}

function testFutureCastNegative() {
    future<int> s1 = start testFutureFunc();
    any a = s1;
    future<json> s2 = <future<json>> a;
}

function testObjectCastPositive() returns boolean {
    EmployeeObject e = new("Em Zee");
    PersonObject p = e;
    EmployeeObject e2 = <EmployeeObject> p;
    return e === e2;
}

function testObjectCastNegative() {
    EmployeeObject e = new("Em Zee");
    PersonObject p = e;
    LeadObject e2 = <LeadObject> p;
}

function testTypedescCastPositive() returns boolean {
    typedesc<int> t1 = int;
    any a = t1;
    typedesc<int> t2 = <typedesc<int>> a;
    return t1 === t2;
}

function testTypedescCastNegative() {
    typedesc<int> t1 = int;
    any a = t1;
    int t2 = <int> a;
}

function testMapElementCastPositive() returns boolean {
    Employee e1 = { name: "Anne", id: 12495673 };
    EmployeeObject e2 = new("John");
    int iVal = 12345;
    boolean bVal = true;
    string sVal = "Hello from Ballerina";
    map<string> strMapVal = {
        stringVal: sVal
    };

    map<any> m = {
        emp1: e1,
        intVal: iVal,
        emp2: e2,
        boolVal: bVal,
        mapVal: strMapVal
    };

    map<string|int> strMapValTwo = <map<string|int>> m.get("mapVal");

    return <Employee> m.get("emp1") === e1 && <EmployeeObject> m.get("emp2") === e2 && <int> m.get("intVal") == iVal &&
                <string> strMapValTwo.get("stringVal") == sVal && bVal == <boolean> m.get("boolVal");
}

function testMapElementCastNegative() {
    Employee e1 = { name: "Anne", id: 12495673 };
    EmployeeObject e2 = new("John");
    int iVal = 12345;
    boolean bVal = true;
    string sVal = "Hello from Ballerina";
    map<string> strMapVal = {
        stringVal: sVal
    };

    map<any> m = {
        emp1: e1,
        intVal: iVal,
        emp2: e2,
        boolVal: bVal,
        mapVal: strMapVal
    };

    Employee e3 = <Employee> m.get("emp1");
    int iVal2 = <int> m.get("intVal");
    map<int> strMapValTwo = <map<int>> m.get("mapVal");
}

function testListElementCastPositive() returns boolean {
    Employee e1 = { name: "Anne", id: 12495673 };
    int iVal = 12345;
    int iValTwo = 2357812;
    boolean bVal = true;
    string sVal = "Hello from Ballerina";
    any[] anyArr = [iValTwo, sVal, bVal];

    [Employee, int, any[]] t1 = [e1, iVal, anyArr];

    any a = t1[2];
    any[] anyArrTwo = <any[]> a;

    return <Employee> t1[0] === e1 && <int> t1[1] == iVal && <int> anyArrTwo[0] == iValTwo &&
                <string> anyArrTwo[1] == sVal && bVal == <boolean> anyArrTwo[2];
}

function testListElementCastNegative() {
    Employee e1 = { name: "Anne", id: 12495673 };
    int iVal = 12345;
    boolean bVal = true;
    string sVal = "Hello from Ballerina";
    any[] anyArr = [sVal, bVal];

    [Employee, int, any[]] t1 = [e1, iVal, anyArr];

    any a = t1[2];
    any[] anyArrTwo = <any[]> a;
    int iValTwo = <int> anyArrTwo[0];
}

function testOutOfOrderUnionConstraintCastPositive() returns boolean {
    map<int|string> m = { one: 1, two: "2" };
    anydata a = m;
    map<string|int> m2 = <map<string|int>> a;
    return m === m2;
}

function testStringAsInvalidBasicType() {
    string|int u1 = "I'm not an int!";
    any a = u1;
    int i = <int> a;
}

function testBroaderObjectCast() returns boolean {
    string name = "Em Zee";
    EmployeeObject e = new(name);
    PersonObject p = e;
    PersonObject p2 = <PersonObject> p;
    return e === p2 && p2.name == name;
}

function testCastOnPotentialConversion() returns boolean {
    string s = "Em Zee";
    string|int|Employee u1 = s;
    string|float u2 = <string|float> u1;
    return u2 == s;
}

function testCastToNumericType() returns boolean {
    int i = 1;
    anydata? j = i;
    int k = <int> j;

    decimal l = 10.0;
    j = l;
    decimal m = <decimal> j;

    float n = 10.01;
    j = n;
    float o = <float> j;

    return k == i && m == l && n == o;
}

function testCastPanicWithCheckTrap() returns string|int|error {
    return check trap testFunctionCastNegativeHelper();
}

function testFunctionCastNegativeHelper() returns string|int {
    testFunctionCastNegative();
    return "successful";
}

//////////////////////// from string ////////////////////////

function testStringAsString(string s1) returns boolean {
    string s2 = <string> s1;
    anydata s3 = <string> getString(s1);

    return s1 == s3 && s2 == s1 && s3 is string;
}

function testStringInUnionAsString(string s1) returns boolean {
    Employee|string|int s2 = s1;
    json s3 = s1;
    anydata s4 = s1;
    any s5 = s1;

    string s6 = <string> s2;
    string s7 = <string> s3;
    string s8 = <string> s4;
    string s9 = <string> s5;

    return s1 == s6 && s7 == s6 && s7 == s8 && s9 == s8;
}

//////////////////////// from boolean ////////////////////////

function testBooleanAsBoolean() returns [boolean, boolean, boolean, boolean] {
    boolean b1 = true;
    boolean s1 = <boolean> b1;
    anydata a = <boolean> getBoolean(b1);
    boolean s2 = false;
    if (a is boolean) {
        s2 = a;
    }

    b1 = false;
    boolean s3 = <boolean> b1;
    boolean s4 = <boolean> getBoolean(b1);

    return [s1, s2, s3, s4];
}

function testBooleanInUnionAsBoolean() returns [boolean, boolean] {
    boolean f1 = true;
    Employee|string|int|boolean f2 = f1;
    json f3 = true;
    anydata f4 = f1;
    any f5 = f1;

    boolean s6 = <boolean> f2;
    boolean s7 = <boolean> f3;
    boolean s8 = <boolean> f4;
    boolean s9 = <boolean> f5;

    boolean ft1 = (s7 == s6 && s7 == s8 && s9 == s8) ? s6 : false;

    f1 = false;
    f2 = f1;
    f3 = false;
    f4 = f1;
    f5 = f1;

    s6 = <boolean> f2;
    s7 = <boolean> f3;
    s8 = <boolean> f4;
    s9 = <boolean> f5;

    boolean ft2 = (s7 == s6 && s7 == s8 && s9 == s8) ? s6 : true;

    return [ft1, ft2];
}

function testSimpleTypeToUnionCastPositive() returns boolean {
    string s = "hello world";
    string? u1 = <string?> s;
    boolean castSuccessful = s == u1;

    anydata u2 = <boolean|float> true;
    castSuccessful = castSuccessful && u2 == true;

    int i = 3;
    any u3 = <any> i;
    anydata u4 = <anydata> u3;
    return castSuccessful && u4 == i;
}

function testDirectlyUnmatchedUnionToUnionCastPositive() returns boolean {
    string s = "hello world";
    string|typedesc<anydata> v1 = s;
    json v2 = <json> v1;
    boolean castSuccessful = s == v2;

    Lead lead = { name: "Em", id: 2000, rating: 10.0 };
    Employee|string v3 = lead;
    Lead|int v4 = <Lead|int> v3;
    return castSuccessful && v4 == lead && v4 === lead;
}

function testDirectlyUnmatchedUnionToUnionCastNegative_1() {
    string|int v1 = 1;
    string|boolean v2 = <string|boolean> v1;
}

function testDirectlyUnmatchedUnionToUnionCastNegative_2() {
    Employee|string v3 = "lead";
    Lead|int v4 = <Lead|int> v3;
}

function testTypeCastOnRecordLiterals() returns [string, string, string] {
    string s1 = _init_(<ServerModeConfig>{});
    string s2 = _init_(<EmbeddedModeConfig>{});
    string s3 = _init_(<InMemoryModeConfig>{});
    return [s1, s2, s3];
}

function _init_(InMemoryModeConfig|ServerModeConfig|EmbeddedModeConfig rec) returns string {
    if (rec is ServerModeConfig) {
        return "Server mode configuration";
    } else if (rec is EmbeddedModeConfig) {
        return "Embedded mode configuration";
    } else {
        return "In-memory mode configuration";
    }
}

public type InMemoryModeConfig record {|
    string name = "";
    string username = "";
    string password = "";
    map<any> dbOptions = {name:"asdf"};
|};

public type ServerModeConfig record {|
    string host = "";
    int port = 9090;
    *InMemoryModeConfig;
|};

public type EmbeddedModeConfig record {|
    string path = "";
    *InMemoryModeConfig;
|};

type FooBar "foo"|"bar";
type FooBarOne "foo"|"bar"|1;
type FooBarOneTwoTrue "foo"|"bar"|1|2.0|boolean;

function testFiniteTypeToValueTypeCastPositive() returns boolean {
    FooBar f = "foo";
    string s = <string> f;
    boolean castSuccessful = f == s;

    FooBarOne f2 = 1;
    int i = <int> f2;
    castSuccessful = castSuccessful && f2 == i;

    FooBarOneTwoTrue f3 = true;
    boolean b = <boolean> f3;
    return castSuccessful && f3 == b;
}

function testFiniteTypeToValueTypeCastNegative() {
    FooBarOne f2 = 1;
    string i = <string> f2;
}

function testFiniteTypeToRefTypeCastPositive() returns boolean {
    FooBar f = "bar";
    string|int s = <string|int> f;
    boolean castSuccessful = f == s;

    FooBarOne f2 = "foo";
    any i = <any> f2;
    castSuccessful = castSuccessful && f2 === i;

    FooBarOneTwoTrue f3 = true;
    json b = <json> f3;
    return castSuccessful && f3 == b;
}

function testFiniteTypeToRefTypeCastNegative() {
    FooBarOne f2 = 1;
    string|xml i = <string|xml> f2;
}

function testValueTypeToFiniteTypeCastPositive() returns boolean {
    string a = "bar";
    FooBar b = <FooBar> a;
    boolean castSuccessful = a == b;

    int c = 1;
    FooBarOne d = <FooBarOne> c;
    castSuccessful = castSuccessful && c == d;

    float f = 2.0;
    FooBarOneTwoTrue g = <FooBarOneTwoTrue> f;
    return castSuccessful && f == g;
}

function testValueTypeToFiniteTypeCastNegative() {
    int a = 2;
    FooBarOne d = <FooBarOne> a;
}

type FooOneTrue 1|"foo"|true;

function testFiniteTypeToFiniteTypeCastPositive() returns boolean {
    FooBar a = "bar";
    FooBarOne b = <FooBarOne> a;
    boolean castSuccessful = a == b;

    FooBarOne c = "foo";
    FooOneTrue d = <FooOneTrue> c;
    return castSuccessful && c == d;
}

function testFiniteTypeToFiniteTypeCastNegative() {
    FooBarOne a = "bar";
    FooOneTrue b = <FooOneTrue> a;
}

function testFunc(string s, int i) returns string {
    return i.toString() + s;
}

function testFutureFunc() returns int {
    return 1;
}

function getString(string s) returns string {
    return s;
}

function getBoolean(boolean b) returns boolean {
    return b;
}

function testContexuallyExpectedType() returns Employee {
    Employee e = <@untainted> { name: "Em Zee", id: 1100 };
    return e;
}

function testContexuallyExpectedTypeRecContext() returns Employee {
    return <@untainted> { name: "Em Zee", id: 1100 };
}
