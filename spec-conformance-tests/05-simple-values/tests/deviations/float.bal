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

// The multiple bit patterns that IEEE 754 treats as NaN are considered to be the same value in Ballerina
// TODO: Decimal does not support division by zero
// TODO: == and === not working for float NAN values
@test:Config {}
function testFloatingPointNaNValuesBroken() {
    float f1 = +0.0/0.0;
    float f2 = -0.0/0.0;
    //test:assertTrue(f1 == f2, msg = "expected +0.0 and -0.0 to be of same value");
    test:assertTrue(f1 != f2, msg = "expected +0.0 and -0.0 to be of same value");
    //test:assertTrue(f1 === f2, msg = "expected +0.0 and -0.0 to be of same value");
    test:assertTrue(f1 !== f2, msg = "expected +0.0 and -0.0 to be of same value");

    //decimal d1 = <decimal>+0.0/0.0;
    //decimal d2 = <decimal>-0.0/0.0;
    ////test:assertTrue(d1 == d2, msg = "expected +0.0 and -0.0 to be of same value");
    //test:assertTrue(d1 != d2, msg = "expected +0.0 and -0.0 to be of same value");
    ////test:assertTrue(d1 === d2, msg = "expected +0.0 and -0.0 to be of same value");
    //test:assertTrue(d1 !== d2, msg = "expected +0.0 and -0.0 to be of same value");
}

// Positive and negative zero of a floating point basic type are distinct values,
// following IEEE 754, but are defined to have the same shape, so that they will usually be
// treated as being equal.
// TODO: Decimal and Float zeros should be distinct values
@test:Config {
    groups: ["broken"]
}
function testFloatingPointZeroValuesBroken() {
    float f1 = +0.0;
    float f2 = -0.0;
    //test:assertTrue(f1 !== f2, msg = "expected +0.0 and -0.0 to be distinct values");
    test:assertTrue(f1 === f2, msg = "expected +0.0 and -0.0 to be distinct values");

    decimal d1 = <decimal>+0.0;
    decimal d2 = <decimal>-0.0;
    //test:assertTrue(d1 !== d2, msg = "expected +0.0 and -0.0 to be distinct values");
    test:assertTrue(d1 === d2, msg = "expected +0.0 and -0.0 to be distinct values");
    test:assertTrue(d1 == d2, msg = "expected +0.0 and -0.0 to be of same value");
}
