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

import ballerina/jballerina.java;

type Person record {
    readonly string name;
    int age;
};

type Employee record {
    *Person;
    string designation;
};

Person expPerson = {name: "John Doe", age: 20};


// Test functions

function testSimpleTypes() {
    int i = getValue();
    assert(150, i);

    float f = getValue();
    assert(12.34, f);

    decimal d = getValue();
    assert(23.45d, d);

    string s = getValue();
    assert("Hello World!", s);

    boolean b = getValue();
    assert(true, b);
}

function testRecordVarRef() {
    Person p = getRecord();
    assert(expPerson, p);

    Employee e = getRecord(td = Employee);
    assert(<Employee>{name: "Jane Doe", age: 25, designation: "Software Engineer"}, e);
}

function testVarRefInMapConstraint() {
    map<int> m1 = query("foo");
    assert(<map<int>>{"one": 10, "two": 20}, m1);

    map<string> m2 = query("foo");
    assert(<map<string>>{"name": "Pubudu", "city": "Panadura"}, m2);
}

function testRuntimeCastError() {
    map<anydata>|error m1 = trap query("foo");

    error err = <error>m1;
    assert("{ballerina}TypeCastError", err.message());
    assert("incompatible types: 'map' cannot be cast to 'map<anydata>'", <string> checkpanic err.detail()["message"]);
}

function testTupleTypes() {
    [int, Person, float] tup1 = getTuple(int, Person);
    assert(<[int, Person, float]>[150, expPerson, 12.34], tup1);

    [int, Person, boolean...] tup2 = getTupleWithRestDesc(int, Person);
    assert(<[int, Person, boolean...]>[150, expPerson, true, true], tup2);
    tup2[4] = false;
    assert(5, tup2.length());
}

function testArrayTypes() {
    int[] arr = getArray();
    assert(<int[]>[10, 20, 30], arr);
}

type XmlComment xml:Comment;
type XmlElement xml:Element;

function testXml() {
    xml:Comment x1 = xml `<!--Comment 1-->`;
    xml<xml:Comment> x2 = <xml<xml:Comment>> x1.concat(xml `<!--Comment 2-->`);
    xml<xml:Comment> a = getXml(val = x2);
    assert(x2, a);
    assert(2, a.length());
    any v1 = a;
    assert(true, v1 is xml<xml:Comment>);

    xml<xml:Element> x3 = xml `<hello/>`;
    xml<xml:Element> b = getXml(val = x3);
    assert(x3, b);
    assert(1, b.length());
    any v2 = b;
    assert(true, v2 is xml<xml:Element>);

    xml<xml:Comment> c = getXml(XmlComment, x2);
    assert(x2, c);
    assert(2, c.length());
    any v3 = c;
    assert(true, v3 is xml<xml:Comment>);

    xml<xml:Element|xml:Comment> d = getXml(td = XmlElement, val = x3);
    assert(x3, d);
    assert(1, d.length());
    any v4 = d;
    assert(true, v4 is xml<xml:Element>);

    xml<xml:Element> x5 = xml `<foo/>`;
    xml<xml:Element> e = getXml();
    assert(x5, e);
    assert(1, e.length());
    any v5 = e;
    assert(true, v5 is xml<xml:Element>);
}

function testCastingForInvalidValues() {
    var fn = function() {
        int x = getInvalidValue(td2 = Person);
    };

    error? y = trap fn();
    assert(true, y is error);
    error err = <error> y;
    assert("{ballerina}TypeCastError", err.message());
    assert("incompatible types: 'Person' cannot be cast to 'int'", <string> checkpanic err.detail()["message"]);
}

function testStream() {
    string[] stringList = ["hello", "world", "from", "ballerina"];
    stream<string> st = stringList.toStream();
    stream<string> newSt = getStream(st);
    string s = "";

    error? err = newSt.forEach(function (string x) { s += x; });
    assert("helloworldfromballerina", s);
}

function testTable() {
    table<Employee> key(name) tab = table [
        { name: "Chiran", age: 33, designation: "SE" },
        { name: "Mohan", age: 37, designation: "SE" },
        { name: "Gima", age: 38, designation: "SE" },
        { name: "Granier", age: 34, designation: "SE" }
    ];

    table<Person> newTab = getTable(tab);
    assert(tab, newTab);
}

