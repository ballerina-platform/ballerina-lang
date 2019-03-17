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

import ballerina/test;
import utils;

// int-type-descriptor := int
// int-literal := DecimalNumber | HexIntLiteral
// DecimalNumber := 0 | NonZeroDigit Digit*
// HexIntLiteral := HexIndicator HexNumber
// HexNumber := HexDigit+
// HexIndicator := 0x | 0X
// HexDigit := Digit | a .. f | A .. F
// Digit := 0 .. 9
// NonZeroDigit := 1 .. 9
@test:Config {
    dataProvider: "intDataProvider"
}
function testInt(int i1, int i2, int i3, int i4) {
    test:assertEquals(i1, i2, msg = "expected int values to be equal");
    test:assertEquals(i1, i3, msg = "expected int values to be equal");
    test:assertEquals(i1, i4, msg = "expected int values to be equal");
}

// The int type consists of integers between -9,223,372,036,854,775,808 and
// 9,223,372,036,854,775,807 (i.e. signed integers than can fit into 64 bits using a two’s
// complement representation)
@test:Config {
    dataProvider: "decimalDataProvider"
}
function testOutOfRangeValueAsInt(decimal d) {
    utils:assertPanic(function() returns int|error { return int.convert(d); },
        "{ballerina}ConversionError", "invalid reason on out of range int value");
}

function intDataProvider() returns int[][] {
    return [
        [0, 0x0, 0X00, 0x000],
        [2767, 0xACF, 0xacf, 0XAcf],
        [1105, 0x451, 0X451, 0X0451]
    ];
}

function decimalDataProvider() returns decimal[][] {
    return [
        [-9223372036854775810.0],
        [-9223372036854775809.0],
        [9223372036854775808.0],
        [9223372036854775810.5]
    ];
}
