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

import ballerina/lang.'int as ints;

function testValueAssignment() {
    ints:Signed32 a1 = 2147483647;
    ints:Signed32 a2 = 2147483648; // Error
    ints:Signed32 a3 = -2147483648;
    ints:Signed32 a4 = -2147483649; // Error
    ints:Signed16 b1 = 32767;
    ints:Signed16 b2 = 32768; // Error
    ints:Signed16 b3 = -32768;
    ints:Signed16 b4 = -32769; // Error
    ints:Signed8 c1 = 127;
    ints:Signed8 c2 = 128; // Error
    ints:Signed8 c3 = -128;
    ints:Signed8 c4 = -129; // Error
    ints:Unsigned32 d1 = 4294967295;
    ints:Unsigned32 d2 = 4294967296; // Error
    ints:Unsigned32 d3 = 0;
    ints:Unsigned32 d4 = -1; // Error
    ints:Unsigned16 e1 = 65535;
    ints:Unsigned16 e2 = 65536; // Error
    ints:Unsigned16 e3 = 0;
    ints:Unsigned16 e4 = -1; // Error
    ints:Unsigned8 f1 = 255;
    ints:Unsigned8 f2 = 256; // Error
    ints:Unsigned8 f3 = 0;
    ints:Unsigned8 f4 = -1; // Error
    byte g1 = 255;
    byte g2 = 256;
    byte g3 = 0;
    byte g4 = -1;
}

function testIntAssignment() {
    int max = 10;
    ints:Signed32 a = max; // Error
    ints:Signed16 b = max; // Error
    ints:Signed8 c = max; // Error
    ints:Unsigned32 d = max; // Error
    ints:Unsigned16 e = max; // Error
    ints:Unsigned8 f = max; // Error
    byte g = max; // Error
}

type NewInt ints:Signed32;

function testTypeAlias() {
    int max = 10;
    NewInt a = max; // Error
    NewInt a1 = 2147483647;
    NewInt a2 = 2147483648; // Error
    NewInt a3 = -2147483648;
    NewInt a4 = -2147483649; // Error

    ints:Signed32 b = a1;
    ints:Unsigned8 c = a1; // Error
}


//const ints:Signed32 constS32a = 2147483647;
//const ints:Signed32 constS32b = 2147483648; // Error // TODO : Fix this, Issue : #21542

function testSigned32Assignment() {
    ints:Signed32 value = 2147483647;
    ints:Signed16 b = value; // Error
    ints:Signed8 c = value; // Error
    ints:Unsigned32 d = value; // Error
    ints:Unsigned16 e = value; // Error
    ints:Unsigned8 f = value; // Error
    byte g = value;  // Error
}

function testSigned16Assignment() {
    ints:Signed16 value = 32767;
    ints:Signed32 b = value;
    ints:Signed8 c = value; // Error
    ints:Unsigned32 d = value; // Error
    ints:Unsigned16 e = value; // Error
    ints:Unsigned8 f = value; // Error
    byte g = value; // Error
}

function testSigned8Assignment() {
    ints:Signed8 value = 127;
    ints:Signed32 b = value;
    ints:Signed16 c = value;
    ints:Unsigned32 d = value; // Error
    ints:Unsigned16 e = value; // Error
    ints:Unsigned8 f = value; // Error
    byte g = value; // Error
}

function testUnsigned32Assignment() {
    ints:Unsigned32 value = 2147483647;
    ints:Signed32 a = value; // Error
    ints:Signed16 b = value; // Error
    ints:Signed8 c = value; // Error
    ints:Unsigned16 d = value; // Error
    ints:Unsigned8 e = value; // Error
    byte g = value; // Error
}

function testunsigned16Assignment() {
    ints:Unsigned16 value = 32767;
    ints:Signed32 a = value;
    ints:Signed16 b = value; // Error
    ints:Signed8 c = value; // Error
    ints:Unsigned32 d = value;
    ints:Unsigned8 e = value; // Error
    byte g = value; // Error
}

function testunsigned8Assignment() {
    ints:Unsigned8 value = 255;
    ints:Signed32 a = value;
    ints:Signed16 b = value;
    ints:Signed8 c = value; // Error
    ints:Unsigned32 d = value;
    ints:Unsigned16 e = value;
    byte g = value;
}

// TODO : Fix this, Issue : #21542
//const ints:Unsigned32 cde = -10;  // Error
//const ints:Unsigned16 cee = -10; // Error
//const ints:Unsigned8 cfe = -10; // Error
//const byte cge = -10; // Error

// TODO : Fix me. Following are valid, But Not supported yet.
//type TenOrEleven 10|11;
//type TenOrMinusTen 10|-10;
//
//function testUnion1(){
//    TenOrEleven val = 10;
//    ints:Signed32 a = val;
//    ints:Signed16 b = val;
//    ints:Signed8 c = val;
//    ints:Unsigned32 d = val;
//    ints:Unsigned16 e = val;
//    ints:Unsigned8 f = val;
//    byte g = val;
//}
//
//function testUnion2(){
//    TenOrMinusTen val = -10;
//    ints:Signed32 a = val;
//    ints:Signed16 b = val;
//    ints:Signed8 c = val;
//    ints:Unsigned32 d = val; // Error
//    ints:Unsigned16 e = val; // Error
//    ints:Unsigned8 f = val; // Error
//    byte g = val; // Error
//}

