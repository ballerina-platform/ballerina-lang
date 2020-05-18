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

    test:assertValueEqual(ints:abs(34), ints:abs(-34));
    byte g1 = 255;
    byte g2 = 0;

    test:assertValueEqual(2147483647, a1);
    test:assertValueEqual(-2147483648, a2);
    test:assertValueEqual(32767, b1);
    test:assertValueEqual(-32768, b2);
    test:assertValueEqual(127, c1);
    test:assertValueEqual(-128, c2);
    test:assertValueEqual(4294967295, d1);
    test:assertValueEqual(0, d2);
    test:assertValueEqual(65535, e1);
    test:assertValueEqual(0, e2);
    test:assertValueEqual(255, f1);
    test:assertValueEqual(0, f2);
    test:assertValueEqual(255, g1);
    test:assertValueEqual(0, g2);
}
