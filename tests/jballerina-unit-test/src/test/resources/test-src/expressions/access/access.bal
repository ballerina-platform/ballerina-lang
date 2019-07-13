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

function assertNonMappingJsonError(json|error je) returns boolean {
    if (je is error) {
        return je.reason() == "{ballerina}JSONOperationError" && je.detail()?.message == "JSON value is not a mapping";
    }
    return false;
}
