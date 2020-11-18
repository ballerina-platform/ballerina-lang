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
    return j2 == "qwer";
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
        return je.message() == "{ballerina}JSONOperationError" && je.detail()["message"].toString() == "JSON value is not a mapping";
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
        floatVar += 2;
        decimalVar += 3;
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
    panic error(ASSERTION_ERR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
