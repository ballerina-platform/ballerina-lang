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

import testorg/foo.dependently_typed as rt;

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
    int i = rt:getValue(int);
    assert(150, i);

    float f = rt:getValue(float);
    assert(12.34, f);

    decimal d = rt:getValue(decimal);
    assert(23.45d, d);

    string s = rt:getValue(string);
    assert("Hello World!", s);

    boolean b = rt:getValue(boolean);
    assert(true, b);
}

function testRecordVarRef() {
    Person p = rt:getRecord();
    assert(expPerson, p);

    Employee e = rt:getRecord(td = Employee);
    assert(<Employee>{name: "Jane Doe", age: 25, designation: "Software Engineer"}, e);
}

function testVarRefInMapConstraint() {
    map<int> m1 = rt:query("foo");
    assert(<map<int>>{"one": 10, "two": 20}, m1);

    map<string> m2 = rt:query("foo", rowType = string);
    assert(<map<string>>{"name": "Pubudu", "city": "Panadura"}, m2);
}

function testRuntimeCastError() {
    map<anydata> _ = rt:query("foo", rowType = float);
}

function testVarRefUseInMultiplePlaces() {
    [int, Person, float] tup1 = rt:getTuple(int, Person);
    assert(<[int, Person, float]>[150, expPerson, 12.34], tup1);
}

function testUnionTypes() {
    int|Person u = rt:getVariedUnion(1, int, Person);
    assert(expPerson, u);
}

function testArrayTypes() {
    int[] arr = rt:getArray(int);
    assert(<int[]>[10, 20, 30], arr);
}

function testCastingForInvalidValues() {
    int _ = rt:getInvalidValue(int, Person);
}

type XmlElement xml:Element;

function testXML() {
    xml:Element elem1 = xml `<hello>xml content</hello>`;
    xml<xml:Element> x1 = rt:getXML(XmlElement, elem1);
    assert(elem1, x1);
}

function testStream() {
    string[] stringList = ["hello", "world", "from", "ballerina"];
    stream<string> st = stringList.toStream();
    stream<string> newSt = rt:getStream(st, string);
    string s = "";

    newSt.forEach(function (string x) { s += x; });
    assert("helloworldfromballerina", s);
}

function testTable() {
    table<Employee> key(name) tab = table [
        { name: "Chiran", age: 33, designation: "SE" },
        { name: "Mohan", age: 37, designation: "SE" },
        { name: "Gima", age: 38, designation: "SE" },
        { name: "Granier", age: 34, designation: "SE" }
    ];

    table<Person> newTab = rt:getTable(tab, Person);
    assert(tab, newTab);
}

function testFunctionPointers() {
    function (anydata) returns int fn = s => 10;
    function (string) returns int newFn = rt:getFunction(fn, string, int);
    assertSame(fn, newFn);
    assert(fn("foo"), newFn("foo"));
}

function testTypedesc() {
    typedesc<Person> tP = rt:getTypedesc(Person);
    assert(Person.toString(), tP.toString());
}

function testFuture() {
    var fn = function (string name) returns string => "Hello " + name;
    future<string> f = start fn("Pubudu");
    future<string> fNew = rt:getFuture(f, string);
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

type PersonTable table<Person>;
type IntStream stream<int>;
type IntArray int[];
type XmlType xml;

function testComplexTypes() {
    json j = rt:echo(<json>{"name": "John Doe"}, json);
    assert(<json>{"name": "John Doe"}, j);

    xml x = rt:echo(xml `<hello>xml content</hello>`, XmlType);
    assert(xml `<hello>xml content</hello>`, x);

    int[] ar = rt:echo(<int[]>[20, 30, 40], IntArray);
    assert(<int[]>[20, 30, 40], ar);

    PersonObj pObj = new("John", "Doe");
    PersonObj nObj = rt:echo(pObj, PersonObj);
    assertSame(pObj, nObj);

    int[] intList = [10, 20, 30, 40, 50];
    stream<int> st = intList.toStream();
    stream<int> newSt = rt:echo(st, IntStream);
    int tot = 0;

    newSt.forEach(function (int x) { tot+= x; });
    assert(150, tot);

    table<Person> key(name) tab = table [
        { name: "Chiran", age: 33},
        { name: "Mohan", age: 37},
        { name: "Gima", age: 38},
        { name: "Granier", age: 34}
    ];

    table<Person> newTab = rt:echo(tab, PersonTable);
    assert(tab, newTab);
}

type XmlComment xml:Comment;

function testTypedescInferring() {
    xml:Comment x1 = xml `<!--Comment 1-->`;
    xml<xml:Comment> x2 = <xml<xml:Comment>> x1.concat(xml `<!--Comment 2-->`);
    xml<xml:Comment> a = rt:getDependentlyTypedXml(val = x2);
    assert(x2, a);
    assert(2, a.length());
    any v1 = a;
    assert(true, v1 is xml<xml:Comment>);

    xml<xml:Element> x3 = xml `<hello/>`;
    xml<xml:Element> b = rt:getDependentlyTypedXml(val = x3);
    assert(x3, b);
    assert(1, b.length());
    any v2 = b;
    assert(true, v2 is xml<xml:Element>);

    xml<xml:Comment> c = rt:getDependentlyTypedXml(XmlComment, x2);
    assert(x2, c);
    assert(2, c.length());
    any v3 = c;
    assert(true, v3 is xml<xml:Comment>);

    xml<xml:Element|xml:Comment> d = rt:getDependentlyTypedXml(td = XmlElement, val = x3);
    assert(x3, d);
    assert(1, d.length());
    any v4 = d;
    assert(true, v4 is xml<xml:Element>);

    xml<xml:Element> x5 = xml `<foo/>`;
    xml<xml:Element> e = rt:getDependentlyTypedXml();
    assert(x5, e);
    assert(1, e.length());
    any v5 = e;
    assert(true, v5 is xml<xml:Element>);
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

function assertSame(any expected, any actual) {
    if (expected !== actual) {
        typedesc<any> expT = typeof expected;
        typedesc<any> actT = typeof actual;
        string detail = "expected value of type [" + expT.toString() + "] is not the same as actual value" +
                                " of type [" + actT.toString() + "]";
        panic error("{AssertionError}", message = detail);
    }
}
