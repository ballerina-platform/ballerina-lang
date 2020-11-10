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
    string name = "";
    int id = 0;
};

type OpenPerson record {
    string name;
    int id;
};

type ClosedEmployee record {|
    string name = "";
    int id = 0;
|};

class Abc {
    public string name;
    float salary;
    int id;

    public function init (string name, float salary, int id) {
        self.name = name;
        self.salary = salary;
        self.id = id;
    }
}

class Def {
    public string name;
    float salary;
    int id;
    private int idTwo = 0;

    public function init(string name, float salary, int id) {
        self.name = name;
        self.salary = salary;
        self.id = id;
    }
}

function testBooleanRefEquality(boolean a, boolean b) returns boolean {
    return a === b && !(a !== b);
}

function testIntRefEquality(int a, int b) returns boolean {
    return a === b && !(a !== b);
}

function testByteRefEquality(byte a, byte b) returns boolean {
    return a === b && !(a !== b);
}

function testFloatRefEquality(float a, float b) returns boolean {
    return a === b && !(a !== b);
}

function testStringRefEquality(string a, string b) returns boolean {
    return a === b && !(a !== b);
}

function testRefEqualityToNil(any a) returns boolean {
    return a === () && !(a !== ());
}

function testOpenRecordRefEqualityPositive() returns boolean {
    OpenEmployee e1 = { name: "Em", id: 4000 };
    OpenEmployee e2 = e1;

    OpenEmployee e3 = { name: "Em" };
    OpenEmployee e4 = e3;

    OpenEmployee e5 = { name: "Em", id: 4000, "dept": "finance" };
    OpenPerson e6 = e5;

    OpenEmployee e7 = {};
    OpenEmployee e8 = e7;
    OpenEmployee e9 = e7;

    return e1 === e2 && e3 === e4 && e5 === e6 && isRefEqual(e8, e9) && !(e1 !== e2) && !(e3 !== e4) && !(e5 !== e6);
}

function testOpenRecordRefEqualityNegative() returns boolean {
    OpenEmployee e1 = { name: "Em", id: 4000 };
    OpenEmployee e2 = {};

    OpenEmployee e3 = { name: "Em", "area": 51 };
    OpenEmployee e4 = { name: "Em", "area": 51 };

    OpenEmployee e5 = { name: "Em", id: 4100 };
    OpenEmployee e6 = { name: "Em", id: 4100 };

    return e1 === e2 || e3 === e4 || isRefEqual(e5, e6) || !(e1 !== e2) || !(e3 !== e4);
}


function testClosedRecordRefEqualityPositive() returns boolean {
    ClosedEmployee e1 = { name: "Em", id: 4000 };
    ClosedEmployee e2 = e1;

    ClosedEmployee e3 = {};
    ClosedEmployee e4 = e3;
    ClosedEmployee e5 = e3;

    ClosedEmployee e6 = {};
    ClosedEmployee e7 = e6;

    return e1 === e2 && e4 === e5 && isRefEqual(e6, e7) && !(e1 !== e2) && !(e4 !== e5);
}

function testClosedRecordRefEqualityNegative() returns boolean {
    ClosedEmployee e1 = { name: "Em", id: 4000 };
    ClosedEmployee e2 = {};

    ClosedEmployee e3 = { name: "Em", id: 4000 };
    ClosedEmployee e4 = e3;
    e3 = { name: "Em", id: 21 };

    return e1 === e2 || isRefEqual(e3, e4) || !(e1 !== e2);
}

function testArrayRefEqualityPositive() returns boolean {
    int[3] a = [0, 0, 0];
    int[3] b = a;

    boolean refEquals = a === b && !(a !== b);

    a = [1, 2, 3];
    b = a;

    refEquals = refEquals && a === b && !(a !== b);

    (boolean|float)?[][] c = [];
    (boolean|float)?[][] d = c;

    c = [[true, 1.3], [false, false, 12.2]];
    d = c;

    return refEquals && isRefEqual(c, d);
}

function testArrayRefEqualityNegative() returns boolean {
    int[2] a = [0, 0];
    int[2] b = [0, 0];

    boolean refEquals = a === b || !(a !== b);

    a = [1, 2];
    b = [1, 2];

    refEquals = refEquals && a === b || !(a !== b);

    b = a;
    a = [1, 2];

    return refEquals || isRefEqual(a, b);
}

