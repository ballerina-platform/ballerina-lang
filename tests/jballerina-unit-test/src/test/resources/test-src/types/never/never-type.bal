// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

//------------ Testing a function with 'never' return type ---------

function functionWithNeverReturnType() returns never {
    panic error("Panic occured in function with never return");
}

function testNeverReturnTypedFunctionCall() {
    functionWithNeverReturnType();
}

//------------ Testing record type with 'never' field ---------

type InclusiveRecord record {
    int j;
    never p?;
};

type ExclusiveRecord record {|
    int j;
    never p?;
|};

function testInclusiveRecord() {
    InclusiveRecord inclusiveRecord = {j:0, "q":1};
}

function testExclusiveRecord() {
    ExclusiveRecord exclusiveRecord = {j:0};
}


//------------- Testing XML<never> -----------------

function testXMLWithNeverType() {
    xml<never> x = <xml<never>> 'xml:concat();  //generates an empty XML sequence and assign it to XML<never>
    xml<never> a = xml ``;
    xml<never> b = <xml<never>> 'xml:createText("");
    xml c = xml ``;
    'xml:Text d = xml ``;
    xml<'xml:Text> e = a;
    xml f = a;
    xml<xml<never>> g = xml ``;
    xml<xml<'xml:Text>> h = xml ``;
    string empty = "";
    'xml:Text j = xml `${empty}`;
    xml k = xml `${empty}`;
    xml<never>|'xml:Text l = xml ``;
    xml<never> & readonly m =  xml ``;
    string|'xml:Text n = a;

    xml<xml<'xml:Element>> t = xml ``;
    xml<'xml:Element> u = xml ``;
    xml<xml<'xml:Comment>> v = xml ``;
    xml<'xml:Comment> w = xml ``;
    xml<xml<'xml:ProcessingInstruction>> z = xml ``;
    xml<'xml:ProcessingInstruction> y = xml ``;
}

//---------------Test 'never' types with 'union-type' descriptors ------------
function testNeverWithUnionType1() {
    string|never j;
}

function testNeverWithUnionType2() {
    float|(int|never) j;
}

function testNeverWithUnionType3() {
    string|never j = "sample";
    string h = j;
}

// -------------Test 'never' with table key constraints --------------
type Person record {
  readonly string name;
  int age;
};

type PersonalTable table<Person> key<never>;

function testNeverWithKeyLessTable() {
    PersonalTable personalTable = table [
        { name: "DD", age: 33},
        { name: "XX", age: 34}
    ];
}

type SomePersonalTable table<Person> key<never|string>;

function testNeverInUnionTypedKeyConstraints() {
    SomePersonalTable somePersonalTable = table key(name) [
        { name: "MM", age: 33},
        { name: "PP", age: 34}
    ];
}

// --------------Test 'never' with 'future' type ----------------------

function testNeverAsFutureTypeParam() {
    future<never> someFuture;
}


// --------------Test 'never' with 'map' type ----------------------

function testNeverAsMappingTypeParam() {
    map<never> mp;
}

function testNeverWithCallStmt() {
    foo();
}

function testNeverWithStartAction1() {
    future<never> f = start foo();
    any|error result = trap wait f;
    assertEquality(true, result is error);
    if (result is error) {
        assertEquality("Bad Sad!!", result.message());
    }
}

function testNeverWithStartAction2() {
    Bar bar = new (12);
    future<never> f = start bar.barFunc();
    any|error result = trap wait f;
    assertEquality(true, result is error);
    if (result is error) {
        assertEquality("Bad Sad!!", result.message());
    }
}

function testNeverWithTrapExpr1() {
    error err = trap foo();
    assertEquality("Bad Sad!!", err.message());
}

function testNeverWithTrapExpr2() {
    Bar bar = new (12);
    error err = trap bar.barFunc();
    assertEquality("Bad Sad!!", err.message());
}

function testNeverWithMethodCallExpr() {
    Bar bar = new (12);
    bar.barFunc();
}

function foo() returns never {
  error e = error("Bad Sad!!");
  panic e;
}

class Bar {
    public int val;

    function init(int val) {
        self.val = val;
    }

    function barFunc() returns never {
        error e = error("Bad Sad!!");
        panic e;
    }
}

function testNeverWithIterator1() {
    map<never> x = {};
    record {| never value; |}? y = x.iterator().next();
    assertEquality((), y);
}

function testNeverWithIterator2() {
    map<never> x = {};
    var y = x.iterator().next();
    assertEquality((), y);
}

function testNeverWithIterator3() {
    never[] x = [];
    record {| never value; |}? y = x.iterator().next();
    assertEquality((), y);
}

function testNeverWithIterator4() {
    xml<never> x = xml ``;
    record {| never value; |}? y = x.iterator().next();
    assertEquality((), y);
}

type NeverTable table<map<never>>;

function testNeverWithIterator5() {
    NeverTable x = table [
    ];
    record {| map<never> value; |}? y = x.iterator().next();
    assertEquality((), y);
}

type Bunny record {|
    string name;
|};

type BunnyTable table<Bunny> key<never>;

function testNeverWithIterator6() {
    BunnyTable x = table [
            {"name": "ABC"},
            {"name": "DEF"}
    ];
    record {| Bunny value; |}? y = x.iterator().next();
    record {| Bunny value; |} z = {"value":{"name":"ABC"}};
    assertEquality(z, y);
}

function testNeverWithForeach1() {
    map<never> x = {};
    any y = "ABC";
    foreach never a in x {
        y = "DEF";
    }
    assertEquality("ABC", y);
}

