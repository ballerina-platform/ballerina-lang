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

type ItemType xml:Element|xml:Comment|xml:ProcessingInstruction|xml:Text;
type XmlElement xml:Element;

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

function testXML() {
    xml:Element elem1 = xml `<hello>xml content</hello>`;
    xml<xml:Element> x1 = getXML(XmlElement, elem1);
    assert(elem1, x1);
}

function testStream() {
    string[] stringList = ["hello", "world", "from", "ballerina"];
    stream<string> st = stringList.toStream();
    stream<string> newSt = getStream(st, string);
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

    table<Person> newTab = getTable(tab, Person);
    assert(tab, newTab);
}

function testFunctionPointers() {
    function (anydata) returns int fn = s => 10;
    function (string) returns int newFn = getFunction(fn, string, int);
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
        paramTypes: ["io.ballerina.runtime.internal.values.ObjectValue", "io.ballerina.runtime.api.values.BTypedesc"]
    } external;
}

type IntStream stream<int>;

type PersonTable table<Person>;

type IntArray int[];

type XmlType xml;

function testComplexTypes() {
    json j = echo(<json>{"name": "John Doe"}, json);
    assert(<json>{"name": "John Doe"}, j);

    xml x = echo(xml `<hello>xml content</hello>`, XmlType);
    assert(xml `<hello>xml content</hello>`, x);

    int[] ar = echo(<IntArray>[20, 30, 40], IntArray);
    assert(<IntArray>[20, 30, 40], ar);

    PersonObj pObj = new("John", "Doe");
    PersonObj nObj = echo(pObj, PersonObj);
    assertSame(pObj, nObj);

    int[] intList = [10, 20, 30, 40, 50];
    stream<int> st = intList.toStream();
    stream<int> newSt = echo(st, IntStream);
    int tot = 0;

    error? err = newSt.forEach(function (int x) { tot+= x; });
    assert(150, tot);

    table<Person> key(name) tab = table [
        { name: "Chiran", age: 33},
        { name: "Mohan", age: 37},
        { name: "Gima", age: 38},
        { name: "Granier", age: 34}
    ];

    table<Person> newTab = echo(tab, PersonTable);
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
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BXml"]
} external;

function getStream(stream<anydata> value, typedesc<anydata> td) returns stream<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getStream",
    paramTypes: ["io.ballerina.runtime.api.values.BStream", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getTable(table<map<anydata>> value, typedesc<anydata> td) returns table<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTable",
    paramTypes: ["io.ballerina.runtime.internal.values.TableValue", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getFunction(function (string|int) returns anydata fn, typedesc<anydata> param, typedesc<anydata> ret)
                                                                returns function (param) returns ret = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getFunction",
    paramTypes: ["io.ballerina.runtime.api.values.BFunctionPointer", "io.ballerina.runtime.api.values.BTypedesc",
                    "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getTypedesc(typedesc<anydata> td) returns typedesc<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getTypedesc",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getFuture(future<anydata> f, typedesc<anydata> td) returns future<td> = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getFuture",
    paramTypes: ["io.ballerina.runtime.api.values.BFuture", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function echo(any val, typedesc<any> td) returns td = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "echo",
    paramTypes: ["io.ballerina.runtime.api.values.BValue", "io.ballerina.runtime.api.values.BTypedesc"]
} external;

function getValue2(typedesc<int|string> aTypeVar) returns aTypeVar = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    name: "getValue",
    paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
} external;

public type OutParameter object {
    // This is OK, referencing classes/object constructors should have 'external' implementations.
    public function get(typedesc<anydata> td) returns td|error;
};

public class OutParameterClass {
    *OutParameter;

    int a;
    string b;
    error c = error("not a nor b");

    function init() {
        self.a = 1234;
        self.b = "hello world";
    }

    public function get(typedesc<anydata> td) returns td|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
    } external;
}

var outParameterObject = object OutParameter {

    int i = 321;

    public isolated function get(typedesc<anydata> td) returns td|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getIntFieldOrDefault",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
    } external;
};

