 // Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 
configurable int testInt = ?;
configurable float testFloat = 66.8;
configurable string testString = "test";
configurable boolean testBoolean = ?;
configurable xml xmlVal = ?;

@test:Config {}
 function testBooleanValue() {
    boolean res = getBoolean();
    test:assertEquals(res, true);
 }

@test:Config {}
 function testAverage() {
    float res = getAverage();
    test:assertEquals(res, 22.25);
 }
 
@test:Config {}
 function testStringValue() {
    string res = getString();
    test:assertEquals(res, "main test");
 }

@test:Config {}
 function testInternalVariables() {
    test:assertEquals(testInt, 30);
    test:assertEquals(testFloat, 5.6);
    test:assertEquals(testString, "cli arg");
    test:assertEquals(testBoolean, false);
 }

@test:Config {}
  function testXml() {
    xml xml1 = xml `<book>The Lost Symbol</book>`;
    test:assertEquals(xmlVal, xml1);
  }
