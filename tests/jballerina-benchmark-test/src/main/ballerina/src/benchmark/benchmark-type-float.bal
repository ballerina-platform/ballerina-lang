// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public function benchmarkFloatAddition() {
    float b = 9.9;
    float a = 10.1;
    float c = a + b;
}

public function benchmarkFloatMultiplication() {
    float a = 2.5;
    float b = 5.5;
    float c = a * b;
}

public function benchmarkFloatSubtraction() {
    float a = 25.5;
    float b = 15.5;
    float c = a - b;
}

public function benchmarkFloatDivision() {
    float a = 25.5;
    float b = 5.1;
    float c = a / b;
}

public function benchmarkFloatAdditionWithReturn() {
    float a = floatAddition();
}

function floatAddition() returns (float) {
    float b = 9.9;
    float a = 10.1;
    return a + b;
}

public function benchmarkFloatMultiplicationWithReturn() {
    float a = floatMultiplication();
}

function floatMultiplication() returns (float) {
    float a = 2.5;
    float b = 5.5;
    return a * b;
}

public function benchmarkFloatSubtractionWithReturn() {
    float a = floatSubtraction();
}

function floatSubtraction() returns (float) {
    float a = 25.5;
    float b = 15.5;
    return a - b;
}

public function benchmarkFloatDivisionWithReturn() {
    float a = floatDivision();
}

function floatDivision() returns (float) {
    float a = 25.5;
    float b = 5.1;
    return a / b;
}

