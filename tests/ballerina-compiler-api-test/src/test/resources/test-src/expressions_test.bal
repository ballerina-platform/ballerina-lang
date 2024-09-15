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

import ballerina/lang.regexp;

function testLiterals() {
    anydata[] arr = [5, 12.34, 34.5d, true, (), "foo"];
    json j = null;
    byte[] b = base64 `SGVsbG8gQmFsbGVyaW5h`;
}

function testTemplateExprs() {
    string s = string `Hello World!`;
    xml x = xml `<Greeting>Hello!</Greeting>`;
    'object:RawTemplate rt = `${s} from Ballerina`;
}

function testStructuralConstructors() {
    string[] arr1 = ["foo", "bar"];
    var arr2 = [10, 20, 30];
    [int, string, float] tup = [10, "foo", 12.34];
    map<string> m = {"foo": "bar"};
    var m2 = {"City": "Colombo", "Code": 1};

    record {|
        string name;
        int age;
    |} person = {name: "John Doe", age: 20};

    json j = {name: "Jane Doe", age: 25};
}

function testAccessExprs() {
    record {|
        string name;
        int age?;
    |} person = {name: "John", age: 20};

    string name = person.name;
    int? age = person?.age;
    string optName = person["name"];
}

function testObjectConstructor() {
    PersonObj p1 = new ("Pubudu");
    PersonObj p2 = new PersonObj("Pubudu");

    object {
        string name;

        function getName() returns string;
    } person = object {
        string name = "Anon";

        function getName() returns string => self.name;
    };
}

function testMiscExprs() {
    int x = -20;
    int y = 10 * 20 / 5;
    int z = 10 + 20 - 5;
    boolean b = y >= z;
    anydata ad = "foo";

    if (ad is string && y != 0) {
        string s = ad;
    }

    string greeting = ad == "" ? "Hello" : "Hello " + ad.toString();
}

function testCheckingExprs() returns error? {
    string s1 = check foo();
    string s2 = checkpanic foo();
}

function testCastingExprs() {
    anydata ad = 10;
    string s = <string>ad;
    int x = <@untainted int>ad;
}

function testIneferredRecordType() {
    {"name" : "foo"};
}

function testStartAction() {
    start testAsync();
}

public function testAsync() {
    // do something
}

function testFunctionCall() {
    foo();

    PersonObj p = new("Pubudu");
    string s = p.getName();
}

service on new Listener() {
    resource function get processRequest() returns json {
        var v = {name: "John Doe"};
        return v;
    }
}

function testParameterizedType1(typedesc<anydata> td) returns td = external;

function testParameterizedType2(typedesc td = <>) returns td = external;

function testDependentlyTypedFunctionCall() {
    testParameterizedType1(string);

    int a = testParameterizedType2();
    testParameterizedType2(boolean);
}

function testExpressionsOfIntersectionTypes() {
    int[] x = [1, 2];
    x.cloneReadOnly();

    errorIntersection();

    recordIntersection({});

    nilableIntersection1();

    nilableIntersection2();
}

type ErrorOne error<record { boolean fatal; }>;
type ErrorTwo error<record { int code; }>;
type ErrorIntersection ErrorOne & ErrorTwo;

function errorIntersection() returns ErrorOne & ErrorTwo => error ErrorIntersection("error!", code = 1234, fatal = false);

type Foo record {|
    int i = 1;
|};

function recordIntersection(Foo foo) returns (readonly & Foo)|int|(string[] & readonly) => foo.cloneReadOnly();

function nilableIntersection1() returns (int[] & readonly)? => ();

function nilableIntersection2() returns ()|(int[] & readonly) => [1, 2];

function testExprsInDoAndOnFail() {
    do {
        Foo f = {i: 10};
    } on fail error e {
        _ = testParameterizedType1(string);
    }
}

function testDependentlyTypedSignatures() {
    PersonObj p = new("John Doe");
    int x = p.depFoo("bar", 10, 20);
}

function testExpr() {
    TestClass tc = new;
    int x = tc.testFn();
}

function testTableConstructor() {
    table<Person> key(id) tbl1 = table key(id) [
        {id: 1001, name: "John Doe"}
    ];
}

