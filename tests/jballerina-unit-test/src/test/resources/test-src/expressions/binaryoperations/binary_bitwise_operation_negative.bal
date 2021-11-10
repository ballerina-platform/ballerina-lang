// Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public const int A1 = 12;
public const float A2 = 5.0;

type A A1|A2;

type B int|string;

function testBinaryBitwiseOperationsNegativeScenarios() {
    float a1 = 1.0;
    int x1 = a1 & 64;

    A a2 = 5.0;
    int x2 = 1 & a2;

    int|float a3 = 12;
    int x3 = 1 & a3;

    B a4 = 10;
    int x4 = 1 & a4;

    int x5 = 1 & A2;

    int x6 = a1 | 64;

    int x7 = 1 | a2;

    int x8 = 1 | a3;

    int x9 = 1 | a4;

    int x10 = 1 | A2;

    int x11 = a1 ^ 64;

    int x12 = 1 ^ a2;

    int x13 = 1 ^ a3;

    int x14 = 1 ^ a4;

    int x15 = 1 ^ A2;

    byte a = 12;
    int:Unsigned16 e = 12;
    int:Unsigned32 f = 5;

    byte x16 = a | f;
    byte x17 = a | e;
    int:Unsigned8 x18 = a | f;
    int:Unsigned8 x19 = a | e;
}
