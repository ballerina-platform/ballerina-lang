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

function testInvaidAssignmentToVariable() {
    int i = "hello";
}

class A {
    string x = "hello";
}

function testInvalidObjectLvExpr() {
    A a = new;
    a.y = 1;
    a["x"] = "hello";
}

type B record {
    string x = "world";
    int y?;
};

function testInvalidOptionalFieldAccesOnLhs() {
    B m = { y: 34 };
    m?.x += "qwer";
    m?.y += 1;
}

class ObjectWithInvalidFillingReadOnUninitializedField {

    map<int[]> s;

    function init() {
        self.s["one"][0] = 1;
    }
}

type C record {
    map<int>? m = ();
};

class D {
    C c = {};
}

function testInvalidFillingReadOnInitializedObjectField() {
    D d = new;
    d.c.m["one"] = 1;
}

type E record {
    string x;
};

type F record {
    map<int>? m = ();
    E? e;
};

function testInvalidRecordFieldAccessLvExpr() {
    E e = { x: "hello" };
    e.y = 1;

    F f = { m: { "one": 1 }, e: () };
    f.m["two"] = 2;
    f.e.x = "ddd";
}
