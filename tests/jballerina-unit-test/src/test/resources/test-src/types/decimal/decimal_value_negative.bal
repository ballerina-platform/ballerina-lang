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

// Test invalid decimal value scenarios.
function testInvlaidDecimalValue() {
    // This is to test a syntax issue.
    decimal a = 12.3

    // This is to test invalid literal assignment.
    decimal b = 12.3g;

    // This is to test non-supported decimal value assignment that starts with 0.
    decimal d = 023.04;

    // This is to test invalid hexadecimal literal assignment.
    decimal e = -0xX1231.12P1;

    // This is to test hexadecimal literal assignment with invalid extra space.
    decimal f = -0 X1231.12P2;

    // This is to test invalid syntax for missing digit after the dot.
    decimal _ = 2.;
    decimal _ = 2.e12;

    // This is to test invalid syntax for missing hex number after the hex indicator
    decimal _ = 0x;
    decimal _ = 0X;
}