function testDependentlyTypedMethodsWithObjectTypeInclusion() {
    OutParameterClass c1 = new;
    int|error v1 = c1.get(int);
    assert(1234, <int> checkpanic v1);
    assertSame(c1.c, c1.get(float));
    assert("hello world", <string> checkpanic c1.get(string));

    assert(321, <int> checkpanic outParameterObject.get(int));
    decimal|error v2 = outParameterObject.get(decimal);
    assert(23.45d, <decimal> checkpanic v2);
}

public class Bar {
    int i = 1;

    public function get(typedesc<anydata> td) returns td|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getIntFieldOrDefault",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
    } external;
}

public class Baz {
    int i = 2;

    public function get(typedesc<anydata> td) returns anydata|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getIntFieldOrDefault",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
    } external;
}

public class Qux {
    int i = 3;

    public function get(typedesc<anydata> td) returns td|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getIntFieldOrDefault",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
    } external;
}

public class Quux {
    int i = 4;

    public function get(typedesc<any> td) returns td|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getIntFieldOrDefault",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc"]
    } external;
}

class Corge {
    int i = 100;

    function get(typedesc<anydata> td2, typedesc<anydata> td) returns td2|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getValueForParamOne",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BTypedesc"]
    } external;
}

class Grault {
    int i = 200;

    function get(typedesc<anydata> td, typedesc<anydata> td2) returns td|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getValueForParamOne",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BTypedesc"]
    } external;
}

class Garply {
    int i = 300;

    function get(typedesc<anydata> td, typedesc<anydata> td2) returns td2|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getValueForParamOne",
        paramTypes: ["io.ballerina.runtime.api.values.BTypedesc", "io.ballerina.runtime.api.values.BTypedesc"]
    } external;
}

public function testSubtypingWithDependentlyTypedMethods() {
    Bar bar = new;
    Baz baz = new;
    Qux qux = new;

    assert(1, <int> checkpanic bar.get(int));
    assert(2, <int> checkpanic baz.get(int));
    assert(3, <int> checkpanic qux.get(int));
    decimal|error v2 = bar.get(decimal);
    assert(23.45d, <decimal> checkpanic v2);
    anydata|error v3 = baz.get(decimal);
    assert(23.45d, <decimal> checkpanic v3);
    v2 = qux.get(decimal);
    assert(23.45d, <decimal> checkpanic v2);

    Baz baz1 = bar;
    Bar bar1 = qux;
    assert(1, <int> checkpanic baz1.get(int));
    assert(3, <int> checkpanic bar1.get(int));

    assert(true, <any> bar is Baz);
    assert(true, <any> qux is Bar);
    assert(true, <any> bar is Qux);
    assert(false, <any> baz is Bar);
    assert(false, <any> new Quux() is Qux);
    assert(false, <any> qux is Quux);

    Corge corge = new Grault();
    assert(200, <int> checkpanic corge.get(int, string));
    assert("Hello World!", <string> checkpanic corge.get(string, int));

    Grault grault = new Corge();
    assert(100, <int> checkpanic grault.get(int, string));
    assert("Hello World!", <string> checkpanic grault.get(string, float));

    assert(true, <any> new Corge() is Grault);
    assert(true, <any> new Grault() is Corge);
    assert(false, <any> new Corge() is Garply);
    assert(false, <any> new Garply() is Corge);
    assert(false, <any> new Grault() is Garply);
    assert(false, <any> new Garply() is Grault);
}

function getWithDefaultableParams(int|string x, int|string y = 1, typedesc<int|string> z = int) returns z =
    @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getWithDefaultableParams"
    } external;

function testDependentlyTypedFunctionWithDefaultableParams() {
    int a = getWithDefaultableParams(1);
    assert(2, a);

    int b = getWithDefaultableParams("1");
    assert(2, b);

    string c = getWithDefaultableParams("hello", z = string);
    assert("hello1", c);

    string d = getWithDefaultableParams("hello", z = string, y = " world");
    assert("hello world", d);

    string e = getWithDefaultableParams(z = string, y = " world", x = "hello again");
    assert("hello again world", e);

    int f = getWithDefaultableParams(1, 2);
    assert(3, f);

    int g = getWithDefaultableParams(2, 2, int);
    assert(4, g);

    int h = getWithDefaultableParams(z = int, x = 101);
    assert(102, h);
}

