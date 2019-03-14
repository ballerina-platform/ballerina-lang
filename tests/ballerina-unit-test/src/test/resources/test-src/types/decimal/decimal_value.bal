// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Simple decimal literal - positive.
function testDecimalValue() returns (decimal) {
    decimal d;
    d = 10.1;
    return d;
}

// Simple decimal literal - negative.
function testNegativeDecimalValue() returns (decimal) {
    decimal d;
    d = -10.1;
    return d;
}

// Decimal value assignment by a return value.
function testDecimalValueAssignmentByReturnValue() returns (decimal) {
    decimal d;
    d = testDecimalValue();
    return d;
}

// Decimal addition.
function testDecimalAddition() returns (decimal) {
    decimal a = 4.565;
    decimal b = 3.1;
    return a + b; // EXPECTED: 7.665
}

// Decimal substraction.
function testDecimalSubtraction() returns (decimal) {
    decimal a = 4.565;
    decimal b = 3.1;
    return a - b; // EXPECTED: 1.465
}

// Decimal multiplication.
function testDecimalMultiplication() returns (decimal) {
    decimal a = 4.565;
    decimal b = 3.1;
    return a * b; // EXPECTED: 14.1515
}

// Decimal division.
function testDecimalDivision() returns (decimal) {
    decimal a = 4.565;
    decimal b = 3.1;
    return a / b; // EXPECTED: 1.47258064516
}

// Decimal modulus.
function testDecimalModulus() returns (decimal) {
    decimal a = 4.565;
    decimal b = 3.1;
    return a % b; // EXPECTED: 1.465
}

// Decimal negation.
function testDecimalNegation() returns (decimal) {
    decimal a = 4.565;
    return -a; // EXPECTED: -4.565
}

// Decimal comparison operations.
function testDecimalComparisonOperations() returns (boolean, boolean, boolean, boolean, boolean, boolean) {
    decimal d1 = 45.678432;
    decimal d2 = 45.678432005;

    boolean b1 = (d1 == d2);
    boolean b2 = (d1 != d2);
    boolean b3 = (d1 > d2);
    boolean b4 = (d1 < d2);
    boolean b5 = (d1 >= d2);
    boolean b6 = (d1 <= d2);

    return (b1, b2, b3, b4, b5, b6);
}

// Decimal value passed as a parameter.
function testDecimalParameter(decimal a, decimal b) returns (decimal, decimal) {
    decimal c;
    decimal d;
    c = a;
    d = b;
    return (c, d);
}

// Test int literal assingment for a decimal variable.
function testIntLiteralAssignment() returns (decimal, decimal) {
    decimal d = 12;
    return (d, 15);
}

// Test assigning positive hexadecimal literal without power and floating point.
function testHexAssignment1() returns decimal {
    decimal d = 0x123F0;
    return d;
}

// Test assigning positive hexadecimal literal with floating point but without power.
function testHexAssignment2() returns decimal {
    decimal d = 0x1A2B.3F0;
    return d;
}

// Test assigning positive hexadecimal literal with positive power but without floating point.
function testHexAssignment3() returns (decimal, decimal) {
    decimal d1 = 0x1A2Fp5;
    decimal d2 = 0x1A2FP5;
    return (d1, d2);
}

// Test assigning positive hexadecimal literal with negative power but without floating point.
function testHexAssignment4() returns (decimal, decimal) {
    decimal d1 = 0x1A2Fp-2;
    decimal d2 = 0x1A2FP-2;
    return (d1, d2);
}

// Test assigning positive hexadecimal literal with floating point and positive power.
function testHexAssignment5() returns (decimal, decimal) {
    decimal d1 = 0x1A2F.1C2p2;
    decimal d2 = 0x1A2F.1C2P2;
    return (d1, d2);
}

// Test assigning positive hexadecimal literal with floating point and negative power.
function testHexAssignment6() returns (decimal, decimal) {
    decimal d1 = 0x1A2F.1C2p-2;
    decimal d2 = 0x1A2F.1C2P-2;
    return (d1, d2);
}

// Test assigning negative hexadecimal literal without power and floating point.
function testHexAssignment7() returns decimal {
    decimal d = -0x123F0;
    return d;
}

// Test assigning negative hexadecimal literal with floating point but without power.
function testHexAssignment8() returns decimal {
    decimal d = -0x1A2B.3F0;
    return d;
}

// Test assigning negative hexadecimal literal with positive power but without floating point.
function testHexAssignment9() returns (decimal, decimal) {
    decimal d1 = -0X1A2Fp5;
    decimal d2 = -0x1A2FP5;
    return (d1, d2);
}

// Test assigning negative hexadecimal literal with negative power but without floating point.
function testHexAssignment10() returns (decimal, decimal) {
    decimal d1 = -0x1A2Fp-2;
    decimal d2 = -0X1A2FP-2;
    return (d1, d2);
}

// Test assigning negative hexadecimal literal with floating point and positive power.
function testHexAssignment11() returns (decimal, decimal) {
    decimal d1 = -0x1A2F.1C2p2;
    decimal d2 = -0x1A2F.1C2P2;
    return (d1, d2);
}

// Test assigning negative hexadecimal literal with floating point and negative power.
function testHexAssignment12() returns (decimal, decimal) {
    decimal d1 = -0x1A2F.1C2p-2;
    decimal d2 = -0x1A2F.1C2P-2;
    return (d1, d2);
}

// Test assigning a hexadecimal literal with extra whitespaces
function testHexWithAdditionalWhiteSpaces() returns decimal {
    decimal d =     -    0X1A2F.1C2p-2;
    return d;
}

// Test a complex expression including hexadecimal literals
function testHexComplexExpression() returns decimal {
    decimal d1 = -0x123F0;
    decimal d2 = -200.5;
    decimal d3 = (-1 * <decimal>0x1A2F.1C2p-2 + d1) / d2;
    return d3;
}

// Test positively signed literal assignment
function testPositivelySignedLiteralAssignment() returns (decimal, decimal, decimal, decimal) {
    decimal d1 = +12.23;
    decimal d2 = + 0.0;
    decimal d3 = +0X1A2F.1C2p-2;
    decimal d4 = -12.23;
    decimal d5 = d1 + d4;
    return (d1, d2, d3, d5);
}
