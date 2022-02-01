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

import ballerina/test;

// Decimal array.
function testDecimalArray() returns [decimal[], int, decimal, decimal] {
    decimal[] dArr = [12.3, 23.2, 34.534, 5.4];
    int length = dArr.length();
    decimal element0 = dArr[0];
    decimal element1 = dArr[1];
    return [dArr, length, element0, element1];
}

// Decimal map.
function testDecimalMap() returns [map<decimal>, int, string[], decimal] {
    map<decimal> dMap = {element0: 12.45, element1: 34.3, element2: 2314.31};
    int length = dMap.length();
    string[] keys = dMap.keys();
    decimal element0 = dMap.get("element0");
    return [dMap, length, keys, element0];
}

// Test record with decimal fields
function testDecimalRecord() returns [decimal, decimal] {
    Animal a = {weight: 23.45, height: 120.43};
    return [a.weight, a.height];
}

// Test object with decimal fields
function testDecimalObject() returns [string, int, decimal, decimal] {
    Student s = new (57.25, 168.67);
    return [s.name, s.age, s.weight, s.height];
}

// Record with decimal fields
type Animal record {
    decimal weight = 0.0;
    decimal height = 0.0;
};

// Object with decimal fields
class Student {
    string name = "";
    int age = 0;
    decimal weight = 65.65;
    decimal height = 0.0;

    function init(decimal weight, decimal height, string name = "Bob", int age = 25) {
        self.name = name;
        self.age = age;
        self.weight = weight;
        self.height = height;
    }
}

// Decimal defaultable parameters
function testDecimalDefaultable() {
    decimal price = decimalDefaultable(540.5, discountRate = 0.1);
    decimal expected = 496.73;
    test:assertEquals(price, expected);
}

function decimalDefaultable(decimal fixedPrice, decimal tax = 10.28, decimal discountRate = 0.05) returns decimal {
    decimal discount = fixedPrice * discountRate;
    decimal price = fixedPrice + tax - discount;
    return price;
}

// Decimal exponents 
function testDecimalNegativeLargeExponents() {
    decimal|error value = trap getDecimal1();
    test:assertTrue(value is error);
    error err = <error>value;
    test:assertEquals(err.message(), "{ballerina}DecimalExponentError");
    var message = err.detail()["message"];
    string messageString = message is error ? message.toString() : message.toString();
    test:assertEquals(messageString, "too many exponents found in decimal value '99999999.9e99999999999999'");

    value = trap getDecimal2();
    test:assertTrue(value is error);
    err = <error>value;
    test:assertEquals(err.message(), "{ballerina}DecimalExponentError");
    message = err.detail()["message"];
    messageString = message is error ? message.toString() : message.toString();
    test:assertEquals(messageString, "too many exponents found in decimal value '99999999.9e9999999999'");
}

function getDecimal1() returns decimal {
    return 99999999.9e99999999999999;
}

function getDecimal2() returns decimal {
    return 99999999.9e9999999999;
}
