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
        [<decimal>100e+12, <decimal>100E+12, <decimal>100e12, <decimal>100E12],
        [<decimal>100e-2, <decimal>100E-2, <decimal>1000e-3, <decimal>10E-1],
        [<decimal>0e2, <decimal>0E2, <decimal>0e-2, <decimal>0e-2],
        [<decimal>0.0, <decimal>.0, <decimal>0.0, <decimal>0.00],
        [<decimal>22.2, <decimal>22.2e0, <decimal>22.2E0, <decimal>2.22E1],
        [<decimal>.0e+12, <decimal>.00E+12, <decimal>0.0e12, <decimal>0.00E12],
        [<decimal>1.0e-12, <decimal>1.00E-12, <decimal>1.0e-12, <decimal>1.00E-12],

        // HexFloatingPointLiteral
        [<decimal>0x1p+12, <decimal>0x1P+12, <decimal>0x1p12, <decimal>0x1P12],
        [<decimal>0X2p+12, <decimal>0X2P+12, <decimal>0X2p12, <decimal>0X2P12],
        [<decimal>0X500p-1, <decimal>0X500P-1, <decimal>0X500p-1, <decimal>0X500P-1],
        [<decimal>0x500p-1, <decimal>0x500P-1, <decimal>0x500p-1, <decimal>0x500P-1],
        [<decimal>0xaap+12, <decimal>0xAAP+12, <decimal>0xaAp12, <decimal>0xAaP12],
        [<decimal>0x0.a, <decimal>0x.a, <decimal>0x0.A, <decimal>0x0.A0],
        [<decimal>0X22.F, <decimal>0X22.F, <decimal>0X22.f, <decimal>0X22.f0],
        [<decimal>0x0.ap0, <decimal>0x.ap0, <decimal>0x0.Ap0, <decimal>0x0.A0p0],
        [<decimal>0X22.FP0, <decimal>0X22.FP0, <decimal>0X22.fP0, <decimal>0X22.f0P0]
    ];
}

// Positive and negative zero of a floating point basic type are distinct values,
// following IEEE 754, but are defined to have the same shape, so that they will usually be
// treated as being equal.
@test:Config {}
function testDecimalZeroValues() {
    decimal d1 = <decimal>+0.0;
    decimal d2 = <decimal>-0.0;
    test:assertTrue(d1 == d2, msg = "expected +0.0 and -0.0 to be of same value");
}