type IntOrString int|string;

public function testStartActionWithDependentlyTypedFunctions() {
    Client cl = new;

    var assert1 = function (future<int|string|error> f) {
        int|string|error r = wait f;
        assert(true, r is error);
        error e = <error> r;
        assert("Error!", e.message());
        assert("Union typedesc", <string> <any> checkpanic e.detail()["message"]);
    };
    future<int|string|error> a = start getWithUnion("", IntOrString);
    assert1(a);
    //future<int|string|error> b = start cl.get("", IntOrString);
    //assert1(b);
    future<int|string|error> c = start cl->remoteGet("", IntOrString);
    assert1(c);

    var assert2 = function (future<int|error> f, int expected) {
        int|error r = wait f;
        assert(true, r is int);
        assert(expected, checkpanic r);
    };
    future<int|error> d = start getWithUnion("hello", int);
    assert2(d, 5);
    //future<int|error> e = start cl.get(3, int);
    //assert2(e, 4);
    //future<int|error> f = start cl.get("");
    //assert2(f, 0);
    future<int|error> g = start cl->remoteGet("hi", int);
    assert2(g, 2);

    var assert3 = function (future<string|error> f, string expected) {
        string|error r = wait f;
        assert(true, r is string);
        assert(expected, checkpanic r);
    };
    future<string|error> h = start getWithUnion("hello", string);
    assert3(h, "hello");
    //future<string|error> i = start cl.get(1, string);
    //assert3(i, "1");
    future<string|error> j = start cl->remoteGet("", string);
    assert3(j, "");
}

function getWithUnion(int|string x, typedesc<int|string> y) returns y|error =
    @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "getWithUnion"
    } external;

client class Client {
    // https://github.com/ballerina-platform/ballerina-lang/issues/28740
    //function get(int|string x, typedesc<int|string> y = int) returns y|error = @java:Method {
    //    'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
    //    name: "clientGetWithUnion"
    //} external;

    remote function remoteGet(int|string x, typedesc<int|string> y) returns y|error = @java:Method {
        'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
        name: "clientRemoteGetWithUnion"
    } external;
}

function getWithRestParam(int i, typedesc<int|string> j, int... k) returns j|boolean =
     @java:Method {
         'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType"
     } external;

