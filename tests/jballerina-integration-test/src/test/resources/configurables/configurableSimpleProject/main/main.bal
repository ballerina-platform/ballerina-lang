// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/jballerina.java;

configurable int intVar = 5;
configurable byte byteVar = ?;
configurable float floatVar = 9.5;
configurable string stringVar = ?;
configurable boolean booleanVar = ?;
configurable decimal decimalVar = ?;
configurable xml & readonly xmlVar = ?;

configurable string files = "abc+def.txt";
configurable string data = "Hindu-123";

enum Colors {
    RED,
    GREEN
}

enum CountryCodes {
    SL = "Sri Lanka",
    US = "United States"
}

configurable Colors & readonly color = ?;
configurable CountryCodes & readonly countryCode = ?;

public function main() {
    testSimpleValues();
    testXmlValues();
    testEnumValues();
    print("Tests passed");
}

function testSimpleValues() {
    test:assertEquals(42, intVar);
    test:assertEquals(3.5, floatVar);
    test:assertEquals("waru=na", stringVar);
    test:assertTrue(booleanVar);

    decimal result = 24.87;
    test:assertEquals(result, decimalVar);

    byte result2 = 22;
    test:assertEquals(byteVar, result2);

    test:assertEquals("pqr-1.toml", files);
    test:assertEquals("intVar=bbb", data);
}

function testXmlValues() {
    xml x1 = xml `<book>The Lost World</book>`;
    test:assertEquals(xmlVar, x1);
}

function testEnumValues() {
    test:assertEquals(color, RED);
    test:assertEquals(countryCode, SL);
}

//Extern methods to verify no errors while testing
function system_out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function println(handle receiver, handle arg0) = @java:Method {
    name: "println",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.String"]
} external;

function print(string str) {
    println(system_out(), java:fromString(str));
}
