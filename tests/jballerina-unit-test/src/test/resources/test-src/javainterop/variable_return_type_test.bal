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

import ballerina/lang.'xml;
import ballerina/java;

type ItemType 'xml:Element|'xml:Comment|'xml:ProcessingInstruction|'xml:Text;

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
    int i = getValue(int);
    assert(150, i);

    float f = getValue(float);
    assert(12.34, f);

    decimal d = getValue(decimal);
    assert(23.45d, d);

    string s = getValue(string);
    assert("Hello World!", s);

    boolean b = getValue(boolean);
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

    map<string> m2 = query("foo", rowType = string);
    assert(<map<string>>{"name": "Pubudu", "city": "Panadura"}, m2);
}

function testRuntimeCastError() {
    map<anydata> m1 = query("foo", rowType = float);
}

function testVarRefUseInMultiplePlaces() {
    [int, Person, float] tup1 = getTuple(int, Person);
    assert(<[int, Person, float]>[150, expPerson, 12.34], tup1);
}

function testUnionTypes() {
    int|Person u = getVariedUnion(1, int, Person);
    assert(expPerson, u);
}

function testArrayTypes() {
    int[] arr = getArray(int);
    assert(<int[]>[10, 20, 30], arr);
}

function testCastingForInvalidValues() {
    int x = getInvalidValue(int, Person);
}

//function testXML() {
//    'xml:Element elem1 = xml `<hello>xml content</hello>`;
//    xml<'xml:Element> x1 = getXML('xml:Element, elem1);
//    assert(elem1, x1);
//}

function testStream() {
    string[] stringList = ["hello", "world", "from", "ballerina"];
    stream<string> st = stringList.toStream();
    stream<string> newSt = getStream(string, st);
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

    table<Person> newTab = getTable(Person, tab);
    assert(tab, newTab);
}

function testFunctionPointers() {
    function (anydata) returns int fn = s => 10;
    function (string) returns int newFn = getFunction(string, int, fn);
    assertSame(fn, newFn);
    assert(fn("foo"), newFn("foo"));
}

function testTypedesc() {
    typedesc<Person> tP = getTypedesc(Person);
    assert(Person.toString(), tP.toString());
}

function testFuture() {
    var fn = function (string name) returns string => "Hello " + name;
    future<string> f = start fn("Pubudu");
    future<string> fNew = getFuture(string, f);
    string res = wait fNew;
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

    function getObjectValue(typedesc<int|float|decimal|string|boolean> td) returns td = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType"
    } external;

    function getObjectValueWithTypeDescParam(typedesc<int|float|decimal|string|boolean> td) returns td = @java:Method {
        name: "getObjectValue",
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
    } external;

    function getObjectValueWithParamTypes(typedesc<int|float|decimal|string|boolean> td) returns td = @java:Method {
        name: "getObjectValue",
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        paramTypes: ["io.ballerina.runtime.values.ObjectValue", "io.ballerina.runtime.api.values.BTypedesc"]
    } external;
}

type IntStream stream<int>;

type PersonTable table<Person>;

type IntArray int[];

function testComplexTypes() {
    json j = echo(json, <json>{"name": "John Doe"});
    assert(<json>{"name": "John Doe"}, j);

    xml x = echo(xml, xml `<hello>xml content</hello>`);
    assert(xml `<hello>xml content</hello>`, x);

    int[] ar = echo(IntArray, <IntArray>[20, 30, 40]);
    assert(<IntArray>[20, 30, 40], ar);

    PersonObj pObj = new("John", "Doe");
    PersonObj nObj = echo(PersonObj, pObj);
    assertSame(pObj, nObj);

    int[] intList = [10, 20, 30, 40, 50];
    stream<int> st = intList.toStream();
    stream<int> newSt = echo(IntStream, st);
    int tot = 0;

    error? err = newSt.forEach(function (int x) { tot+= x; });
    assert(150, tot);

    table<Person> key(name) tab = table [
        { name: "Chiran", age: 33},
        { name: "Mohan", age: 37},
        { name: "Gima", age: 38},
        { name: "Granier", age: 34}
    ];

    table<Person> newTab = echo(PersonTable, tab);
    assert(tab, newTab);
}

function testObjectExternFunctions() {
    PersonObj pObj = new("John", "Doe");
    string s = pObj.getObjectValue(string);
    assert("John Doe", s);
    s = pObj.getObjectValueWithTypeDescParam(string);
    assert("John Doe Doe", s);
    s = pObj.getObjectValueWithParamTypes(string);
    assert("John Doe Doe Doe", s);
    assert("John Doe Doe Doe", pObj.fname);
    int a = pObj.getObjectValue(int);
    assert(150, a);
    assert("John Doe Doe Doe", pObj.fname);
}

function testFunctionAssignment() {
    function (typedesc<string|int> td) returns int|string fn = getValue2;
    int x = <int>fn(int);
    assert(150, x);

    string s = <string>fn(string);
    assert("Hello World!", s);

    x = <int>fn(string);
}


// Interop functions
function getValue(typedesc<int|float|decimal|string|boolean> td) returns td = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getValue",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getRecord(typedesc<anydata> td = Person) returns td = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getRecord",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function query(string q, typedesc<anydata> rowType = int) returns map<rowType> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "query",
    paramTypes: ["io.ballerina.runtime.api.values.BString", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getTuple(typedesc<int|string> td1, typedesc<record {}> td2, typedesc<float|boolean> td3 = float) returns [td1, td2, td3] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTuple",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getVariedUnion(int x, typedesc<int|string> td1, typedesc<record{ string name; }> td2) returns (td1|td2) = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getVariedUnion",
    paramTypes: ["long", "io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getArray(typedesc<anydata> td) returns td[] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getArray",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getInvalidValue(typedesc<int|Person> td1, typedesc<Person> td2) returns td1 = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getInvalidValue",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getXML(typedesc<ItemType> td, xml value) returns xml<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getXML",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BXML"]
} external;

function getStream(typedesc<anydata> td, stream<anydata> value) returns stream<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getStream",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BStream"]
} external;

function getTable(typedesc<anydata> td, table<anydata> value) returns table<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTable",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.values.TableValue"]
} external;

function getFunction(typedesc<anydata> param, typedesc<anydata> ret, function (string|int) returns anydata fn)
                                                                returns function (param) returns ret = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getFunction",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BTypedesc",
                    "io.ballerina.runtime.api.values.BFunctionPointer"]
} external;

function getTypedesc(typedesc<anydata> td) returns typedesc<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTypedesc",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getFuture(typedesc<anydata> td, future<anydata> f) returns future<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getFuture",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BFuture"]
} external;

function echo(typedesc<any> td, any val) returns td = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "echo",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BValue"]
} external;

function getValue2(typedesc<int|string> aTypeVar) returns aTypeVar = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getValue",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
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
