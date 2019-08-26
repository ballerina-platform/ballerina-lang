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

import ballerina/io;

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
function testDecimalComparisonOperations() returns [boolean, boolean, boolean, boolean, boolean, boolean] {
    decimal d1 = 45.678432;
    decimal d2 = 45.678432005;

    boolean b1 = (d1 == d2);
    boolean b2 = (d1 != d2);
    boolean b3 = (d1 > d2);
    boolean b4 = (d1 < d2);
    boolean b5 = (d1 >= d2);
    boolean b6 = (d1 <= d2);

    return [b1, b2, b3, b4, b5, b6];
}

// Decimal value passed as a parameter.
function testDecimalParameter(decimal a, decimal b) returns [decimal, decimal] {
    decimal c;
    decimal d;
    c = a;
    d = b;
    return [c, d];
}

// Test int literal assignment for a decimal variable.
function testIntLiteralAssignment() returns [decimal, decimal] {
    decimal d = 12;
    return [d, 15];
}

function testDecimalArrayValue() returns (decimal[]) {
    decimal[] ds = [1.0, 2.0];
    return ds;
}

function testDecimalArrayValueWithDiscriminator() returns (decimal[]) {
    decimal[] ds = [1.0d, 2.0d, 3.0e3d];
    return ds;
}

function testDiscriminatedDecimalLiterals() returns [decimal, decimal, decimal, decimal] {
    var a = 3.22d;
    var b = 0.0e5d;
    decimal c =3.141592653589793238462643383279502d;
    decimal d =3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679d;
    return [a, b, c, d];
}

// Test assigning positive hexadecimal literal without power and floating point.
function testHexAssignment1() returns decimal {
    decimal d = 0x123F0;
    return d;
}

// Test assigning negative hexadecimal literal without power and floating point.
function testHexAssignment7() returns decimal {
    decimal d = -0x123F0;
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
function testPositivelySignedLiteralAssignment() returns [decimal, decimal, decimal] {
    decimal d1 = +12.23;
    decimal d2 = + 0.0;
    decimal d4 = -12.23;
    decimal d5 = d1 + d4;
    return [d1, d2, d5];
}

// Test decimal inference for map literal context
function testDecimalInferenceInMapContext() returns map<decimal> {
    map<decimal> md = { a: 33.3d, b: 33.3, c: 0.1, d: 1, e: 10000000000000000000000.123 };
    return md;
}

// Test decimal inference for binary expressions of literals
function decimalInferenceInLiterals() returns [decimal, decimal, decimal, decimal, decimal] {
    decimal a = 1.0/2.0;
    decimal b = 1.0+2.0;
    decimal c = 1.0 - 2.0;
    decimal d = 1.0 * 2.0;
    decimal k = 10000.0001;
    decimal e = 1.0/2.0/1.0 + 1.0*0.005 + k;
    return [a, b, c, d, e];
}

// Test decimal array value load
public function decimalArrayLoad() returns decimal {
    decimal[] a = [1.0, 2.0];
    io:println(a[0]);
    return a[1];
}
