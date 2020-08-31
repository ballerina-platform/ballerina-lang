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
    Student s = new(57.25, 168.67);
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
