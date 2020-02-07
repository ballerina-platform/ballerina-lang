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
function testIsNaN() returns [boolean, boolean, boolean] {
    float f = 0.0/0.0;
    boolean bool = f.isNaN();
    return [bool, f.isInfinite(), f.isFinite()];
}

function testIsInfinite() returns [boolean, boolean, boolean] {
    float f = 1.0/0.0;
    boolean bool = f.isInfinite();
    return [bool, f.isFinite(), f.isNaN()];
}

function testIsFinite() returns [boolean, boolean, boolean] {
    float f = 6.0/2.5;
    boolean bool = f.isFinite();
    return [bool, f.isInfinite(), f.isNaN()];
}

function testWithCalc() returns [boolean, boolean, boolean] {
    float f = 6.0/2.0;
    f = f + 20;
    f = f / 0.0;
    boolean bool = f.isInfinite();
    return [bool, f.isFinite(), f.isNaN()];
}

function testModWithDivisorAsZero() returns [boolean, boolean, boolean] {
    float a = 10.0 % 0.0;
    return [a.isNaN(), a.isInfinite(), a.isFinite()];
}

function testModWithDivisorAsFinite() returns [boolean, boolean, boolean] {
    float a = 10.0 % 3.0;
    return [a.isNaN(), a.isInfinite(), a.isFinite()];
}

function testModZeroByZero() returns [boolean, boolean, boolean] {
    float a = 0.0 % 0.0;
    return [a.isNaN(), a.isInfinite(), a.isFinite()];
}
