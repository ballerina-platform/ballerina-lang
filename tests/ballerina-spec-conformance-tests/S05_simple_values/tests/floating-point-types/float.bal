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

// floating-point-type-descriptor := float | decimal
// floating-point-literal := DecimalFloatingPointNumber | HexFloatingPointLiteral
// DecimalFloatingPointNumber := DecimalNumber Exponent | DottedDecimalNumber [Exponent]
// DottedDecimalNumber := DecimalNumber . Digit* | . Digit+
// Exponent := ExponentIndicator [Sign] Digit+
// ExponentIndicator := e | E
// HexFloatingPointLiteral := HexIndicator HexFloatingPointNumber
// HexFloatingPointNumber := HexNumber HexExponent | DottedHexNumber [HexExponent]
// DottedHexNumber := HexDigit+ . HexDigit* | . HexDigit+
// HexExponent := HexExponentIndicator [Sign] Digit+
// HexExponentIndicator := p | P
// Sign := + | -
@test:Config {
    dataProvider: "floatValueDataProvider"
}
function testFloat(float f1, float f2, float f3, float f4) {
    test:assertEquals(f1, f2, msg = "expected float values to be equal");
    test:assertEquals(f1, f3, msg = "expected float values to be equal");
    test:assertEquals(f1, f4, msg = "expected float values to be equal");
}

function floatValueDataProvider() returns float[][] {
    return [
        // DecimalFloatingPointNumber
        [100e+12, 100E+12, 100e12, 100E12],
        [100e-2, 100E-2, 1000e-3, 10E-1],
        [0e2, 0E2, 0e-2, 0e-2],
        [0.0, .0, 0.0, 0.00],
        [22.2, 22.2e0, 22.2E0, 2.22E1],
        [.0e+12, .00E+12, 0.0e12, 0.00E12],
        [1.0e-12, 1.00E-12, 1.0e-12, 1.00E-12],

        // HexFloatingPointLiteral
        [0x1p+12, 0x1P+12, 0x1p12, 0x1P12],
        [0X2p+12, 0X2P+12, 0X2p12, 0X2P12],
        [0X500p-1, 0X500P-1, 0X500p-1, 0X500P-1],
        [0x500p-1, 0x500P-1, 0x500p-1, 0x500P-1],
        [0xaap+12, 0xAAP+12, 0xaAp12, 0xAaP12],
        [0x0.a, 0x.a, 0x0.A, 0x0.A0],
        [0X22.F, 0X22.F, 0X22.f, 0X22.f0],
        [0x0.ap0, 0x.ap0, 0x0.Ap0, 0x0.A0p0],
        [0X22.FP0, 0X22.FP0, 0X22.fP0, 0X22.f0P0]
    ];
}

// Positive and negative zero of a floating point basic type are distinct values,
// following IEEE 754, but are defined to have the same shape, so that they will usually be
// treated as being equal.
@test:Config {}
function testFloatZeroValues() {
    float f1 = +0.0;
    float f2 = -0.0;
    test:assertTrue(f1 == f2, msg = "expected +0.0 and -0.0 to be of same value");

    match f1 {
        0.0 => test:assertTrue(true);
        _ => test:assertFail(msg = "expected +0.0 to be same shape as 0.0");
    }
    match f2 {
        0.0 => test:assertTrue(true);
        _ => test:assertFail(msg = "expected -0.0 to be same shape as 0.0");
    }
}