function testNeverWithForeach2() {
    map<never> x = {};
    any y = "ABC";
    foreach var a in x {
        y = "DEF";
    }
    assertEquality("ABC", y);
}

type Foo record {|
|};

function testNeverWithForeach3() {
    Foo x = {};
    any y = "ABC";
    foreach never a in x {
        y = "DEF";
    }
    assertEquality("ABC", y);
}

function testNeverWithForeach4() {
    Foo x = {};
    any y = "ABC";
    foreach var a in x {
        y = "DEF";
    }
    assertEquality("ABC", y);
}

function testNeverWithForeach5() {
    never[] x = [];
    any y = "ABC";
    foreach never a in x {
        y = "DEF";
    }
    assertEquality("ABC", y);
}

function testNeverWithForeach6() {
    xml<never> x = xml ``;
    any y = "ABC";
    foreach never a in x {
        y = "DEF";
    }
    assertEquality("ABC", y);
}

function testNeverWithForeach7() {
    xml<never> x = <xml<never>> xml:createText("");
    any y = "ABC";
    foreach var a in x {
        y = "DEF";
    }
    assertEquality("ABC", y);
}

function testNeverWithForeach8() {
    NeverTable x = table [
    ];
    any y = "ABC";
    foreach var a in x {
        y = a;
    }
    assertEquality("ABC", y);
}

function testNeverWithFromClauseInQueryExpr1() {
    map<never> x = {};
    var y = from never a in x select 1;
    assertEquality(0, y.length());
}

function testNeverWithFromClauseInQueryExpr2() {
    xml<never> x = xml ``;
    xml y = from never a in x select xml:concat();
    assertEquality(xml:concat(), y);
}

function testNeverWithFromClauseInQueryExpr3() {
    never[] x = [];
    int[] y = from never a in x select 1;
    assertEquality(0, y.length());
}

function testNeverWithFromClauseInQueryExpr4() {
    never[] x = [];
    never[] y = [];
    int[] z = from var a in x
                join never b in y
                on 1 equals 1
                select 1;
    assertEquality(0, z.length());
}

function testNeverWithFromClauseInQueryExpr5() {
    NeverTable x = table [
    ];
    map<never>[] y = from var a in x select a;
    assertEquality(0, y.length());
}

type RestRecord record {|
    string someName;
    never...;
|};

function testNeverWithRestParamsAndFields() {
    RestRecord x = {someName: "ABC"};
    var y = testNeverWithRestParams();
}

function testNeverWithRestParams(record {| never x; |}... rec) {
}

function testNeverWithServiceObjFunc() {
    service object {} object1 = service object {
        remote function invoke1(string a) returns never {
            error e = error(a);
            panic e;
        }
    };
}

function testNeverSubtyping() {
    ()|error x1 = trap foo();
    assertEquality(true, x1 is error);
    if (x1 is error) {
        assertEquality("Bad Sad!!", x1.message());
    }

    int|error x2 = trap blowUp1();
    assertEquality(true, x2 is error);
    if (x2 is error) {
        assertEquality("Bad Sad!!", x2.message());
    }

    int|error x3 = trap blowUp2();
    assertEquality(true, x3 is error);
    if (x3 is error) {
        assertEquality("Bad Sad!!", x3.message());
    }

    error? x4 = trap blowUp2();
    assertEquality(true, x4 is error);
    if (x4 is error) {
        assertEquality("Bad Sad!!", x4.message());
    }

    int|error x5 = trap blowUp3();
    assertEquality(true, x5 is error);
    if (x5 is error) {
        assertEquality("Bad Sad!!", x5.message());
    }
}

function blowUp1() returns int {
    panic error("Bad Sad!!");
}

function blowUp2() returns never {
    panic error("Bad Sad!!");
}

function blowUp3() returns int|never {
    panic error("Bad Sad!!");
}

function testValidNeverReturnFuncAssignment() {
    function () returns record {| never val; |} rec = foo;
    any|error err = trap rec();
    assertEquality(true, err is error);
    if err is error {
       assertEquality("Bad Sad!!", err.message());
    }
}

function testValidNeverReturnFuncAssignment2() {
    function () returns never x = bar;
}

function bar() returns record {| never x; |} {
    panic error("error!");
}

function testNeverWithAnydata() {
    map<never> a = {};
    never[] b = [];

    anydata m = a;
    assertEquality(true, m is map<anydata>);
    map<anydata> mp = <map<anydata>> m;
    assertEquality(0, mp.length());

    anydata n = b;
    assertEquality(true, n is anydata[]);
    anydata[] np = <anydata[]> n;
    assertEquality(0, np.length());

    anydata x1 = baz1();
    assertEquality(true, x1 is map<anydata>);
    map<anydata> xp = <map<anydata>> x1;
    assertEquality(0, xp.length());

    anydata x2 = baz2();
    assertEquality(true, x2 is anydata[]);
    anydata[] xxp = <anydata[]> x2;
    assertEquality(0, xxp.length());
}

function baz1() returns map<never> {
    return {};
}

function baz2() returns never[] {
    return [];
}

function testNeverInSubsequentInvocations() {
    int|error i = trap num();
    assertEquality(true, i is error);
    if i is error {
        assertEquality("unreachable code!!", i.message());
    }
}

function num() returns int {
    unreached();
}

function unreached() returns never {
    panic error("unreachable code!!");
}

function testNeverInGroupedExpr() {
    error? err = trap (unreached());
    assertEquality(true, err is error);
    if err is error {
        assertEquality("unreachable code!!", err.message());
    }
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
