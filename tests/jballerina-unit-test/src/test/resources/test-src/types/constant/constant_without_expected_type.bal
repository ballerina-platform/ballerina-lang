// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

const CAE = 2.0 + 3f;
const CAI = 10 + 5;
const CAF = 10.0 + 5.0;
const CAS = "hello" + "world";

function testConstAdditions() {
    assertEqual(CAE, 5.0);
    assertEqual(CAI, 15);
    assertEqual(CAF, 15.0);
    assertEqual(CAS, "helloworld");
}

const CSI = 10 - 5;
const CSF = 10.5 - 5.0;

function testConstSubtracts() {
    assertEqual(CSI, 5);
    assertEqual(CSF, 5.5);
}

const CMI = 10 * 5;
const CMF = 10.5 * 5.0;
const CMD = 10.5 * 5;

function testConstMultiplications() {
    assertEqual(CMI, 50);
    assertEqual(CMF, 52.5);
    assertEqual(CMD, 52.5);
}

const CDI = 10 / 5;
const CDF = 10.5 / 5.0;
const NAN = 0.0 / 0.0;
const IFN = 1.0 / 0.0;
const CDD = 10.5 / 5;

function testConstDivisions() {
    assertEqual(CDI, 2);
    assertEqual(CDF, 2.1);
    assertEqual(NAN.toString(), "NaN");
    assertEqual(IFN, float:Infinity);
    assertEqual(CDD, 2.1);
}

const CGI1 = (10 + (5 + (10 - 5)));
const CGI2 = (10 / (-5 - (10 - 5)));
const CGI3 = (10 + (5 * (10 - 5)));

function testConstGrouping() {
    assertEqual(CGI1, 20);
    assertEqual(CGI2, -1);
    assertEqual(CGI3, 35);
}

const CF1 = 1.0 + 2.0;
const CF2 = 2.0 + 3.0;

const CFMap = { v1 : CF1, v2 : CF2};

function testMapAccessReference() {
    assertEqual(CFMap.v1, 3.0);
    assertEqual(CFMap.v2, 5.0);
}

const SHIFTED = (1 << CAI) + 1;

const RSHIFTED = (64 >>  1);
const RSHIFTED_C = CAI >> 2;

const URSHIFTED = (-32) >>> 2;
const URSHIFTED_C = CAI >>> 2;

const AND = 31 & 1;
const OR = 30 | 1;
const XOR = 3 ^ 1;

const WRAP_AROUND_0 = 1 << 62;
const WRAP_AROUND_2 = 1 << 64;
const WRAP_AROUND_3 = 1 << 65;

const ZERO_EXT_0 = 3 >> 0;
const ZERO_EXT_1 = 3 >> 1;
const ZERO_EXT_2 = 3 >> 2;
const ZERO_EXT_3 = 3 >> 3;
const ZERO_EXT_4 = 3 >> 4;
const ZERO_EXT_5 = 3 >> 5;

const TAG_MASK = 0xff;
const TAG_LEFT_SHIFT = 0x38;
const TAG_RIGHT_SHIFT = 0x2;

function testBitwiseConstExpressions() {
    assertEqual(SHIFTED, 0x8001);
    assertEqual(RSHIFTED, 0x20);
    assertEqual(RSHIFTED_C, 0x3);
    assertEqual(URSHIFTED, 0x3ffffffffffffff8);
    assertEqual(URSHIFTED_C, 0x3);
    assertEqual(AND, 0x1);
    assertEqual(OR, 0x1F);
    assertEqual(XOR, 0x2);

    assertEqual(WRAP_AROUND_0, 0x4000000000000000);
    assertEqual(WRAP_AROUND_2, 0x1);
    assertEqual(WRAP_AROUND_3, 0x2);

    assertEqual(ZERO_EXT_0, 0x3);
    assertEqual(ZERO_EXT_1, 0x1);
    assertEqual(ZERO_EXT_2, 0x0);
    assertEqual(ZERO_EXT_3, 0x0);
    assertEqual(ZERO_EXT_4, 0x0);
    assertEqual(ZERO_EXT_5, 0x0);

    assertEqual(TAG_MASK << TAG_LEFT_SHIFT, -0x0100000000000000);
    assertEqual(TAG_MASK >> TAG_RIGHT_SHIFT, 0x3f);
    assertEqual(~(TAG_MASK << TAG_LEFT_SHIFT), 0xffffffffffffff);
    assertEqual(~(TAG_MASK >> TAG_RIGHT_SHIFT), -64);
}

const CUI1 = -(10);
const CUI2 = (-(+(5+10)));
const CUI3 = ~2;
const CUI4 = ~(-36);
const CUF1 = -(10.0 * 2.0);
const CUF2 = +(10.0 + 2.0);
const CUB = !(true);
const CUE = {
    a: -1
};
const CUI5 = -int:MAX_VALUE;

function testConstUnaryExpressions() {
    assertEqual(CUI1, -10);
    assertEqual(CUI2, -15);
    assertEqual(CUI3, -3);
    assertEqual(CUI4, 35);
    assertEqual(CUF1, -20.0);
    assertEqual(CUF2, 12.0);
    assertEqual(CUB, false);
    assertEqual(CUE["a"], -1);
    assertEqual(CUI5, -9223372036854775807);
}

const X1 = 5.5;
const ANS1 = X1 % 0;
const ANS2 = X1 % 1.1;  // 1.0999999999999996
const ANS3 = float:NaN % 5; // error
const ANS4 = float:Infinity % 5;
const ANS5 = 5 % 2;

function testConstRemainderOperation() {
    assertEqual(ANS1.toString(), "NaN");
    assertEqual(ANS2, 1.0999999999999996);
    assertEqual(ANS3.toString(), "NaN");
    assertEqual(ANS4.toString(), "NaN");
    assertEqual(ANS5.toString(), "1");
}

function assertEqual(int|float|decimal|boolean|string actual, int|float|decimal|boolean|string expected) {
    if (actual != expected) {
        panic error(string `Assertion error: expected ${expected} found ${actual}`);
    }
}