function checkMapRefEqualityPositive() returns boolean {
    map<any> m1 = {};
    map<any> m2 = m1;

    map<string> m3 = {};
    map<string> m4 = m3;

    boolean 'equals = m1 === m2 && !(m1 !== m2) && isRefEqual(m3, m4);

    m1["one"] = 1;
    m1["two"] = "two";
    m1["three"] = 3.0;
    m2 = m1;

    m3["last"] = "last";
    m3["a"] = "a";
    m4 = m3;

    return 'equals && m1 === m2 && m3 === m4 && !(m1 !== m2) && !(m3 !== m4);
}

function checkMapRefEqualityNegative() returns boolean {
    map<any> m1 = {};
    map<any> m2 = {};

    boolean 'equals = m1 === m2 || !(m1 !== m2);

    m1["one"] = "hi";
    m2["one"] = "hi";

    'equals = 'equals && m1 === m2 || !(m1 !== m2);

    map<int> m3 = {};
    map<int> m4 = {};

    m3["one"] = 1;
    m4["one"] = 1;

    return 'equals || m3 === m4 || !(m4 !== m3);
}

function checkTupleRefEqualityPositive() returns boolean {
    [string, int] t1 = ["", 0];
    [string, int] t2 = t1;

    [string, int, OpenEmployee] t3 = ["hi", 0, { name: "Em" }];
    [string, int, OpenEmployee] t4 = t3;

    return t1 === t2 && t3 === t4 && !(t1 !== t2) && !(t3 !== t4);
}

function checkTupleRefEqualityNegative() returns boolean {
    [boolean, int] t1 = [false, 0];
    [boolean, int] t2 = [false, 0];

    [string, ClosedEmployee] t3 = ["hi", { name: "EmZee" }];
    [string, ClosedEmployee] t4 = ["hi", { name: "Em" }];

    return t1 === t2 || t3 === t4 && !(t1 !== t2) && !(t3 !== t4);
}

function checkJsonRefEqualityPositive() returns boolean {
    json j = { Hello: "World" };
    json j2 = j;
    boolean 'equals = j === j2 && !(j2 !== j);

    j = "Hello";
    j2 = j;
    'equals = 'equals && j === j2 && !(j !== j2);

    int[] intArr = [1, 2, 3];
    json[] jArr = intArr;
    int[] intArrTwo = intArr;
    'equals = 'equals && isRefEqual(intArrTwo, jArr);

    string[] strArr = ["hello world", "ballerina"];
    jArr = strArr;
    string[] strArrTwo = strArr;
    'equals = 'equals && isRefEqual(strArrTwo, jArr);

    [string, int] tup = ["hi", 1];
    [string, int] tup1 = tup;
    [json, int] jTup = tup;
    return 'equals && isRefEqual(jTup, tup1);
}

function checkJsonRefEqualityNegative() returns boolean {
    json j = { Hello: "World" };
    json j2 = { Hello: "World" };

    return j === j2 && !(j2 !== j);
}

function testIntByteRefEqualityPositive() returns boolean {
    byte a = 0;
    int b = 0;

    boolean 'equals = a === b && !(a !== b);

    a = 5;
    b = 5;

    return 'equals && a === b && !(a !== b);
}

function testIntByteEqualityNegative() returns boolean {
    byte a = 5;
    int b = 50;
    return a === b && !(a !== b);
}

function testXmlRefEqualityPositive() returns boolean {
    xml x1 = xml `<foo> </foo>`;
    xml x2 = x1;

    xml x3 = xml `<book><name>The Lost World<!-- I'm a comment --></name></book>`;
    xml x4 = x3;
    xml x5 = x3;

    return x1 === x2 && x4 === x5 && !(x1 !== x2) && !(x4 !== x5);
}

function testXmlRefEqualityNegative() returns boolean {
    xml x1 = xml `<book>The Lot World</book>`;
    xml x2 = xml `<book>The Lost World</book>`;

    xml x3 = xml `<book><name>The Lost World<!-- I'm a comment --></name></book>`;
    xml x4 = x3;
    x3 = xml `<book><name>The World</name></book>`;
    return x1 === x2 || x3 === x4 || !(x1 !== x2) || !(x4 !== x3);
}

