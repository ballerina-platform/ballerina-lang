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

const S = "test string const";

// string-type-descriptor := string
// string-literal :=
// DoubleQuotedStringLiteral | symbolic-string-literal
// DoubleQuotedStringLiteral := " (StringChar | StringEscape)* "
// StringChar := ^ ( 0xA | 0xD | \ | " )
// StringEscape := StringSingleEscape | StringNumericEscape
// StringSingleEscape := \t | \n | \r | \\ | \"
// StringNumericEscape := \u[ CodePoint ]
// CodePoint := HexDigit+
//
@test:Config {}
function testStringLiterals() {
    string s1 = "a";
    string s2 = "A0";
    string s3 = "\t\n";
    string s4 = "b\r\\\"c";
    string s5 = "\u2ABC";
    string s6 = "aA0\t\nb\r\\\"c\u2ABC";
    test:assertEquals(s1 + s2 + s3 + s4 + s5, s6, msg = "expected strings to be equal");

    s1 = "a0Bc";
    s2 = "A";
    s3 = "\t";
    s4 = "\rbc\\\"";
    s5 = "D\u2ABCe\u2222F";
    s6 = "a0BcA\t\rbc\\\"D⪼e∢F";
    test:assertEquals(s1 + s2 + s3 + s4 + s5, s6, msg = "expected strings to be equal");
}

// A string is an immutable sequences of zero or more Unicode code points.
@test:Config {}
function testStringImmutability() {
    string s1 = S;
    string s2 = s1;
    s1 = s1.replace("const", "1");
    test:assertTrue(s1 == "test string 1", msg = "expected s1's value to have changed");
    test:assertTrue(s2 == S, msg = "expected original string to not be mutated");
}
