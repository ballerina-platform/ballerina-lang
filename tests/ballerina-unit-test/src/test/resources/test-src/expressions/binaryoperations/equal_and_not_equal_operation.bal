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

function checkBooleanEquality(boolean a, boolean b) returns boolean {
    return (a == b) && !(a != b);
}

function checkIntEquality(int a, int b) returns boolean {
    return (a == b) && !(a != b);
}

function checkFloatEquality(float a, float b) returns boolean {
    return (a == b) && !(a != b);
}

function checkStringEquality(string a, string b) returns boolean {
    return (a == b) && !(a != b);
}

function checkEqualityToNil(any a) returns boolean {
    return (a == ()) && !(a != ());
}

function check1DArrayEquality(boolean[]|int[]|float[]|string[] a, boolean[]|int[]|float[]|string[] b) returns boolean {
    return (a == b) && !(a != b);
}

function checkJsonEquality(json a, json b) returns boolean {
    return (a == b) && !(a != b);
}