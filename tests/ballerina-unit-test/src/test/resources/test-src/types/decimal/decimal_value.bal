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
