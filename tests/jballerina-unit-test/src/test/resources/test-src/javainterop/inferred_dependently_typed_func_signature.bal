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

import ballerina/java;

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
    assert("incompatible types: 'map' cannot be cast to 'map<anydata>'", err.detail()["message"].toString());
}

function testVarRefUseInMultiplePlaces() {
    [int, Person, float] tup1 = getTuple();
    assert(<[int, Person, float]>[150, expPerson, 12.34], tup1);
}

function testArrayTypes() {
    int[] arr = getArray();
    assert(<int[]>[10, 20, 30], arr);
}

//function testCastingForInvalidValues() {
//    int|error x = trap getInvalidValue(td2 = Person);
//
//    error err = <error>x;
//    assert("{ballerina}TypeCastError", err.message());
//    assert("incompatible types: 'map' cannot be cast to 'map<anydata>'", err.detail()["message"].toString());
//}

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
    function (string) returns int newFn = getFunction(fn);
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
    string res = wait fNew;
    assertSame(f, fNew);
    assert("Hello Pubudu", res);
}

type PersonObj object {
    string fname;
    string lname;

    function init(string fname, string lname) {
        self.fname = fname;
        self.lname = lname;
    }

    function name() returns string => self.fname + " " + self.lname;
};

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

    error? err = newSt.forEach(function (int x) { tot+= x; });
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
    assert("incompatible types: 'string' cannot be cast to 'int'", err.detail()["message"].toString());
}


// Interop functions
function getValue(typedesc<int|float|decimal|string|boolean> td = <>) returns td = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getValue",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function getRecord(typedesc<anydata> td = <>) returns td = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getRecord",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function query(string q, typedesc<anydata> rowType = <>) returns map<rowType> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "query",
    paramTypes: ["org.ballerinalang.jvm.values.api.BString", "org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function getTuple(typedesc<int|string> td1 = <>, typedesc<record {}> td2 = <>, typedesc<float|boolean> td3 = <>) returns [td1, td2, td3] = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTuple",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function getArray(typedesc<anydata> td = <>) returns td[] = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getArray",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

//function getInvalidValue(typedesc<int|Person> td1 = <>, typedesc<Person> td2 = Person) returns td1 = @java:Method {
//    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
//    name: "getInvalidValue",
//    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc", "org.ballerinalang.jvm.values.api.BTypedesc"]
//} external;

function getStream( stream<anydata> value, typedesc<anydata> td = <>) returns stream<td> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getStream",
    paramTypes: ["org.ballerinalang.jvm.values.api.BStream", "org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function getTable(table<anydata> value, typedesc<anydata> td = <>) returns table<td> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTable",
    paramTypes: ["org.ballerinalang.jvm.values.TableValue", "org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function getFunction(function (string|int) returns anydata fn, typedesc<anydata> param = <>, typedesc<anydata> ret = <>)
                                                                returns function (param) returns ret = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getFunction",
    paramTypes: ["org.ballerinalang.jvm.values.api.BFunctionPointer", "org.ballerinalang.jvm.values.api.BTypedesc",
                    "org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function getTypedesc(typedesc<anydata> td = <>) returns typedesc<td> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTypedesc",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function getFuture(future<anydata> f, typedesc<anydata> td = <>) returns future<td> = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getFuture",
    paramTypes: ["org.ballerinalang.jvm.values.api.BFuture", "org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function echo(any val, typedesc<any> td = <>) returns td = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "echo",
    paramTypes: ["org.ballerinalang.jvm.values.api.BValue", "org.ballerinalang.jvm.values.api.BTypedesc"]
} external;

function getValue2(typedesc<int|string> aTypeVar = <>) returns aTypeVar = @java:Method {
    class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getValue",
    paramTypes: ["org.ballerinalang.jvm.values.api.BTypedesc"]
} external;


// Util functions
function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string detail = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        panic error("{AssertionError}", message = detail);
    }
}

function assertSame(any expected, any actual) {
    if (expected !== actual) {
        typedesc<any> expT = typeof expected;
        typedesc<any> actT = typeof actual;
        string detail = "expected value of type [" + expT.toString() + "] is not the same as actual value" +
                                " of type [" + actT.toString() + "]";
        panic error("{AssertionError}", message = detail);
    }
}