function getWithMultipleTypedescs(int i, typedesc<int|string> j, typedesc<int> k, typedesc<int>... l)
    returns j|k|boolean = @java:Method { 'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType" } external;

function testArgsForDependentlyTypedFunctionViaTupleRestArg() {
    [typedesc<string>] a = [string];
    string|error b = getWithUnion(123, ...a);
    assert("123", checkpanic b);

    [int, typedesc<int>] c = [10, int];
    int|error d = getWithUnion(...c);
    assert(11, checkpanic d);

    [int, typedesc<string>] e = [0, string];
    string|boolean f = getWithRestParam(...e);
    assert(true, f);

    [typedesc<string>] g = [string];
    string|boolean h = getWithRestParam(1, ...g);
    assert(false, h);

    [int, typedesc<int>, int...] i = [101, int, 1, 2, 3];
    int|boolean j = getWithRestParam(...i);
    assert(107, j);

    [int, typedesc<int>, int...] k = [101, int];
    assert(101, getWithRestParam(...k));

    int|boolean l = getWithRestParam(102, int, 1, 2);
    assert(105, l);

    [int, typedesc<string>, typedesc<int>, typedesc<int>] m = [1, string, int, int];
    int|string|boolean n = getWithMultipleTypedescs(...m);
    assert(true, n);

    [typedesc<string>, typedesc<int>, typedesc<int>] o = [string, int, int];
    int|string|boolean p = getWithMultipleTypedescs(1, ...o);
    assert(true, p);

    [int, typedesc<byte>, typedesc<byte>, typedesc<int>...] q = [1, byte, byte, int];
    byte|boolean r = getWithMultipleTypedescs(...q);
    assert(true, r);
}

function testArgsForDependentlyTypedFunctionViaArrayRestArg() {
    typedesc<string>[1] a = [string];
    string|error b = getWithUnion(123, ...a);
    assert("123", checkpanic b);

    typedesc<int>[1] c = [int];
    int|error d = getWithUnion(10, ...c);
    assert(11, checkpanic d);

    assert(101, getWithRestParam(101, ...c));

    typedesc<int>[2] m = [int, int];
    int|string|boolean n = getWithMultipleTypedescs(1, string, ...m);
    assert(true, n);

    typedesc<byte>[4] q = [byte, byte, byte, byte];
    byte|boolean r = getWithMultipleTypedescs(1, ...q);
    assert(true, r);
}

type XY record {|
    int|string x;
    typedesc<int> y = int;
|};

type IJ record {|
    int i;
    typedesc<string> j;
|};

type IJK record {|
    int i;
    typedesc<string> j;
    typedesc<int> k;
|};

function testArgsForDependentlyTypedFunctionViaRecordRestArg() {
    record {| typedesc<string> y; |} a = {y: string};
    string|error b = getWithUnion(123, ...a);
    assert("123", checkpanic b);

    XY c = {x: 10};
    int|error d = getWithUnion(...c);
    assert(11, checkpanic d);

    IJ e = {i: 0, j: string};
    string|boolean f = getWithRestParam(...e);
    assert(true, f);

    record {| typedesc<string> j = string; |} g = {};
    string|boolean h = getWithRestParam(1, ...g);
    assert(false, h);

    IJK m = {i: 1, j: string, k: int};
    int|string|boolean n = getWithMultipleTypedescs(...m);
    assert(true, n);

    record {| typedesc<string> j = string; typedesc<int> k; |} o = {k: int};
    int|string|boolean p = getWithMultipleTypedescs(1, ...o);
    assert(true, p);

    record {| int i; typedesc<byte> j = byte; typedesc<byte> k; |} q = {i: 1, k: byte};
    byte|boolean r = getWithMultipleTypedescs(...q);
    assert(true, r);
}

public type ClientActionOptions record {|
    string mediaType?;
    string header?;
|};

public type TargetType typedesc<int|string>;

public client class ClientWithMethodWithIncludedRecordParamAndDefaultableParams {
    remote function post(TargetType targetType = int, *ClientActionOptions options)
        returns @tainted targetType = @java:Method {
                                          'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
                                          name: "clientPost"
                                      } external;
                                      
    function calculate(int i, TargetType targetType = int, *ClientActionOptions options)
        returns @tainted targetType = @java:Method {
                                          'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
                                          name: "calculate"
                                      } external;
}

public client class ClientWithMethodWithIncludedRecordParamAndRequiredParams {
    remote function post(TargetType targetType, *ClientActionOptions options)
        returns @tainted targetType = @java:Method {
                                          'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
                                          name: "clientPost"
                                      } external;

    function calculate(int i, TargetType targetType, *ClientActionOptions options)
        returns @tainted targetType|error = @java:Method {
                                                 'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
                                                 name: "calculate"
                                             } external;
}

function testDependentlyTypedFunctionWithIncludedRecordParam() {
    ClientWithMethodWithIncludedRecordParamAndDefaultableParams cl = new;
    string p1 = cl->post(mediaType = "application/json", header = "active", targetType = string);
    assert("application/json active", p1);

    int p2 = cl->post(mediaType = "json", header = "active", targetType = int);
    assert(10, p2);

    int p3 = cl->post(mediaType = "xml", header = "active");
    assert(9, p3);

    string p4 = cl->post(mediaType = "json", targetType = string);
    assert("json ", p4);

    string p5 = cl->post(targetType = string);
    assert(" ", p5);

    int p6 = cl->post();
    assert(0, p6);
    
    string p7 = cl.calculate(0, mediaType = "application/json", header = "active", targetType = string);
    assert("application/json active0", p7);

    int p8 = cl.calculate(101, mediaType = "json", header = "active", targetType = int);
    assert(111, p8);

    int p9 = cl.calculate(2, mediaType = "xml", header = "active");
    assert(11, p9);

    string p10 = cl.calculate(3, mediaType = "json", targetType = string);
    assert("json 3", p10);

    string p11 = cl.calculate(4, targetType = string);
    assert(" 4", p11);

    int p12 = cl.calculate(12);
    assert(12, p12);

    string p13 = cl->post(targetType = string, mediaType = "xml");
    assert("xml ", p13);

    string p14 = cl.calculate(5, targetType = string, mediaType = "json");
    assert("json 5", p14);

    string p15 = cl.calculate(6, string, {});
    assert(" 6", p15);

    int p16 = cl.calculate(7, int, {});
    assert(7, p16);

    int p17 = cl.calculate(8, int, {});
    assert(8, p17);

    ClientWithMethodWithIncludedRecordParamAndRequiredParams cl2 = new;
    string p18 = cl2->post(mediaType = "application/json", header = "active", targetType = string);
    assert("application/json active", p18);

    int p19 = cl2->post(mediaType = "json", header = "active", targetType = int);
    assert(10, p19);

    int p20 = cl2->post(int, {});
    assert(0, p20);

    int p21 = cl2->post(int, {mediaType: "application/json"});
    assert(16, p21);

    string p22 = cl2->post(string, {});
    assert(" ", p22);

    string p23 = cl2->post(string, {mediaType: "application/json"});
    assert("application/json ", p23);

    string|error p24 = cl2.calculate(0, mediaType = "application/json", header = "active", targetType = string);
    assert("application/json active0", checkpanic p24);

    int|error p25 = cl2.calculate(1, mediaType = "json", header = "active", targetType = int);
    assert(11, checkpanic p25);

    int|error p26 = cl2.calculate(2, int, {header: "active"});
    assert(8, checkpanic p26);

    int|error p27 = cl2.calculate(3, int, {mediaType: "application/json"});
    assert(19, checkpanic p27);

    string|error p28 = cl2.calculate(4, string, {});
    assert(" 4", checkpanic p28);

    string|error p29 = cl2.calculate(5, string, {mediaType: "application/json"});
    assert("application/json 5", checkpanic p29);
}

client class ClientObjImpl {
    *ClientObject;
    remote isolated function query(stream<record {}> strm, typedesc<record {}> rowType = <>) returns stream <rowType> =
    @java:Method {
                'class: "org.ballerinalang.nativeimpl.jvm.tests.VariableReturnType",
                name: "getStreamOfRecords",
                paramTypes: ["io.ballerina.runtime.api.values.BStream", "io.ballerina.runtime.api.values.BTypedesc"]
                } external;
}

public type ClientObject client object {
    remote isolated function query(stream<record {}> strm, typedesc<record {}> rowType = <>) returns stream <rowType>;
};

function testDependentlyTypedFunctionsWithStreams() {
    ClientObject cl = new ClientObjImpl();
    Person p1 = getRecord();
    Person p2 = getRecord();
    Person[] personList = [p1, p2];
    stream<Person> studentStream = personList.toStream();
    stream<Person> y = cl->query(studentStream, Person);
    var rec = y.next();
    if (rec is record {| Person value; |}) {
        Person person = rec.value;
        assert(20, person.age);
        assert("John Doe", person.name);
        return;
    }
    rec = y.next();
    assert(true, rec is record {| Person value; |});
}

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

function assertSame(any|error expected, any|error actual) {
    if (expected !== actual) {
        typedesc<any|error> expT = typeof expected;
        typedesc<any|error> actT = typeof actual;
        string detail = "expected value of type [" + expT.toString() + "] is not the same as actual value" +
                                " of type [" + actT.toString() + "]";
        panic error("{AssertionError}", message = detail);
    }
}
