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

import ballerina/lang.'int as ints;
import ballerina/lang.test;

function testMax(int n, int... ns) returns int {
    return ints:max(n, ...ns);
}

function testMin(int n, int... ns) returns int {
    return ints:min(n, ...ns);
}

function testFromString() returns [int|error, int|error] {
    int|error v1 = ints:fromString("123");
    int|error v2 = ints:fromString("12invalid34");
    return [v1, v2];
}

function testSum() returns int {
    return ints:sum(10, 25, 35, 40);
}

function testAbs() returns [int, int] {
    int x = -123;
    int y = 234;
    return [x.abs(), y.abs()];
}

function testToHexString() returns [string, string] {
    int x = 123456789;
    int y = -12345;
    return [x.toHexString(), y.toHexString()];
}

function testFromHexString() returns [int|error, int|error] {
    int|error v1 = ints:fromHexString("aBCd45");
    int|error v2 = ints:fromHexString("12invalid34");
    return [v1, v2];
}

function testChainedIntFunctions() returns int {
    var e = 0;
    int a = e.max(1).min(2);
    return a;
}

function testLangLibCallOnIntSubTypes() {
    int a = 12;
    byte b = 12;
    int:Signed8 c = 12;
    int:Signed16 d = 12;
    int:Signed32 e = 12;
    int:Unsigned8 f = 12;
    int:Unsigned16 g = 12;
    int:Unsigned32 h = 12;
    int:Signed8|int:Unsigned32 i = 510;

    string s1 = a.toHexString();
    string s2 = b.toHexString();
    string s3 = c.toHexString();
    string s4 = d.toHexString();
    string s5 = e.toHexString();
    string s6 = f.toHexString();
    string s7 = g.toHexString();
    string s8 = h.toHexString();
    // https://github.com/ballerina-platform/ballerina-lang/issues/22771
    // string s9 = i.toHexString();

    test:assertValueEqual("c", s1);
    test:assertValueEqual(s1, s2);
    test:assertValueEqual(s1, s3);
    test:assertValueEqual(s1, s4);
    test:assertValueEqual(s1, s5);
    test:assertValueEqual(s1, s6);
    test:assertValueEqual(s1, s7);
    test:assertValueEqual(s1, s8);
    // test:assertValueEqual((510).toHexString(), s9);
}

type Ints 12|21;

function testLangLibCallOnFiniteType() {
    Ints x = 12;
    string s = x.toHexString();
    test:assertValueEqual("c", s);
}
