// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function checkEqualityOfTwoTypes() returns boolean {
    int a;
    string b;
    return a == b;
}

function checkEqualityOfClosedArraysOfDifferentSize() returns boolean {
    int[2] a;
    int[1] b;
    boolean bool1 = a == b;

    float[3] c;
    float[!...] d = [2.1, 3.4];
    boolean bool2 = c == d;

    return bool1 && bool2;
}
