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

byte b0 = 0;
byte b1 = 1;
byte b254 = 254;
byte b255 = 255;

// byte-type-descriptor := byte

// The byte type is an abbreviation for a union of the int values in the range 0 to 255 inclusive.
@test:Config {
    dataProvider: "byteValueDataProvider"
}
function testByteTypeDescriptor(byte bVal) {
    byte bVal2 = bVal;
    test:assertEquals(bVal2, bVal, msg = "expected variable to hold the assigned value");
}

function byteValueDataProvider() returns byte[][] {
    return [
        [b0],
        [b1],
        [b254],
        [b255]
    ];
}
