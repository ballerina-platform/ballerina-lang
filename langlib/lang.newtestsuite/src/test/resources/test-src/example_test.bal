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

import ballerina/lang.test as test;
import ballerina/lang.'int as ints;


function testValueAssignment() {
    ints:Signed32 a1 = 2147483647;
    ints:Signed32 a2 = -2147483648;
    ints:Signed16 b1 = 32767;
    ints:Signed16 b2 = -32768;
    ints:Signed8 c1 = 127;
    ints:Signed8 c2 = -128;
    ints:Unsigned32 d1 = 4294967295;
    ints:Unsigned32 d2 = 0;
    ints:Unsigned16 e1 = 65535;
    ints:Unsigned16 e2 = 0;
    ints:Unsigned8 f1 = 255;
    ints:Unsigned8 f2 = 0;

    byte g1 = 255;
    byte g2 = 0;

    test:assertEquals(2147483647, a1);
    test:assertEquals(-2147483648, a2);
    test:assertEquals(32767, b1);
    test:assertEquals(-32768, b2);
    test:assertEquals(127, c1);
    test:assertEquals(-128, c2);
    test:assertEquals(4294967295, d1);
    test:assertEquals(0, d2);
    test:assertEquals(65535, e1);
    test:assertEquals(0, e2);
    test:assertEquals(255, f1);
    test:assertEquals(0, f2);
    test:assertEquals(255, g1);
    test:assertEquals(0, g2);
}

function testAbs() returns decimal {
    decimal d1 = 100.1;
    return d1;
}

function sum(int a) returns int {
    return a + 10;
}

function testFail(){
    test:fail();
}

function concat(string s1, string... sa) returns string {
    string r  = s1;
    foreach string s in sa {
        r += s;
    }
    return r;
}