function testObjectRefEqualityPositive() returns boolean {
    Def abcOne = new("abc", 100.0, 23);
    Abc abcTwo = abcOne;
    Abc abcThree = abcOne;
    boolean refEquals = abcTwo === abcOne && abcOne === abcThree && abcTwo === abcThree &&
        !(abcTwo !== abcOne) && !(abcOne !== abcThree) && !(abcTwo !== abcThree);

    abcThree = abcTwo;
    return refEquals && isRefEqual(abcThree, abcTwo) && abcTwo === abcThree && !(abcTwo !== abcThree);
}

function testObjectRefEqualityNegative() returns boolean {
    Abc abcOne = new("abc", 100.0, 23);
    Abc abcTwo = new("abc", 100.0, 23);
    boolean refEquals = abcOne === abcTwo || abcTwo === abcOne || !(abcTwo !== abcOne);

    Abc abcThree = abcTwo;
    return refEquals || isRefEqual(abcThree, abcOne);
}

function testValueTypeAndRefTypeEqualityPositive() returns boolean {
    int i = 5;
    json j = 5;

    boolean refEquals = i === j && !(i !== j);

    int|boolean|OpenEmployee a = true;
    boolean b = true;
    refEquals = refEquals && a === b && !(a !== b);

    any c = "hello world";
    string s = "hello world";
    return refEquals && c === s && !(c !== s);
}

function testValueTypeAndRefTypeEqualityNegative() returns boolean {
    int i = 5;
    json j = 15;

    boolean refEquals = i === j || !(i !== j);

    int|string|OpenEmployee a = "hello world";
    string s = "hello";
    refEquals = refEquals || a === s || !(s !== a);

    any b = true; // TODO: change to anydata
    return refEquals || isRefEqual(b, j) || isRefEqual(b, i) || isRefEqual(a, b);
}

function testValueTypesAsRefTypesEqualityPositive() returns boolean {
    any i = 5;
    int|float|byte j = 5;
    boolean refEquals = i === j && !(j !== i);

    i = 342.1;
    j = 342.1;
    refEquals = refEquals && i === j && !(j !== i);

    byte b = 45;
    i = b;
    j = b;
    refEquals = refEquals && i === j && !(j !== i);

    int|boolean|OpenEmployee a = true;
    anydata bool = true;
    refEquals = refEquals && a === bool && !(bool !== a);

    json c = "hello world";
    string s = "hello world";
    return refEquals && isRefEqual(c, s);
}

function testValueTypesAsRefTypesEqualityNegative() returns boolean {
    int|float i = 5;
    json j = 15;

    boolean refEquals = i === j || !(i !== j);

    int|string|OpenEmployee a = "hello world";
    any s = "hello";
    refEquals = refEquals || a === s || !(s !== a);

    anydata b = true;
    return refEquals || isRefEqual(b, j) || isRefEqual(b, i) || isRefEqual(a, b);
}

function isRefEqual(any a, any b) returns boolean {
    return a === b && !(b !== a);
}

function testXMLSequenceRefEquality() returns boolean {
    xml x = xml `<a>a</a>`;
    xml x1 = xml `<b>b</b>`;
    xml x2 = x + x1;
    xml x3 = x + x1;

    return x2 === x3;
}

function testXMLSequenceRefEqualityDifferentLength() returns boolean {
    xml x = xml `<a>a</a>`;
    xml x1 = xml `<b>b</b>`;
    xml x2 = x + x1;
    xml x3 = x + x1 + xml `<c>c</c>`;

    return x2 === x3;
}

function testXMLSequenceRefEqualityFalse() returns boolean {
    xml x = xml `<a>a</a>`;
    xml x1 = xml `<b>b</b>`;
    xml x11 = xml `<b>b</b>`;
    xml x2 = x + x1;
    xml x3 = x + x11;

    return x2 === x3;
}

function testXMLSequenceRefEqualityIncludingString() returns boolean {
    xml x = xml `<a>a</a>`;
    xml x1 = xml `<b>b</b>`;
    xml x2 = x + x1 + "abcd";
    xml x3 = x + x1 + "abcd";

    return x2 === x3;
}

function testXMLSequenceRefEqualityIncludingDifferentString() returns boolean {
    xml x = xml `<a>a</a>`;
    xml x1 = xml `<b>b</b>`;
    xml x2 = x + x1 + "abcd";
    xml x3 = x + x1 + "abcde";

    return x2 === x3;
}

function testEmptyXMLSequencesRefEquality() returns boolean {
    xml x = xml `<elem></elem>`;
    xml y = xml `<elem></elem>`;
    xml z = x/*;
    xml q = y/*;
    return z === q;
}
