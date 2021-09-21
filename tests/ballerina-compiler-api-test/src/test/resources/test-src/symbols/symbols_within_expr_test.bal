// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testLiterals() {
    () nil = ();
    boolean b = true;
    int x = 10;
    float f = 12.34;
    string s = "foo";
    byte[] b64 = base64 `aGVsbG8=`;
}

function testTemplateExprs() {
    int i = 10;
    string s = string `foo ${i}`;
    xml x = xml `<age>${i}</age>`;
    'object:RawTemplate rt = `bar ${i}`;
}

function testStructuralConstructors() {
    anydata[] arr1 = [hundred, PI];
    arr1 = [];
    var tup = [greet, PI, 10];

    map<anydata> m1 = {a: 10, "b": PI};
    FooRec rec1 = {x: hundred, "y": greet, [greet]: "Another " + greet, "pi": PI};
    string y = "foo";
    rec1 = {x: 10, y, ...m1};

    table<Person> key(id) tbl1 = table key(id) [
        {id: 1001, name: "John Doe"}
    ];

    var m2 = {id: 1002, name: "Jane Doe"};

    var tbl2 = table key(id) [
        {id: 1001, name: "John Doe"},
        {id: 1002, name: "Jane Doe"}
        //{...m}
    ];
}

function testNewExpr() {
    string name = "John Doe";
    PersonClz p = new(name);
    p = new PersonClz(name);

    _StreamImplementor si = object {
        public isolated function next() returns record {|FooRec value;|}? {
            return ();
        }
    };
    var st = new stream<FooRec>(si);
}

function testAnnotTagReference() {
    Annot? annot = FooRec.@v1;
}

function testFunctionAndMethodCallExpr() {
    _ = rand(hundred, y = 99);
    _ = rand(hundred, PI);

    PersonClz p = new("J. Doe");
    _ = p.baz(hundred, PI);
}

function testErrorConstructor() {
    string msg = "Op failed";
    error c = error("Cause");
    Error1 err1 = error Error1(msg, c, code = hundred);
    Error2 err2 = error Error2(msg, c, a = "foo", b = 10);
    error err3 = error(msg, c, a = "foo");
}

function testTypeCast() {
    anydata ad = 10;
    int x = <@v1{foo: "bar"} int>ad;
}

function testTypeOf() {
    int x = 10;
    typedesc<anydata> td = typeof (x + 25);
}

function testUnaryExpr() {
    int x = 10;
    int y = +x;
    y = -x;
    y = ~x;
    boolean tr = true;
    boolean b = !tr;
}

function testCheckingAndTrappingExprs() returns error? {
    int x = check getError();
    x = checkpanic getError();
    int|error y = trap panickingFn();
}

// utils

const PI = 3.14;
int hundred = 100;
string greet = "Hello World!";

@v1 {
    foo: "bar"
}
type FooRec record {
    int x;
    string y;
};

type Person record {|
    readonly int id;
    string name;
|};

class PersonClz {
    string name;

    function init(string name) {
        self.name = name;
    }

    function baz(int x, int y = 10, string... z) returns string => self.name;
}

type _StreamImplementor object {
    public isolated function next() returns record {|FooRec value;|}?;
};

type Annot record {
    string foo;
};

public annotation Annot v1 on type;

function rand(int x, int y = 0, float... z) returns string => "random str";

type Error1 error<record {int code;}>;
type Error2 error<record {}>;

function getError() returns Error2|int => error Error2("Checking Expr");

function panickingFn() returns int => panic error("Panicked");
