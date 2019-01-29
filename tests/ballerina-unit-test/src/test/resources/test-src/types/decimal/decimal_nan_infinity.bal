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

final decimal ZERO = 0.0;
final decimal NaN = <decimal>0.0 / 0.0;
final decimal POSITIVE_INF = <decimal>2.1 / 0.0;
final decimal NEGATIVE_INF = <decimal>(-2.1) / 0.0;


// Test decimal zero divided by zero
function testZeroDividedByZero() returns decimal {
    decimal d1 = 0;
    decimal d2 = <decimal>0/0;
    return d2;
}

// Test positive decimal number divided by zero
function testPositiveNumberDividedByZero() returns decimal {
    decimal d1 = 23.2;
    decimal d2 = 0;
    decimal d3 = d1/d2;
    return d3;
}

// Test negative decimal number divided by zero
function testNegativeNumberDividedByZero() returns decimal {
    decimal d1 = -23.2;
    decimal d2 = 0;
    decimal d3 = d1/d2;
    return d3;
}


// Test addition with LHS operand zero
function testZeroAddition() returns (decimal, decimal, decimal) {
    decimal d1 = ZERO + POSITIVE_INF;
    decimal d2 = ZERO + NEGATIVE_INF;
    decimal d3 = ZERO + NaN;
    return (d1, d2, d3);
}

// Test subtraction with LHS operand zero
function testZeroSubtraction() returns (decimal, decimal, decimal) {
    decimal d1 = ZERO - POSITIVE_INF;
    decimal d2 = ZERO - NEGATIVE_INF;
    decimal d3 = ZERO - NaN;
    return (d1, d2, d3);
}

// Test multiplication with LHS operand zero
function testZeroMultiplication() returns (decimal, decimal, decimal) {
    decimal d1 = ZERO * POSITIVE_INF;
    decimal d2 = ZERO * NEGATIVE_INF;
    decimal d3 = ZERO * NaN;
    return (d1, d2, d3);
}

// Test division with LHS operand zero
function testZeroDivision() returns (decimal, decimal, decimal, decimal) {
    decimal d1 = ZERO / POSITIVE_INF;
    decimal d2 = ZERO / NEGATIVE_INF;
    decimal d3 = ZERO / NaN;
    decimal d4 = ZERO / ZERO;
    return (d1, d2, d3, d4);
}

// Test modulo with LHS operand zero
function testZeroModulo() returns (decimal, decimal, decimal, decimal) {
    decimal d1 = ZERO % POSITIVE_INF;
    decimal d2 = ZERO % NEGATIVE_INF;
    decimal d3 = ZERO % NaN;
    decimal d4 = ZERO % ZERO;
    return (d1, d2, d3, d4);
}


// Test addition with LHS operand positive infinity
function testPositiveInfinityAddition() returns (decimal, decimal, decimal, decimal) {
    decimal d1 = POSITIVE_INF + <decimal> (-45.4);
    decimal d2 = POSITIVE_INF + POSITIVE_INF;
    decimal d3 = POSITIVE_INF + NEGATIVE_INF;
    decimal d4 = POSITIVE_INF + NaN;
    return (d1, d2, d3, d4);
}

// Test subtraction with LHS operand positive infinity
function testPositiveInfinitySubtraction() returns (decimal, decimal, decimal, decimal) {
    decimal d1 = POSITIVE_INF - <decimal> 45.4;
    decimal d2 = POSITIVE_INF - POSITIVE_INF;
    decimal d3 = POSITIVE_INF - NEGATIVE_INF;
    decimal d4 = POSITIVE_INF - NaN;
    return (d1, d2, d3, d4);
}

// Test multiplication with LHS operand positive infinity
function testPositiveInfinityMultiplication() returns (decimal, decimal, decimal, decimal, decimal, decimal) {
    decimal d1 = POSITIVE_INF * <decimal> 45.4;
    decimal d2 = POSITIVE_INF * <decimal> (-45.4);
    decimal d3 = POSITIVE_INF * <decimal> 0.0;
    decimal d4 = POSITIVE_INF * POSITIVE_INF;
    decimal d5 = POSITIVE_INF * NEGATIVE_INF;
    decimal d6 = POSITIVE_INF * NaN;
    return (d1, d2, d3, d4, d5, d6);
}

// Test division with LHS operand positive infinity
function testPositiveInfinityDivision() returns (decimal, decimal, decimal, decimal, decimal, decimal) {
    decimal d1 = POSITIVE_INF / <decimal> 45.4;
    decimal d2 = POSITIVE_INF / <decimal> (-45.4);
    decimal d3 = POSITIVE_INF / <decimal> 0.0;
    decimal d4 = POSITIVE_INF / POSITIVE_INF;
    decimal d5 = POSITIVE_INF / NEGATIVE_INF;
    decimal d6 = POSITIVE_INF / NaN;
    return (d1, d2, d3, d4, d5, d6);
}

// Test modulo with LHS operand positive infinity
function testPositiveInfinityModulo() returns (decimal, decimal, decimal, decimal, decimal, decimal) {
    decimal d1 = POSITIVE_INF % <decimal> 45.4;
    decimal d2 = POSITIVE_INF % <decimal> (-45.4);
    decimal d3 = POSITIVE_INF % <decimal> 0.0;
    decimal d4 = POSITIVE_INF % POSITIVE_INF;
    decimal d5 = POSITIVE_INF % NEGATIVE_INF;
    decimal d6 = POSITIVE_INF % NaN;
    return (d1, d2, d3, d4, d5, d6);
}


