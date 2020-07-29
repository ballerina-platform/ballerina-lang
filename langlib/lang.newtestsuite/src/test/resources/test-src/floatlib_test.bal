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

import ballerina/lang.'float as floats;
import ballerina/lang.test as test;


function testIsFinite() {
    float f = 12.34;
    float inf = 1.0/0.0;
    test:assertTrue(f.isFinite());
    test:assertFalse(inf.isFinite());
}

function testIsInfinite() {
    float f = 12.34;
    float inf = 1.0/0.0;
    test:assertFalse(f.isInfinite());
    test:assertTrue(inf.isInfinite());
}

function testSum() {
    test:assertEquals(floats:sum(12.34, 23.45, 34.56), 70.35);
}

function testFloatConsts() returns [float,float] {
    return [floats:NaN, floats:Infinity];
}
