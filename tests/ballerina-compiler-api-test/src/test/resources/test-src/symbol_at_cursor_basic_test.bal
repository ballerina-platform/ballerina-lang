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

string aString = "foo";
int anInt = 10;

function test() {
    string greet = "Hello " + aString;

    var greetFn = function (string name) returns string => HELLO + " " + name;
    greet = greetFn("Pubudu");

    if (true) {
        int a = 20;

        while(true) {
            var x = 0;

        }

        int y = 10;
    }

    int z = 20;
}

function testTypeRefs() {
    Person p = {};
    PersonObj pObj = new("Jane", "Doe");
    string name = pObj.getFullName();
    name = p.name;
    name = <string>p["name"];
}

const HELLO = "Hello";

type Person record {|
    string name = "John Doe";
|};

class PersonObj {
    string fname;
    string lname;

    public function init(string fname, string lname) {
        self.fname = fname;
        self.lname = lname;
    }

    public function getFullName() returns string {
        return self.fname + " " + self.lname;
    }
}

enum Colour {
    RED, GREEN, BLUE
}

function testFunctionCall() {
    test();
}

# The key algorithms supported by the Crypto module.
public type KeyAlgorithm RSA;

# The RSA algorithm.
public const RSA = "RSA";

function testExprs()  {
    RSA rsa = "RSA";
    string s = RSA;
}

@v1 {
    val: 2
}
[int, string] [intVar, stringVar] = [1, "myString"];

public function func(@v2 { val: "v63 value required" } int id,
                     @v2 { val: "v63 value defaultable" } string s = "hello",
                     @v2 { val: "v63 value rest" } float... others) returns @v3 float {
    return 1.0;
}

const annotation map<int> v1 on source var;
annotation Annot v2 on parameter;
public annotation v3 on return;
const annotation map<string> v4 on source annotation;

@v4 {
    str: "v4 value"
}
const annotation map<string> v5 on source annotation;

type Annot record {
    string val;
};

function testIgnoreSym() {
    _ = 3.14;
}

public type TEST_TYPE int[CONST1];
public const CONST1 = 12;

function func1(string a, int b) returns function (int,function (int, int) r)  {

}

function exprBodyScope(string myStr) returns string => myStr;

enum Keyword {
  nil,
  'boolean,
  'int,
  'string,
  'never,
  'any,
  union,
  intersection
}

type Atype Keyword;

public type Path int[];

public type ParseDetail record {
    Path path;
};

public type ParseError error<ParseDetail>;

function testOnFail() returns boolean {
    do {
        boolean ok = true;
        return ok;
    }
    on fail ParseError err {
        return false;
    }
}

function foo() returns string {
    return "abc";
}

function test(string name, int age) {
    Person p = {
        name: name,
        age,
        [foo()]: "FOO"
    };
}

function testAnnotTagReference() {
    Annot? annot = FooRec.@v5;
}

@v5 {
    foo: "bar"
}
type FooRec record {
    int x;
    string y;
};

type AnnotRec record {
    string foo;
};

public annotation AnnotRec v5 on type;

type AnnotRec2 record {

};

const annotation AnnotRec2 WorkerAnnot on source worker;

function fn() {
    future<int?> f = @WorkerAnnot start add(1, 2);
}

function add(int a, int b) returns int {
    return 1;
}

int[] x1 = [1, 2 , 3];
int[] y1 = [...x1, 4];

function testListConstructorSpreadOp() {
    int[] x2 = [1, 2 , 3];
    int[] y2 = [...x2, 4];
    int[] y3 = [...y1, 5];
}

function testTypeofSymbol(error result) {
    string errPrefix = "Payload binding failed: ";
    var errMsg = result.detail()["message"];
}

public type ReturnValue02 readonly & string;

ReturnValue02 stringVar01 = "ballerina";

public type ReadOnlyPrimitiveUnion readonly & string|int;

function testAlternateReceive() {
    worker w1 {
        3 -> w3;
    }

    worker w2 {
        4 -> w3;
    }

    worker w2 {
        int _ = <- w1|w2;
    }
}

function testMultipleReceive() {
    worker w1 {
        5 -> w3;
    }

    worker w2 {
        6 -> w3;
    }

    worker w2 {
        _ = <- {a: w1, b: w2};
    }
}
