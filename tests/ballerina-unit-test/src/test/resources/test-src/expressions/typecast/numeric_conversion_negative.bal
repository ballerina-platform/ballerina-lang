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

function testInvalidCastFromBasicTypeToBasicType() {
    int i = 1;
    string s = "test string";
    float f = 1.0;
    boolean b = true;
    decimal d = 10.0;

    string s1 = <string> i;
    boolean b1 = <boolean> i;

    string s2 = <string> f;
    boolean b2 = <boolean> f;

    string s3 = <string> d;
    boolean b3 = <boolean> d;

    int ci1 = <int> b;
    string cs1 = <string> b;
    float cf1 = <float> b;
    decimal cd1 = <decimal> b;

    int ci2 = <int> s;
    boolean cb1 = <boolean> s;
    float cf2 = <float> s;
    decimal cd2 = <decimal> s;
}

function testInvalidCastToUnionWithMultipleBasicNumericTypes() {
    float f = 1.0;
    int|decimal cid1 = <int|decimal> f;
}

function getString(string s) returns string {
    return s;
}
