// Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

const A = 10;
const B = 20;

type C A|B;

function modIncompatibleTypes() {
    C a = 10;
    string b = "ABC";
    float|int c = 12;
    string|string:Char e = "D";

    int i1 = a % b;
    int i2 = a % c;
    string i3 = b % e;
}

function testImplicitConversion() {
    float a = 1;
    decimal b = 1;
    int c = 1;
    var x1 = a % b;
    any x2 = a % b;
    anydata x5 = c % a;
    anydata x6 = c % b;

    C d = 10;
    float e = 12.25;
    var y1 = d % e;
    any y2 = d % e;
}
