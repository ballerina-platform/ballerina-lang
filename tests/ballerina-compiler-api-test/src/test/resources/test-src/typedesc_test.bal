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

const PI = 3.14;

type PathConfig record {|
    string path;
|};

public const annotation PathConfig Path on source const;

function foo(int x, float y = 12.34, int... rest) returns int {
    return 0;
}

class PersonObj {
    string name;

    function init(string name) {
        self.name = name;
    }

    function getName() returns string => self.name;
}

function bar() {
    decimal sum = 10.1 + 20.3;
}

function test() {
    int x = foo(10, 3.4, 10);

    future<int> f1 = start foo(20, 4.5);

    PersonObj[] arr = [];

    map<string> m1 = {};

    [int, string, float...] tup = [];

    typedesc<anydata> td = int;
    typedesc td2 = string;

    int|string|float union = 10;

    Number n = 20;

    Digit d = 1;

    Format fmt = DEFAULT;

    json j = {name: "Pubudu"};

    xml greet1 = xml `<greet>Hello</greet>`;

    readonly ro = 12;

    any a = "Hello World!";
    anydata ad = 1234;

    table<Person> key(name) tab = table [];
    table<Person> tab2 = table [];
    table<Person> key<string> tab3 = table key(name) [];

    'int:Unsigned32 uInt32 = 1000;
    'int:Signed32 sInt32 = -1000;
    'int:Unsigned8 uInt8 = 10;
    'int:Signed8 sInt8 = -10;
    'int:Unsigned16 uInt16 = 100;
    'int:Signed16 sInt16 = -100;

    'string:Char ch = "A";

    'xml:Element greet2 = xml `<greet>Hola!</greet>`;
    'xml:ProcessingInstruction pi = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    'xml:Comment comment = xml `<!-- hello from comment -->`;
    'xml:Text txt = xml `hello text`;

    xml<'xml:Element> greet3 = xml `<greet>Ciao!</greet>`;

    stream<Person> st1;
    stream<Person, ()> st2;
    stream<record {| string name; |}, error> st3;
}

type Number int|float|decimal;

type Digit 0|1|2|3;

public type Format DEFAULT|CSV|TDF;

public const DEFAULT = "default";
public const CSV = "csv";
public const TDF = "tdf";

type Person record {
  readonly string name;
  int age;
};

type Employee record {|
    *Person;
    string designation;
|};

type Foo record {
    int a;
};

type Bar record {|
    string b;
|};

type Baz record {|
    *Foo;
    float c;
    *Bar;
|};

type FooObj object {
    int a;

    function getA() returns int;
};

type BarObj object {
    string b;

    function getB() returns string;
};

type BazObj object {
    *FooObj;
    *BarObj;
};

class EmployeeObj {
    *PersonObj;
    string designation;
}

enum Colour {
    RED, BLUE, GREEN
}

function testEnumAsAType() {
    Colour c = "RED";
    string s = "Selected colour: " + c;
}

function neverReturns() returns never {
    // do something
}

type ReadonlyFoo Foo & readonly;

function testIntersectionType() {
    map<json> & readonly m = {};
    ReadonlyFoo rf = {a: 100};
}

public type DistinctPerson distinct object {
    string name;

    function getName() returns string;
};

function testCompileError() {
    int x = "foo";
    UndefinedType y;
}

function testBasicTypes() {
    int x = 10;
    float f = 12.3;
    decimal d = 23.4;
    boolean b = true;
    () nil = ();
    string s = "foo";
    byte byt = 100;
}

public type CancelledError distinct error;
public type UnKnownError distinct error;

public type Error CancelledError |
UnKnownError;

type Baz "A"|();

function testUnionsWithNil() {
    int|() x = 10;
    int|float|() y = 20;
    "A"|() z = ();
    "A"|"B"|() a = ();
}

const NIL = ();
type Nil NIL;

type Pi PI;

function testFunctionTypedesc() {
    function f = foo;
}

function testParameterizedType1(typedesc<anydata> td) returns td = external;

function testParameterizedType2(typedesc td = <>) returns error|td = external;

type IntString int|string;
type FloatBoolean float|boolean;

type FooUnion IntString|FloatBoolean;

function testUnions() {
    Colour? clr = RED;
    FooUnion fUnion = 10;
}

type FooReadOnly readonly & record {
    int a;
};

function testIntersectionTypeRef(FooObj & readonly f, Foo & readonly g) {
    FooReadOnly recR = g;
}

type bar function(string, int) returns boolean;

type Obj object {
    function(string, string) barObj;
};

function testObjectTypeSignature() {
    client object {
        int a;
        int b;
        *FooObj;

        function testFunc();
        remote function testRFunc();
    } obj;
}
