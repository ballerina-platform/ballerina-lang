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

const int CAI = 10 + 5;
const float CAF = 10.0 + 5.0;
const decimal CAD = 11.5 + 4;
const string CAS = "hello" + "world";

function getConstAdditions() returns [int, float, decimal, string] {
    return [CAI, CAF, CAD, CAS];
}

const int CSI = 10 - 5;
const float CSF = 10.5 - 5.0;
const decimal CSD = 10.5 - 5;

function getConstSubtracts() returns [int, float, decimal] {
    return [CSI, CSF, CSD];
}

const int CMI = 10 * 5;
const float CMF = 10.5 * 5.0;
const decimal CMD = 10.5 * 5;

function getConstMultiplications() returns [int, float, decimal] {
    return [CMI, CMF, CMD];
}

const int CDI = 10 / 5;
const float CDF = 10.5 / 5.0;
const float NAN = 0.0 / 0.0;
const float IFN = 1.0 / 0.0;
const decimal CDD = 10.5 / 5;

function getConstDivisions() returns [int, float, float, float, decimal] {
    return [CDI, CDF, NAN, IFN, CDD];
}

const int CGI1 = (10 + (5 + (10 - 5)));
const int CGI2 = (10 / (-5 - (10 - 5)));
const int CGI3 = (10 + (5 * (10 - 5)));

function getConstGrouping() returns [int, int, int] {
    return [CGI1, CGI2, CGI3];
}

const float CF1 = 1.0 + 2.0;
const float CF2 = 2.0 + 3.0;

const map<float> CFMap = { v1 : CF1, v2 : CF2};

function checkMapAccessReference()  returns map<float> {
    return CFMap;
}

const int SHIFTED = (1 << CAI) + 1;

const int RSHIFTED = (64 >>  1);
const int RSHIFTED_C = CAI >> 2;

const int URSHIFTED = (-32) >>> 2;
const int URSHIFTED_C = CAI >>> 2;

const int AND = 31 & 1;
const int OR = 30 | 1;
const int XOR = 3 ^ 1;

const int WRAP_AROUND_0 = 1 << 62;
const int WRAP_AROUND_2 = 1 << 64;
const int WRAP_AROUND_3 = 1 << 65;

const int ZERO_EXT_0 = 3 >> 0;
const int ZERO_EXT_1 = 3 >> 1;
const int ZERO_EXT_2 = 3 >> 2;
const int ZERO_EXT_3 = 3 >> 3;
const int ZERO_EXT_4 = 3 >> 4;
const int ZERO_EXT_5 = 3 >> 5;

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

const int CUI1 = -(10);
const int CUI2 = (-(+(5+10)));
const int CUI3 = ~2;
const int CUI4 = ~(-36);
const float CUF1 = -(10.0 * 2.0);
const float CUF2 = +(10.0 + 2.0);
const decimal CUD = -(11.5 + 4);
const boolean CUB = !(true);
const map<int> CUE = {
    a: -1
};
const int CUI5 = -int:MAX_VALUE;

function testConstUnaryExpressions() {
    assertEqual(CUI1, -10);
    assertEqual(CUI2, -15);
    assertEqual(CUI3, -3);
    assertEqual(CUI4, 35);
    assertEqual(CUF1, -20.0);
    assertEqual(CUF2, 12.0);
    assertEqual(CUD, -15.5d);
    assertEqual(CUB, false);
    assertEqual(CUE["a"], -1);
    assertEqual(CUI5, -9223372036854775807);
}

const float X1 = 5.5;
const float ANS1 = X1 % 0;
const float ANS2 = X1 % 1.1;  // 1.0999999999999996
const float ANS3 = 5 % float:NaN;
const float ANS4 = float:NaN % 5;
const float ANS5 = float:Infinity % 5;
const float ANS6 = 5 % float:Infinity;

const decimal ANS7 = 5 % 2;
const decimal ANS8 = 100 % 9.999999999999999999999999999999999e6144;

function testConstRemainderOperation() {
    assertEqual(ANS1.toString(), "NaN");
    assertEqual(ANS2, 1.0999999999999996);
    assertEqual(ANS3.toString(), "NaN");
    assertEqual(ANS4.toString(), "NaN");
    assertEqual(ANS5.toString(), "NaN");
    assertEqual(ANS6, 5.0);

    assertEqual(ANS7.toString(), "1");
    assertEqual(ANS8.toString(), "100");
}

const decimal ANS11 = 1.000000000000000000000000000000000e-6143 * 1e-1;
const decimal ANS12 = 1.000000000000000000000000000000000e-6143 * 1e-100;
const decimal ANS13 = -1.000000000000000000000000000000000e-6143 * 1e-1;
const decimal ANS14 = -1.000000000000000000000000000000000e-6143 * 1e-150;

function testConstDecimalSubnormals() {
    assertEqual(ANS11.toString(), "0");
    assertEqual(ANS12.toString(), "0");
    assertEqual(ANS13.toString(), "0");
    assertEqual(ANS14.toString(), "0");
}

function assertEqual(int|float|decimal|boolean|string actual, int|float|decimal|boolean|string expected) {
    if (actual != expected) {
        panic error(string `Assertion error: expected ${expected} found ${actual}`);
    }
}
