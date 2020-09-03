// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Execute tests with Sequence type value assertions

@test:Config {}
function testAssertStringEquals() {
    string concatenated = stringConcat("John", "Doe");
    test:assertEquals(concatenated, "JohnDoe");
}

@test:Config {}
function testAssertStringEqualsNegative() {
    string concatenated = stringConcat("John", "Doe");
    error? err = trap test:assertEquals(concatenated, "DoeJohn");
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertStringNotEquals() {
    string s1 = "abc";
    test:assertNotEquals(s1, "def");
}

@test:Config {}
function testAssertStringNotEqualsNegative() {
    string s1 = "abc";
    error? err = trap test:assertNotEquals(s1, "abc");
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertXmlEquals() {
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `Hello, world!`;
    xml x3 = xml `<!--I am a comment-->`;
    xml x4 = xml `<?target data?>`;
    xml x5 = x1 + x2 + x3 + x4;
    xml x6 = xml `<book>The Lost World</book>` + xml `Hello, world!` + xml `<!--I am a comment-->` + xml `<?target data?>`;
    test:assertEquals(x5, x6);
}

@test:Config {}
function testAssertXmlEqualsNegative() {
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `Hello, world!`;
    xml x3 = xml `<!--I am a comment-->`;
    xml x4 = xml `<?target data?>`;
    xml x5 = x1 + x2 + x3 + x4;
    xml x6 = xml `<book>The Lost World -2</book>` + xml `Hello, world!` + xml `<!--I am a comment-->` + xml `<?target data?>`;
    error? err = trap test:assertEquals(x5, x6);
    test:assertTrue(err is error);
}

@test:Config {}
function testAssertXmlNotEquals() {
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `Hello, world!`;
    xml x3 = xml `<!--I am a comment-->`;
    xml x4 = xml `<?target data?>`;
    xml x5 = x1 + x2 + x3 + x4;
    xml x6 = xml `<book>The Lost World - 2</book>` + xml `Hello, world!` + xml `<!--I am a comment-->` + xml `<?target data?>`;
    test:assertNotEquals(x5, x6);
}

@test:Config {}
function testAssertXmlNotEqualsNegative() {
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `Hello, world!`;
    xml x3 = xml `<!--I am a comment-->`;
    xml x4 = xml `<?target data?>`;
    xml x5 = x1 + x2 + x3 + x4;
    xml x6 = xml `<book>The Lost World</book>` + xml `Hello, world!` + xml `<!--I am a comment-->` + xml `<?target data?>`;
    error? err = trap test:assertNotEquals(x5, x6);
    test:assertTrue(err is error);
}