function testXMLAttributeAccess() {
    xml x = xml `<root attr="attr-val"><a></a><b></b></root>`;
    string|error val = x.attr;

    xmlns "www.url.com" as ns;
    x = xml `<root attr="attr-val" ns:attr="attr-with-ns-val"><a></a><b></b></root>`;
    val = x.ns:attr;

    string|error|() val2 = x?.attr;
}

function testAnnotAccess() {
    Annot? annot = Person.@v1;
}

function testErrorConstructor() {
    string msg = "Op failed";
    error err1 = error(msg, a = "foo");
    Error err2 = error Error(msg, err1, b = 10);
}

function testAnonFuncs() {
    string name = "Pubudu";
    var greet = function () returns string => string `Hello ${name}`;

    var fn1 = @v1 isolated function(int x, int y = 0, string... z) {
        int total = x + y;
    };

    int c = 10;
    function (int, int) returns int add = (a, b) => a + b + c;
}

function testLetExpr() {
    var v = let int x = 10, @v1{foo: "Foo"} Person p = {id: 10, name: "J. Doe"} in x * 2;
}

function testTypeOf() {
    int x = 10;
    typedesc<anydata> td = typeof (x + 25);
}

function testBitwiseExpr() {
    int x = 16284;
    int res = x & 24;
    res = x ^ 2048;
    res = 12345 | x;
}

function testLogicalAndConditionalExpr() {
    boolean expr = 10 < 20;
    boolean res = expr && true;
    res = (20 > 15) || expr;

    string? name = ();
    string greet = "Hello " + (name ?: "Foo");
}

function testShiftExpr() {
    byte shift = 8;
    int res = 16 << shift;
    res = 16 >> 4;
    res = shift >>> 2;
}

function testRangeExpr() {
    int x = 256;
    int a = 10;
    int b = 20;
    var v1 = x...1024;
    var v2 = b...x;
    var v3 = a..<b;
}

function testTrapExpr() {
    int|error res = trap panics();
}

function testXMLFilterExpr() {
    xml x1 = xml `<ns:root></ns:root>`;
    xml x2 = x1.<ns:*>;
    x2 = x1.<ns:*|k:*>;
}

function testXMLStepExpr() {
    xml x1 = xml `<ns:root><ns:child></ns:child></ns:root>`;
    xml x2 = x1/<ns:child>;
    x2 = x1/*;
    x2 = x1/<*>;
    x2 = x1/**/<ns:child>;
    x2 = x1/<ns:child>[2];
    x2 = x1/**/<ns:child|k:child>;
    string s = x1/**/<ns:child>.toString();
}

function testGroupExpr() {
    int x = ((((10 + 5) / 3) + 7));
}


// utils

class PersonObj {
    string name;

    function init(string name) {
        self.name = name;
    }

    function getName() returns string => self.name;

    function depFoo(string s, typedesc<anydata> td = <>, int... rest) returns td = external;
}

function foo() returns string|error => "foo";

public class Listener {

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function detach(service object {} s) returns error? {
    }

    public function attach(service object {} s, string[]? name = ()) returns error? {
    }
}

class TestClass {
    function testFn() returns int|error {
        return 1;
    }
}

@v1 {
    foo: "bar"
}
type Person record {|
    readonly int id;
    string name;
|};

type Annot record {
    string foo;
};

public annotation Annot v1 on type, var;

type Error error<record {}>;

function panics() returns int {
    panic error("Failed");
}

xmlns "foo" as ns;
xmlns "bar" as k;

public function testFutureRes() {
    future<int> futureResult = fooFn();
}

function fooFn() returns future<int> {
    return start barFn();
}

function barFn() returns int {
    return 10;
}

function testRegexp() {
    _ = re `[a-z0-9]+`;
    _ = re ``;
    _ = re `a`;
    _ = re `[a|b]+c`;
    string:RegExp reg1 = re `a|b`;
    regexp:RegExp reg2 = re `[a-z]+`;
}

function name() {
    _ = xml ``;
}

function testXMLStepExprWithExtension() {
    xml x = xml `<root><parent><child>1</child></parent><parent>0</parent></root>`;
    xml x2 = x/*[0];
    x2 = x/*.get(0);
    x2 = x/*[1].first().<child>;
}
