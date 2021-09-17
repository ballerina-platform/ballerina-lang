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
}

type _StreamImplementor object {
    public isolated function next() returns record {|FooRec value;|}?;
};

type Annot record {
    string foo;
};

public annotation Annot v1 on type;
