// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type OpenEmployee record {
    string name;
    int id;
};

type ClosedEmployee record {
    string name;
    int id;
};

function testBooleanRefEquality(boolean a, boolean b) returns boolean {
    return a === b;
}

function testIntRefEquality(int a, int b) returns boolean {
    return a === b;
}

function testByteRefEquality(byte a, byte b) returns boolean {
    return a === b;
}

function testFloatRefEquality(float a, float b) returns boolean {
    return a === b;
}

function testStringRefEquality(string a, string b) returns boolean {
    return a === b;
}

function testRefEqualityToNil(any a) returns boolean {
    return a === ();
}

function testOpenRecordRefEqualityPositive() returns boolean {
    OpenEmployee e1 = { name: "Em", id: 4000 };
    OpenEmployee e2 = e1;

    OpenEmployee e3 = { name: "Em" };
    OpenEmployee e4 = e3;

    OpenEmployee e5 = { name: "Em", id: 4000, dept: "finance" };
    OpenEmployee e6 = e5;

    OpenEmployee e7 = {};
    OpenEmployee e8 = e7;

    return (e1 === e2) && (e3 === e4) && (e5 === e6) && (e7 === e8);
}

function testOpenRecordRefEqualityNegative() returns boolean {
    OpenEmployee e1 = { name: "Em", id: 4000 };
    OpenEmployee e2 = {};

    OpenEmployee e3 = { name: "Em", area: 51 };
    OpenEmployee e4 = { name: "Em", area: 51 };

    OpenEmployee e5 = { name: "Em", id: 4100 };
    OpenEmployee e6 = { name: "Em", id: 4100 };

    return (e1 === e2) || (e3 === e4) || (e5 === e6);
}


function testClosedRecordRefEqualityPositive() returns boolean {
    ClosedEmployee e1 = { name: "Em", id: 4000 };
    ClosedEmployee e2 = e1;

    ClosedEmployee e3 = {};
    ClosedEmployee e4 = e3;

    ClosedEmployee e5;
    ClosedEmployee e6 = e5;

    return (e1 === e2) && (e3 === e4) && (e5 === e6);
}

function testClosedRecordRefEqualityNegative() returns boolean {
    ClosedEmployee e1 = { name: "Em", id: 4000 };
    ClosedEmployee e2 = {};

    ClosedEmployee e3 = { name: "Em", id: 4000 };
    ClosedEmployee e4 = { name: "Em", id: 4000 };

    return (e1 === e2) || (e3 === e4);
}

function testArrayRefEqualityPositive() returns boolean {
    int[3] a;
    int[3] b = a;

    boolean refEquals = a === b;

    a = [1, 2, 3];
    b = a;

    refEquals = refEquals && a === b;

    (boolean|float)[][] c;
    (boolean|float)[][] d = c;

    c = [[true, 1.3], [false, false, 12.2]];
    d = c;

    return refEquals;
}

function testArrayRefEqualityNegative() returns boolean {
    int[2] a;
    int[2] b;

    boolean refEquals = a === b;

    a = [1, 2];
    b = [1, 2];

    refEquals = refEquals && a === b;

    return refEquals;
}

function checkMapRefEqualityPositive() returns boolean {
    map m1;
    map m2 = m1;

    map<string> m3 = {};
    map<string> m4 = m3;

    boolean equals = m1 === m2 && m3 === m4;

    m1["one"] = 1;
    m1["two"] = "two";
    m1["three"] = 3.0;
    m2 = m1;

    m3.last = "last";
    m3["a"] = "a";
    m4 = m3;

    return equals && m1 === m2 && m3 === m4;
}

function checkMapRefEqualityNegative() returns boolean {
    map m1;
    map m2;

    boolean equals = m1 === m2;

    m1.one = "hi";
    m2.one = "hi";

    equals = equals && m1 === m2;

    map<int> m3;
    map<int> m4;

    m3.one = 1;
    m4.one = 1;

    return equals && m3 === m4;
}

function checkTupleRefEqualityPositive() returns boolean {
    (string, int) t1 = ("", 0);
    (string, int) t2 = t1;

    (string, int, OpenEmployee) t3 = ("hi", 0, { name: "Em" });
    (string, int, OpenEmployee) t4 = t3;

    return t1 === t2 && t3 === t4;
}

function checkTupleRefEqualityNegative() returns boolean {
    (boolean, int) t1 = (false, 0);
    (boolean, int) t2 = (false, 0);

    (string, ClosedEmployee) t3 = ("hi", { nickname: "EmZee" });
    (string, ClosedEmployee) t4 = ("hi", { nickname: "Em" });

    return t1 === t2 || t3 === t4;
}

function checkJsonRefEqualityPositive() returns boolean {
    json j = { Hello: "World" };
    json j2 = j;

    boolean equals = j === j2;

    j = "Hello";
    j2 = j;

    return equals && j === j2;
}

function checkJsonRefEqualityNegative() returns boolean {
    json j = { Hello: "World" };
    json j2 = { Hello: "World" };

    boolean equals = j === j2;

    j = "Hello";
    j2 = "Hello";

    return equals || j === j2;
}

function testIntByteRefEqualityPositive() returns boolean {
    byte a;
    int b;

    boolean equals = a === b;

    a = 5;
    b = 5;

    return equals && (a === b);
}

function testIntByteEqualityNegative() returns boolean {
    int[] a;
    byte[] b;

    boolean equals = a === b;

    int[] c = [2, 1];
    byte[] d = [2, 1];

    equals = equals || (c === d);

    byte[][] e = [[23, 45], [123, 43, 68]];
    (int|float)[][] f = [[23, 45], [123, 43, 68]];

    equals = equals || (e === f);

    map<(byte, boolean)> g;
    map<(int, boolean)> h;

    g.one = (10, true);
    g.two = (1, false);
    h.one = (10, true);
    h.two = (1, false);

    equals = equals || (g === h);

    return equals && (g == h) && !(g != h);
}

function testXmlRefEqualityPositive() returns boolean {
    xml x1;
    xml x2 = x1;

    xml x3 = xml `<book><name>The Lost World<!-- I'm a comment --></name></book>`;
    xml x4 = x3;

    return x1 === x2 && x3 === x4;
}

function testXmlRefEqualityNegative() returns boolean {
    xml x1 = xml `<book>The Lot World</book>`;
    xml x2 = xml `<book>The Lost World</book>`;

    xml x3 = xml `<book><name>The Lost World<!-- I'm a comment --></name></book>`;
    xml x4 = xml `<book><name>The Lost World<!-- I'm a comment --></name></book>`;
    return x1 === x2 || x3 === x4;
}
