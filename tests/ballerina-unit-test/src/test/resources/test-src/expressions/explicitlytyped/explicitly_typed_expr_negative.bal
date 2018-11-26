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

function testInvalidExplicitTypeFromString() {
    string s1 = "Ballerina says Hello World";
    string s2 = getString(s1);

    float f1 = <float> s1;
    anydata f2 = <float> s2;

    anydata d1 = <decimal> s1;
    decimal d2 = <decimal> s2;

    int i1 = <int> s1;
    anydata i2 = <int> s2;

    anydata b1 = <byte> s1;
    byte b2 = <byte> s2;

    anydata bl1 = <boolean> s1;
    boolean bl2 = <boolean> s2;
}

function getString(string s) returns string {
    return s;
}
