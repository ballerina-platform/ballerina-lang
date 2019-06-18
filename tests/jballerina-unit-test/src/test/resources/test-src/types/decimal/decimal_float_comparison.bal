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

// Test that compares the representations of a very large number in float type (64 bits) and decimal type (128 bits).
function testLargeFloatingPointNumber() returns [float, decimal] {
    float f =
    4354224522222222222222222222222888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888435345345345346456345232545555555555545768768762452452452352345245245234524524524524534534556.24534534534534534534534534534542524444444444434;

    decimal d =
    4354224522222222222222222222222888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888435345345345346456345232545555555555545768768762452452452352345245245234524524524524534534556.24534534534534534534534534534542524444444444434;

    return [f, d];
}

// Test that demonstrates how decimal type resolves the floating point error problem exists in the float type.
function testPrecisionCorrectness() returns [boolean, boolean] {
    float f1 = 0.1;
    float f2 = 0.2;
    float f3 = 0.3;
    boolean b1 = (f1 + f2 == f3); // EXPECTED: false

    decimal d1 = 0.1;
    decimal d2 = 0.2;
    decimal d3 = 0.3;
    boolean b2 = (d1 + d2 == d3); // EXPECTED: true

    return [b1, b2];
}
