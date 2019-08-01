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

// Test invalid decimal value scenarios.
function testInvlaidDecimalValue() {
    // This is to test a syntax issue.
    decimal a = 12.3f;
    decimal b = 0x4fp1;
    decimal[] ds1 = [12.3f, 22.4];
    decimal[] ds2 = [0x3ap-1, 0x4bp3];
    int|decimal md1 = 0x33p0;
    int|decimal md2 = 22.22;
}

// Test invalid decimal inference.
function testInvalidDecimalInference() {
    decimal a = 1.0 / 2.0f;
    decimal b = 1.0 + 2.0f;
    decimal c = 1.0d + 2.0f;
    decimal d = 1.0 * 2.0f;
    decimal e = 1.0f + 2.0;
    decimal f = 1.0f - 2.0;
    float k = 2.0;
    decimal g = 1.0 * 2.0 + 0.002 / k;
}
