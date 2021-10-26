// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Baz record {
    Qux q;
};

type Qux record {
    string name;
    int id?;
};

function testFieldAccessWithOptionalFieldAccess1() returns boolean {
    Baz b = { q: { name: "John" } };
    string name = b.q?.name;
    int? id = b.q?.id;
    return name == "John" && id is ();
}

function testFieldAccessWithOptionalFieldAccess2() returns boolean {
    json j1 = { a: 1, b: { c: "qwer", d: 12.0 } };
    json|error j2 = j1?.b.c;
    return (checkpanic j2) == "qwer";
}

function testFieldAccessWithOptionalFieldAccess3() returns boolean {
    json j1 = { a: 1, b: { c: "qwer", d: 12.0 } };
    json|error j2 = j1?.a.b;
    json|error j3 = j1?.d.b;
    return assertNonMappingJsonError(j2) && assertNonMappingJsonError(j3);
}

type Alpha record {
    Beta?[] betas = [];
};

public type Beta record {|
    int i;
    string s?;
|};

function testFieldOptionalFieldAndMemberAccess1() returns boolean {
    string sval = "test string";

    Alpha a = {
        betas: [
            { i: 1, s: sval },
            { i: 2 }
        ]
    };

    string? s1 = a.betas[0]?.s;
    string? s2 = a.betas[1]?.s;

    return s1 == sval && s2 is ();
}

function testFieldOptionalFieldAndMemberAccess2() returns boolean {
    string sval = "test string";

    Alpha a = {
        betas: [
            (),
            { i: 1, s: sval }
        ]
    };

    string? s1 = a.betas[0]?.s;
    string? s2 = a.betas[1]?.s;

    return s1 is () && s2 == sval;
}

function testFieldOptionalFieldAndMemberAccess3() {
    Alpha a = {
        betas: []
    };

    string? s1 = a.betas[0]?.s;
}

function testAccessOptionalFieldWithoutOptionalFieldAccess() returns boolean{
    Beta b1 = { i: 1, s: "test" };
    Beta b2 = { i: 1 };
    string? s1 = b1.s;
    string? s2 = b2.s;

    return s1 == "test" && s2 is ();
}

public class Gamma {
    Delta? delta;

    public function init(Delta? d) {
        self.delta = d;
    }
}

public type Delta record {
    Status status = ();
};

public type Status PASSED | FAILED | ();

public const PASSED = "passed";
public const FAILED = "failed";
public const NONE = ();

function testMemberAccessOnNillableObjectField() returns boolean {
    string st = "test string";
    Gamma g1 = new({});
    Gamma g2 = new({ status: FAILED, "oth": st });

    string key1 = "status";
    string key2 = "oth";
    return g1.delta["status"] == () && g2.delta["status"] == FAILED && g2.delta[key1] == FAILED &&
            g2.delta[key2] == st;
}

function testNilLiftingOnMemberAccessOnNillableObjectField() returns boolean {
    Gamma g = new(());
    return g.delta["status"] == ();
}

function assertNonMappingJsonError(json|error je) returns boolean {
    if (je is error) {
        var detailMessage = je.detail()["message"];
        string detailMessageString = detailMessage is error? detailMessage.toString(): detailMessage.toString();
        return je.message() == "{ballerina}JSONOperationError" && detailMessageString == "JSON value is not a mapping";
    }
    return false;
}

const ASSERTION_ERR_REASON = "AssertionError";

function testSimpleTypeAccessOnFunctionPointer() {
    boolean booleanVar = false;
    int intVar = 2;
    float floatVar = 2.5;
    decimal decimalVar = 3.5;
    string stringVar = "test_string";

    var updateVariables = function () {
        booleanVar = true;
        intVar += 1;
        floatVar += 2.0;
        decimalVar += 3d;
        stringVar = "updated_test_string";
    };

    updateVariables();
    assertEquality(true, booleanVar);
    assertEquality(3, intVar);
    assertEquality(4.5, floatVar);
    assertEquality(<decimal> 6.5, decimalVar);
    assertEquality("updated_test_string", stringVar);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }
    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

type R record {| int x; |};
class O { int x = 225; }

public function testAccessOnGroupedExpressions() returns error|boolean {
    boolean result = true;

    // access on checked, cast expressions
    R a = {x: 3};
    R|error b = a;
    any c = a;
    R d = (check b);
    int e = (check b).x;
    int f = (<R>c).x;
    int g = castAndGetX(c);
    result = result && e == 3;
    result = result && f == 3;
    result = result && g == 3;

    // access on int range expressions
    record {| int value; |}? h = (1 ..< 5).iterator().next();
    if (!(h is ())) {
        int val = h.value;
        result = result && val == 1;
    } else {
        result = false;
    }

    // access on unary, binary, ternary expressions
    string s = (!false).toString();
    s = (s == "true" && !false).toString();
    s = (s == "true" ? true : false).toString();
    result = result && s == "true";

    // access on raw template expressions
    string name = "john";
    int l = (`Hello ${name}!`).strings.length();
    result = result && l == 2;

    // literal, simple varref, group-expr, list-construct.
    int i1 = 1;
    s = (1 + i1 + ([1, 2])[0] + [2, 3, 4][1]).toString();
    result = result && s == "6";

    // unary, binary.
    boolean[] bArr = [true, false, true];
    s = ((bArr[0] || !bArr[1]) && bArr[2]).toString();
    result = result && s == "true";

    // bitwise shift.
    int i2 = 64;
    s = (1 << i2).toString();
    result = result && s == "1";

    // chain invocations, builtin functions.
    boolean bool = true;
    s = bool.cloneReadOnly().toString();
    result = result && s == "true";

    // checkpanic, native conversions
    map<anydata> m = {"x":2};
    s = (checkpanic m.cloneWithType(R)).x.toString();
    result = result && s == "2";

    // elvis
    int|() i3 = 120;
    s = (i3 ?: 111).toString();
    result = result && s == "120";

    // error constructor
    s = (error("msg")).message();
    result = result && s == "msg";

    // lambda
    function (int) returns int lambda = (x) => x + 5;
    s = lambda(5).toString();
    result = result && s == "10";

    // let expr
    s = (let int x = 4 in 2 * x * 2).toString();
    result = result && s == "16";

    // object constructor
    s = new O().x.toString();
    result = result && s == "225";

    // trap
    s = (trap trapTest()).message();
    result = result && s == "msg";

    s = (typeof (12.5f)).toString();
    result = result && s == "typedesc 12.5";
    return result;
}

function castAndGetX(any a) returns int {
    // access on cast expressions
    return (<R>a).x;
}

function trapTest() returns error {
    panic error("msg");
}
