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

// Test adding two byte values
public function testByteAddition1() returns int {
    byte b1 = 23;
    byte b2 = 12;
    int b3 = b1 + b2;
    return b3;
}

// Test adding two byte values
public function testByteAddition2() returns int {
    byte b1 = 43;
    byte b2 = 236;
    int b3 = b1 + b2;
    return b3;
}

// Test adding byte and int
public function testByteIntAddition1() returns int {
    byte b1 = 43;
    int b2 = 3456;
    int b3 = b1 + b2;
    return b3;
}

// Test adding int and byte
public function testByteIntAddition2() returns int {
    int b1 = -423;
    byte b2 = 34;
    int b3 = b1 + b2;
    return b3;
}

// Test subtraction between two byte values
public function testByteSubtraction1() returns int {
    byte b1 = 255;
    byte b2 = 55;
    int b3 = b1 - b2;
    return b3;
}

// Test subtraction between two byte values
public function testByteSubtraction2() returns int {
    byte b1 = 55;
    byte b2 = 255;
    int b3 = b1 - b2;
    return b3;
}

// Test subtraction between byte and int
public function testByteIntSubtraction1() returns int {
    byte b1 = 55;
    int b2 = 2555;
    int b3 = b1 - b2;
    return b3;
}

// Test subtraction between int and byte
public function testByteIntSubtraction2() returns int {
    int b1 = -2555;
    byte b2 = 55;
    int b3 = b1 - b2;
    return b3;
}

// Test multiplication between two byte values
public function testByteMultiplication1() returns int {
    byte b1 = 12;
    byte b2 = 20;
    int b3 = b1 * b2;
    return b3;
}

// Test multiplication between two byte values
public function testByteMultiplication2() returns int {
    byte b1 = 250;
    byte b2 = 110;
    int b3 = b1 * b2;
    return b3;
}

// Test multiplication between byte and int
public function testByteIntMultiplication1() returns int {
    byte b1 = 55;
    int b2 = -200;
    int b3 = b1 * b2;
    return b3;
}

// Test multiplication between int and byte
public function testByteIntMultiplication2() returns int {
    int b1 = 300;
    byte b2 = 50;
    int b3 = b1 * b2;
    return b3;
}

// Test byte division
public function testByteDivision1() returns int {
    byte b1 = 22;
    byte b2 = 10;
    int b3 = b1 / b2;
    return b3;
}

// Test byte division
public function testByteDivision2() returns int {
    byte b1 = 250;
    byte b2 = 70;
    int b3 = b1 / b2;
    return b3;
}

// Test dividing a byte by int
public function testByteIntDivision1() returns int {
    byte b1 = 155;
    int b2 = -22;
    int b3 = b1 / b2;
    return b3;
}

// Test dividing an int by byte
public function testByteIntDivision2() returns int {
    int b1 = 334;
    byte b2 = 21;
    int b3 = b1 / b2;
    return b3;
}

// Test byte modulus
public function testByteModulus1() returns int {
    byte b1 = 22;
    byte b2 = 10;
    int b3 = b1 % b2;
    return b3;
}

// Test byte modulus
public function testByteModulus2() returns int {
    byte b1 = 250;
    byte b2 = 110;
    int b3 = b1 % b2;
    return b3;
}

// Test modulus between byte and int
public function testByteIntModulus1() returns int {
    byte b1 = 155;
    int b2 = -22;
    int b3 = b1 % b2;
    return b3;
}

// Test modulus between int and byte
public function testByteIntModulus2() returns int {
    int b1 = 334;
    byte b2 = 21;
    int b3 = b1 % b2;
    return b3;
}

// Test complex expression
public function testByteComplexExpression() returns int {
    byte[] b = [23, 47, 165, 234];
    byte b1 = b[0];
    byte b2 = b[1];
    int b3 = b1 + b2 * (b[3] / b[2]) - b[3];
    return b3;
}

// Test bitwise AND between bytes
public function testByteBitwiseAnd() returns byte {
    byte b1 = 191;
    byte b2 = 88;
    byte b3 = b1 & b2;
    return b3;
}

// Test bitwise AND between int and byte
public function testIntByteBitwiseAnd() returns int {
    int b1 = 959;
    int b2 = 88;
    int b3 = b1 & b2;
    return b3;
}

// Test bitwise AND between byte and int
public function testByteIntBitwiseAnd() returns int {
    int b1 = 88;
    int b2 = -1;
    int b3 = b1 & b2;
    return b3;
}

// Test bitwise AND between ints
public function testIntBitwiseAnd() returns int {
    int i1 = 959;
    int i2 = 447;
    int i3 = i1 & i2;
    return i3;
}

// Test bitwise OR between bytes
public function testByteBitwiseOr() returns byte {
    byte b1 = 191;
    byte b2 = 88;
    byte b3 = b1 | b2;
    return b3;
}

// Test bitwise OR between int, byte
public function testIntByteBitwiseOr() returns int {
    int b1 = 703;
    byte b2 = 88;
    int b3 = b1 | b2;
    return b3;
}

// Test bitwise OR between byte, int
public function testByteIntBitwiseOr() returns int {
    byte b1 = 80;
    int b2 = 696;
    int b3 = b1 | b2;
    return b3;
}

// Test bitwise OR between ints
public function testIntBitwiseOr() returns int {
    int i1 = -80;
    int i2 = 696;
    int i3 = i1 | i2;
    return i3;
}

// Test bitwise XOR between bytes
public function testByteBitwiseXor() returns byte {
    byte b1 = 191;
    byte b2 = 88;
    byte b3 = b1 ^ b2;
    return b3;
}

// Test bitwise XOR between int, byte
public function testIntByteBitwiseXor() returns int {
    int b1 = -191;
    byte b2 = 43;
    int b3 = b1 ^ b2;
    return b3;
}

// Test bitwise XOR between byte, int
public function testByteIntBitwiseXor() returns int {
    byte b1 = 43;
    int b2 = 191;
    int b3 = b1 ^ b2;
    return b3;
}

// Test bitwise XOR between ints
public function testIntBitwiseXor() returns int {
    int i1 = -443;
    int i2 = 191;
    int i3 = i1 ^ i2;
    return i3;
}

// Test byte bitwise complement
public function testByteBitwiseComplement() returns byte {
    byte b = 191;
    return ~b;
}

// Test int bitwise complement
public function testIntBitwiseComplement() returns int {
    int i = 191;
    return ~i;
}
