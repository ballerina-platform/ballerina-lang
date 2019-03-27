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
    dataProvider: "decimalValueDataProvider"
}
function testDecimal(decimal d1, decimal d2, decimal d3, decimal d4) {
    test:assertEquals(d1, d2, msg = "expected decimal values to be equal");
    test:assertEquals(d1, d3, msg = "expected decimal values to be equal");
    test:assertEquals(d1, d4, msg = "expected decimal values to be equal");
}

function decimalValueDataProvider() returns decimal[][] {
    return [
        // DecimalFloatingPointNumber
        [100e+12d, 100E+12d, 100e12d, 100E12],
        [100e-2d, 100E-2d, 1000e-3d, 10E-1],
        [0e2d, 0E2d, 0e-2d, 0e-2],
        [0.0d, .0d, 0.0d, 0.00],
        [22.2d, 22.2e0d, 22.2E0d, 2.22E1],
        [.0e+12d, .00E+12d, 0.0e12d, 0.00E12],
        [1.0e-12d, 1.00E-12d, 1.0e-12d, 1.00E-12],

        // HexFloatingPointLiteral
        [0x1p+12d, 0x1P+12d, 0x1p12d, 0x1P12],
        [0X2p+12d, 0X2P+12d, 0X2p12d, 0X2P12],
        [0X500p-1d, 0X500P-1d, 0X500p-1d, 0X500P-1],
        [0x500p-1d, 0x500P-1d, 0x500p-1d, 0x500P-1],
        [0xaap+12d, 0xAAP+12d, 0xaAp12d, 0xAaP12],
        [0x0.ad, 0x.ad, 0x0.Ad, 0x0.A0],
        [0X22.Fd, 0X22.Fd, 0X22.fd, 0X22.f0],
        [0x0.ap0d, 0x.ap0d, 0x0.Ap0d, 0x0.A0p0],
        [0X22.FP0d, 0X22.FP0d, 0X22.fP0d, 0X22.f0P0]
    ];
}

// Positive and negative zero of a floating point basic type are distinct values,
// following IEEE 754, but are defined to have the same shape, so that they will usually be
// treated as being equal.
@test:Config {}
function testDecimalZeroValues() {
    decimal d1 = +0.0d;
    decimal d2 = -0.0d;
    test:assertTrue(d1 == d2, msg = "expected +0.0 and -0.0 to be of same value");
}