function testFunctionPointers() {
    function (anydata) returns int fn = s => 10;
    function (string) returns int newFn = getFunction(fn, string);
    assertSame(fn, newFn);
    assert(fn("foo"), newFn("foo"));
}

function testTypedesc() {
    typedesc<Person> tP = getTypedesc();
    assert(Person.toString(), tP.toString());
}

function testFuture() {
    var fn = function (string name) returns string => "Hello " + name;
    future<string> f = start fn("Pubudu");
    future<string> fNew = getFuture(f, string);
    string res = checkpanic wait fNew;
    assertSame(f, fNew);
    assert("Hello Pubudu", res);
}

class PersonObj {
    string fname;
    string lname;

    function init(string fname, string lname) {
        self.fname = fname;
        self.lname = lname;
    }

    function name() returns string => self.fname + " " + self.lname;
}

type IntStream stream<int>;

type PersonTable table<Person>;

type IntArray int[];

function testComplexTypes() {
    json j = echo(<json>{"name": "John Doe"});
    assert(<json>{"name": "John Doe"}, j);

    xml x = echo(xml `<hello>xml content</hello>`);
    assert(xml `<hello>xml content</hello>`, x);

    int[] ar = echo(<IntArray>[20, 30, 40]);
    assert(<IntArray>[20, 30, 40], ar);

    PersonObj pObj = new("John", "Doe");
    PersonObj nObj = echo(pObj);
    assertSame(pObj, nObj);

    int[] intList = [10, 20, 30, 40, 50];
    stream<int> st = intList.toStream();
    stream<int> newSt = echo(st);
    int tot = 0;

    error? err = newSt.forEach(function (int x1) { tot+= x1; });
    assert(150, tot);

    table<Person> key(name) tab = table [
        { name: "Chiran", age: 33},
        { name: "Mohan", age: 37},
        { name: "Gima", age: 38},
        { name: "Granier", age: 34}
    ];

    table<Person> newTab = echo(tab);
    assert(tab, newTab);
}

function testFunctionAssignment() {
    function (typedesc<string|int> td) returns int|string fn = getValue2;
    int x = <int>fn(int);
    assert(150, x);

    string s = <string>fn(string);
    assert("Hello World!", s);

    var v = trap <int>fn(string);

    error err = <error>v;
    assert("{ballerina}TypeCastError", err.message());
    assert("incompatible types: 'string' cannot be cast to 'int'", <string> checkpanic err.detail()["message"]);
}

function testUnionTypes() {
    int|boolean? a = getSimpleUnion(1);
    assert(1, a);

    boolean|()|int b = getSimpleUnion("hello");
    assert((), b);

    string|boolean? c = getSimpleUnion(1);
    assert(false, c);

    ()|string|boolean d = getSimpleUnion("hello");
    assert("hello", d);

    int[]|map<int[]>? e = getComplexUnion();
    assert(<int[]> [1, 2], e);

    map<[int, string][]>|()|[int, string][] f = getComplexUnion();
    assert(<map<[int, string][]>> {entry: [[100, "Hello World"]]}, f);

    int|boolean? g = getSimpleUnion(1, int);
    assert(1, g);

    boolean|()|int h = getSimpleUnion(td = int, val = "hello");
    assert((), h);

    string|boolean? i = getSimpleUnion(1, td = string);
    assert(false, i);

    ()|string|boolean j = getSimpleUnion("hello", string);
    assert("hello", j);

    int[]|map<int[]>? k = getComplexUnion(int);
    assert(<int[]> [1, 2], k);

    map<[int, string][]>|()|[int, string][] l = getComplexUnion(td = [int, string]);
    assert(<map<[int, string][]>> {entry: [[100, "Hello World"]]}, l);
}