// Test addition with LHS operand negative infinity
function testNegativeInfinityAddition() returns (decimal, decimal, decimal, decimal) {
    decimal d1 = NEGATIVE_INF + <decimal> 45.4;
    decimal d2 = NEGATIVE_INF + POSITIVE_INF;
    decimal d3 = NEGATIVE_INF + NEGATIVE_INF;
    decimal d4 = NEGATIVE_INF + NaN;
    return (d1, d2, d3, d4);
}

// Test subtraction with LHS operand negative infinity
function testNegativeInfinitySubtraction() returns (decimal, decimal, decimal, decimal) {
    decimal d1 = NEGATIVE_INF - <decimal> 45.4;
    decimal d2 = NEGATIVE_INF - POSITIVE_INF;
    decimal d3 = NEGATIVE_INF - NEGATIVE_INF;
    decimal d4 = NEGATIVE_INF - NaN;
    return (d1, d2, d3, d4);
}

// Test multiplication with LHS operand negative infinity
function testNegativeInfinityMultiplication() returns (decimal, decimal, decimal, decimal, decimal, decimal) {
    decimal d1 = NEGATIVE_INF * <decimal> 45.4;
    decimal d2 = NEGATIVE_INF * <decimal> (-45.4);
    decimal d3 = NEGATIVE_INF * <decimal> 0.0;
    decimal d4 = NEGATIVE_INF * POSITIVE_INF;
    decimal d5 = NEGATIVE_INF * NEGATIVE_INF;
    decimal d6 = NEGATIVE_INF * NaN;
    return (d1, d2, d3, d4, d5, d6);
}

// Test division with LHS operand negative infinity
function testNegativeInfinityDivision() returns (decimal, decimal, decimal, decimal, decimal, decimal) {
    decimal d1 = NEGATIVE_INF / <decimal> 45.4;
    decimal d2 = NEGATIVE_INF / <decimal> (-45.4);
    decimal d3 = NEGATIVE_INF / <decimal> 0.0;
    decimal d4 = NEGATIVE_INF / POSITIVE_INF;
    decimal d5 = NEGATIVE_INF / NEGATIVE_INF;
    decimal d6 = NEGATIVE_INF / NaN;
    return (d1, d2, d3, d4, d5, d6);
}

// Test modulo with LHS operand negative infinity
function testNegativeInfinityModulo() returns (decimal, decimal, decimal, decimal, decimal, decimal) {
    decimal d1 = NEGATIVE_INF % <decimal> 45.4;
    decimal d2 = NEGATIVE_INF % <decimal> (-45.4);
    decimal d3 = NEGATIVE_INF % <decimal> 0.0;
    decimal d4 = NEGATIVE_INF % POSITIVE_INF;
    decimal d5 = NEGATIVE_INF % NEGATIVE_INF;
    decimal d6 = NEGATIVE_INF % NaN;
    return (d1, d2, d3, d4, d5, d6);
}


// Test mathematical operations with a NaN operand
function testNaNOperations() returns (decimal, decimal, decimal, decimal, decimal) {
    decimal d1 = NaN + NEGATIVE_INF;
    decimal d2 = NaN - POSITIVE_INF;
    decimal d3 = NaN * ZERO;
    decimal d4 = NaN / NaN;
    decimal d5 = NaN % <decimal> 23.2;
    return (d1, d2, d3, d4, d5);
}


// ======================== Test builtin functions ================================

// Test isNaN function
function testIsNaN() returns (boolean, boolean, boolean, boolean, boolean, boolean) {
    decimal d1 = -23.4;
    decimal d2 = POSITIVE_INF + NEGATIVE_INF;
    boolean b1 = NaN.isNaN();
    boolean b2 = d2.isNaN();
    boolean b3 = POSITIVE_INF.isNaN();
    boolean b4 = NEGATIVE_INF.isNaN();
    boolean b5 = ZERO.isNaN();
    boolean b6 = d1.isNaN();
    return(b1, b2, b3, b4, b5, b6);
}

// Test isInfinite function
function testIsInfinite() returns (boolean, boolean, boolean, boolean, boolean, boolean) {
    decimal d1 = -23.4;
    decimal d2 = POSITIVE_INF * NEGATIVE_INF;
    boolean b1 = NaN.isInfinite();
    boolean b2 = ZERO.isInfinite();
    boolean b3 = d1.isInfinite();
    boolean b4 = POSITIVE_INF.isInfinite();
    boolean b5 = NEGATIVE_INF.isInfinite();
    boolean b6 = d2.isInfinite();
    return(b1, b2, b3, b4, b5, b6);
}

// Test isFinite function
function testIsFinite() returns (boolean, boolean, boolean, boolean, boolean, boolean) {
    decimal d1 = -23.4;
    decimal d2 = d1 / <decimal> 0.00001;
    boolean b1 = NaN.isFinite();
    boolean b2 = POSITIVE_INF.isFinite();
    boolean b3 = NEGATIVE_INF.isFinite();
    boolean b4 = ZERO.isFinite();
    boolean b5 = d1.isFinite();
    boolean b6 = d2.isFinite();
    return(b1, b2, b3, b4, b5, b6);
}
