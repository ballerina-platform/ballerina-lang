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

function testBooleanFreeze(boolean a) returns boolean {
    boolean b = a.freeze();
    //return a === b;
    return a == b;
}

function testIntFreeze(int a) returns boolean {
    int b = a.freeze();
    return a == b;
}

function testByteFreeze(byte a) returns boolean {
    byte b = a.freeze();
    return a == b;
}

function testFloatFreeze(float a) returns boolean {
    float b = a.freeze();
    return a == b;
}

function testStringFreeze(string a) returns boolean {
    string b = a.freeze();
    return a == b;
}

function testBasicTypesAsJson() returns boolean {
    json a = 5;
    json b = a.freeze();
    boolean equals = a == b;

    a = 5.1;
    b = a.freeze();
    equals = equals && a == b;

    a = "Hello from Ballerina";
    b = a.freeze();
    equals = equals && a == b;

    a = true;
    b = a.freeze();
    return equals && a == b;
}