function testArgCombinations() {
    int[] a = funcWithMultipleArgs(1, int, ["hello", "world"]);
    assert(<int[]> [2, 1], a);

    string[] b = funcWithMultipleArgs(td = string, arr = ["hello", "world"], i = 3);
    assert(<string[]> ["hello", "world", "3"], b);

    record {| string[] arr = ["hello", "world", "Ballerina"]; int i = 123; typedesc<int> td; |} rec1 = {td: int};
    int[] c = funcWithMultipleArgs(...rec1);
    assert(<int[]> [3, 123], c);

    record {| string[] arr = ["hello", "world"]; |} rec2 = {};
    int[] d = funcWithMultipleArgs(1234, int, ...rec2);
    assert(<int[]> [2, 1234], d);

    [int, typedesc<string>, string[]] tup1 = [21, string, ["hello"]];
    string[] e = funcWithMultipleArgs(...tup1);
    assert(<string[]> ["hello", "21"], e);

    [string[]] tup2 = [["hello"]];
    string[] f = funcWithMultipleArgs(34, string, ...tup2);
    assert(<string[]> ["hello", "34"], f);

    int[] g = funcWithMultipleArgs(1);
    assert(<int[]> [0, 1], g);

    string[] h = funcWithMultipleArgs(101, arr = ["hello", "world"]);
    assert(<string[]> ["hello", "world", "101"], h);

    int[] i = funcWithMultipleArgs(arr = ["hello", "world"], i = 202);
    assert(<int[]> [2, 202], i);
}

function testBuiltInRefType() {
    stream<int> strm = (<int[]> [1, 2, 3]).toStream();

    readonly|handle|stream<int> a = funcReturningUnionWithBuiltInRefType(strm);
    assertSame(strm, a);
    stream<byte>|readonly b = funcReturningUnionWithBuiltInRefType();
    assertSame(100, b);
    stream<int>|readonly c = funcReturningUnionWithBuiltInRefType(strm = strm);
    assertSame(strm, c);
    stream<int>|readonly d = funcReturningUnionWithBuiltInRefType(strm, IntStream);
    assertSame(strm, d);
    stream<byte>|readonly e = funcReturningUnionWithBuiltInRefType(strm);
    assert(true, e is handle);
    string? str = java:toString(<handle> checkpanic e);
    assert("hello world", str);
}

function testParameterizedTypeInUnionWithNonParameterizedTypes() {
    record {| stream<int> x; |} rec = {x: (<int[]> [1, 2, 3]).toStream()};
    object {}|record {| stream<int> x; |}|int|error a = getValueWithUnionReturnType(rec);
    assert(101, <int> checkpanic a);

    PersonObj pObj = new ("John", "Doe");
    object {}|record {| stream<int> x; |}|string[]|error b = getValueWithUnionReturnType(pObj);
    assertSame(pObj, b);

    error|object {}|record {| stream<int> x; |}|boolean c = getValueWithUnionReturnType(true, boolean);
    assert(false, <boolean> checkpanic c);

    error|object {}|record {| stream<int> x; |}|boolean d = getValueWithUnionReturnType(td = boolean, val = false);
    assert(true, <boolean> checkpanic d);
}

function testUsageWithVarWithUserSpecifiedArg() {
    stream<int> strm = (<int[]> [1, 2, 3]).toStream();
    var x = funcReturningUnionWithBuiltInRefType(strm, IntStream);
    assertSame(strm, x);
}

function testFunctionWithAnyFunctionParamType() {
   var fn = function (function x, int y) {
   };

   function (function, int) x = getFunctionWithAnyFunctionParamType(fn);
   assertSame(fn, x);
}

function testUsageWithCasts() {
    int a = <int> getValue();
    assert(150, a);

    var b = <float> getValue();
    assert(12.34, b);

    any c = <decimal> getValue();
    assert(23.45d, <anydata> c);

    string|xml|float d = <string> getValue();
    assert("Hello World!", d);

    anydata e = <boolean> getValue();
    assert(true, e);

    anydata f = <[int, Person, boolean...]> getTupleWithRestDesc(int, Person);
    assert(<[int, Person, boolean...]>[150, expPerson, true, true], f);

    record {| stream<int> x; |} g = {x: (<int[]> [1, 2, 3]).toStream()};
    var h = <object {}|record {| stream<int> x; |}|int> checkpanic getValueWithUnionReturnType(g);
    assert(101, <int> h);

    PersonObj i = new ("John", "Doe");
    any|error j = <object {}|record {| stream<int> x; |}|string[]|error> getValueWithUnionReturnType(i);
    assertSame(i, j);
}

