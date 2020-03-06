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


const ints:Signed32 constS32a = 2147483647;
const ints:Signed32 constS32b = 2147483648; // Error

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

const ints:Unsigned32 cde = -10;  // Error
const ints:Unsigned16 cee = -10; // Error
const ints:Unsigned8 cfe = -10; // Error
const byte cge = -10; // Error

const ints:Signed32 ca1 = 10;
const ints:Signed16 cb1 = 10;
const ints:Signed8 cc1 = 10;
const ints:Unsigned32 cd1 = 10;
const ints:Unsigned16 ce1 = 10;
const ints:Unsigned8 cf1 = 10;
const byte cg1 = 10;

// TODO : Fix me. Following are valid, But Not supported yet.

//function testConstReference() {
//    ints:Unsigned8 a = ca1;
//    ints:Unsigned8 b = cb1;
//    ints:Unsigned8 c = cc1;
//    ints:Unsigned8 d = cd1;
//    ints:Unsigned8 e = ce1;
//    ints:Unsigned8 f = cf1;
//    ints:Unsigned8 g = cg1;
//}

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



// TODO : Add more test cases.
