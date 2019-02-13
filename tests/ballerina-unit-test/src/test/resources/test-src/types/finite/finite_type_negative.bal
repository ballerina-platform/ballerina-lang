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

const int ICONST = 5;
const float FCONST = 5;
const decimal DCONST = 5;
const int ICONST2 = 100;
const float FCONST2 = 100.5;
const string SCONST = "S";

type Number ICONST|DCONST|FCONST;

type Finite ICONST|ICONST2;

type FloatingPoint FCONST2|SCONST;

// Test invalid literal assignment
function testInvalidAssignment() {
    Number n = 5.0;
}

// Test invalid string assignment
function testDifferentLiteralKindsWithSameValue() {
    Finite f = "5";
}

// Test float with same value but different precision assignment
function testDifferentPrecisionFloatAssignment() {
    FloatingPoint f = 100.50000000;
}