// Interop functions
function getValue(typedesc<int|float|decimal|string|boolean> td = <>) returns td = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getValue",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getRecord(typedesc<anydata> td = <>) returns td = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getRecord",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function query(string q, typedesc<anydata> rowType = <>) returns map<rowType> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "query",
    paramTypes: ["io.ballerina.runtime.api.values.BString", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getTuple(typedesc<int|string> td1, typedesc<record {}> td2, typedesc<float|boolean> td3 = <>) returns [td1, td2, td3] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTuple",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getTupleWithRestDesc(typedesc<int|string> td1, typedesc<record {}> td2, typedesc<float|boolean> td3 = <>)
        returns [td1, td2, td3...] =
            @java:Method { 'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType" } external;

function getArray(typedesc<anydata> td = <>) returns td[] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getArray",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getXml(typedesc<xml:Element|xml:Comment> td = <>, xml<xml:Element|xml:Comment> val = xml `<foo/>`)
    returns xml<td> = @java:Method {
                          'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType"
                      } external;

function getInvalidValue(typedesc<int|Person> td1 = <>, typedesc<Person> td2 = Person) returns td1 =
    @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getInvalidValue",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BTypedesc"]
    } external;

function getStream( stream<anydata> value, typedesc<anydata> td = <>) returns stream<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getStream",
    paramTypes: ["io.ballerina.runtime.api.values.BStream", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getTable(table<map<anydata>> value, typedesc<anydata> td = <>) returns table<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTable",
    paramTypes: ["io.ballerina.runtime.internal.values.TableValue", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getFunction(function (string|int) returns anydata fn, typedesc<anydata> param, typedesc<anydata> ret = <>)
                                                                returns function (param) returns ret = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getFunction",
    paramTypes: ["io.ballerina.runtime.api.values.BFunctionPointer", "io.ballerina.runtime.api.values.BTypedesc",
                    "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getTypedesc(typedesc<anydata> td = <>) returns typedesc<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTypedesc",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getFuture(future<anydata> f, typedesc<anydata> td = <>) returns future<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getFuture",
    paramTypes: ["io.ballerina.runtime.api.values.BFuture", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function echo(any val, typedesc<any> td = <>) returns td = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "echo",
    paramTypes: ["io.ballerina.runtime.api.values.BValue", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getValue2(typedesc<int|string> aTypeVar = <>) returns aTypeVar = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getValue",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getSimpleUnion(string|int val, typedesc<string|int> td = <>) returns td|boolean? = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType"
} external;

function getComplexUnion(typedesc<int|[int, string]> td = <>) returns td[]|map<td[]>? = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType"
} external;

function funcWithMultipleArgs(int i, typedesc<int|string> td = <>, string[] arr = []) returns td[] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType"
} external;

function funcReturningUnionWithBuiltInRefType(stream<int>? strm = (), typedesc<stream<int>> td = <>)
    returns readonly|td|handle =
        @java:Method {
            'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType"
        } external;

function getValueWithUnionReturnType(object{}|record {| stream<int> x; |}|anydata val, typedesc<anydata> td = <>)
   returns object{}|record {| stream<int> x; |}|error|td =
        @java:Method {
            'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType"
        } external;

function getFunctionWithAnyFunctionParamType(function (function, int) x, typedesc<int> td = <>)
    returns function (function, td) = @java:Method {
                                          'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType"
                                      } external;

function assert(anydata expected, anydata actual) {
    if (expected == actual) {
        return;
    }

    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string detail = "expected [" + expected.toString() + "] of type [" + expT.toString()
                        + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error("{AssertionError}", message = detail);
}

function assertSame(any|error expected, any|error actual) {
    if (expected === actual) {
        return;
    }

    typedesc<any|error> expT = typeof expected;
    typedesc<any|error> actT = typeof actual;
    string detail = "expected value of type [" + expT.toString() + "] is not the same as actual value" +
                            " of type [" + actT.toString() + "]";
    panic error("{AssertionError}", message = detail);
}