function testMathsOperations(){
    ints:Signed32 a = 10;
    ints:Signed16 b = 10;
    int x = a + b;
}

function testInvalidLeftShiftType() {
    ints:Signed8 a = 1;
    ints:Signed16 b = -1567;
    ints:Signed32 c = 139058;
    int d = 6429485;

    byte e = 23;
    ints:Unsigned8 f = 12;
    ints:Unsigned16 g = 2345;
    ints:Unsigned32 h = 5739412;

    byte s = a << 1;
    float t = b << e;
    ints:Signed32 u = c << b;
    byte v = d << a;
    byte w = e << 3;
    ints:Unsigned8 x = f << e;
    byte y = g << 2;
    decimal z = h << g;
}

function testInvalidRightShiftType() {
    ints:Signed8 a = 1;
    ints:Signed16 b = -1567;
    ints:Signed32 c = 139058;
    int d = 6429485;

    byte e = 23;
    ints:Unsigned8 f = 12;
    ints:Unsigned16 g = 2345;
    ints:Unsigned32 h = 5739412;

    byte s = a >> 1;
    float t = b >> e;
    ints:Signed32 u = c >> b;
    byte v = d >> a;
    ints:Signed8 w = e >> 3;
    ints:Signed8 x = f >> e;
    byte y = g >> 2;
    decimal z = h >> g;
}

function testInvalidUnsignedRightShiftType() {
    ints:Signed8 a = 1;
    ints:Signed16 b = -1567;
    ints:Signed32 c = 139058;
    int d = 6429485;

    byte e = 23;
    ints:Unsigned8 f = 12;
    ints:Unsigned16 g = 2345;
    ints:Unsigned32 h = 5739412;

    byte s = a >>> 1;
    float t = b >>> e;
    ints:Signed32 u = c >>> b;
    byte v = d >>> a;
    ints:Signed8 w = e >>> 3;
    ints:Signed8 x = f >>> e;
    byte y = g >>> 2;
    decimal z = h >>> g;
}

function testInvalidBitwiseAndType() {
    ints:Signed8 a = 1;
    ints:Signed16 b = -1567;
    ints:Signed32 c = 139058;
    int d = 6429485;

    byte e = 23;
    ints:Unsigned8 f = 12;
    ints:Unsigned16 g = 2345;
    ints:Unsigned32 h = 5739412;

    byte r = a & 1;
    float s = b & c;
    ints:Signed32 t = d & 2;
    decimal u = e & e;
    ints:Signed8 v = f & f;
    ints:Signed16 w = g & h;
    ints:Signed8 x = h & f;
    ints:Signed8 y = a & h;
    ints:Signed8 z = f & b;
}

function testInvalidBitwiseOrType() {
    ints:Signed8 a = 1;
    ints:Signed16 b = -1567;
    ints:Signed32 c = 139058;
    int d = 6429485;

    byte e = 23;
    ints:Unsigned8 f = 12;
    ints:Unsigned16 g = 2345;
    ints:Unsigned32 h = 5739412;

    byte r = a | 1;
    float s = b | c;
    ints:Signed32 t = d | 2;
    decimal u = e | e;
    ints:Signed8 v = f | f;
    ints:Signed16 w = g | h;
    ints:Signed8 x = h | f;
    ints:Signed8 y = a | h;
    ints:Signed8 z = f | d;
}

function testInvalidBitwiseXorType() {
    ints:Signed8 a = 1;
    ints:Signed16 b = -1567;
    ints:Signed32 c = 139058;
    int d = 6429485;

    byte e = 23;
    ints:Unsigned8 f = 12;
    ints:Unsigned16 g = 2345;
    ints:Unsigned32 h = 5739412;

    byte r = a ^ 1;
    float s = b ^ c;
    ints:Signed32 t = d ^ 2;
    decimal u = e ^ e;
    ints:Signed8 v = g ^ g;
    ints:Signed16 w = g ^ h;
    ints:Signed8 x = h ^ f;
    ints:Signed8 y = a ^ h;
    ints:Signed8 z = f ^ d;
}

type X -1|2;
type Y -1|1|128;
type Z -1|1|"foo";

function testInvalidFiniteTypeAsIntSubType() {
    X a = -1;
    int:Unsigned32 b = a;

    X[] c = [-1, -1, 2];
    int:Unsigned8[] d = c;

    Y[] e = [1, 1, -1];
    int:Signed8[] f = e;

    Z g = -1;
    int:Signed8 h = g;
    string:Char|int:Signed8 i = g;

    Z[] j = [];
    (float|string:Char|int:Signed8)[] k = j;
    (float|string|int:Unsigned8)[] l = j;
}

type InvalidIntType int:Signed32|int:Signed16|string;
type InvalidIntFiniteType 1|2|3|"R";

function testInvalidIntSubtypesInLangLibFunctions() {
    InvalidIntType intVal1 = "FOO";
    int:Signed32|int:Signed16|string intVal2 = "FOO";
    InvalidIntFiniteType intVal3 = 1;

    _ = int:toHexString(intVal1);
    _ = int:toHexString(intVal2);

    _ = intVal1.toHexString();
    _ = intVal2.toHexString();

    _ = int:toHexString(intVal3);
    _ = intVal3.toHexString();
}

// TODO : Add more test cases.
