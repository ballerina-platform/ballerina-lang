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

import ballerina/jballerina.java;
import ballerina/test;

public function testStringUtils() {
    error v = <error>trap foo();
    string val = getStringVal(v.stackTrace());
    test:assertEquals(val, "object lang.error:CallStack");
}

function foo() {
  bar();
}

function bar() {
  baz();
}

function baz() {
  panic error("Op Failed");
}

function getStringVal(any... values) returns string = @java:Method {
    'class:"org/ballerinalang/test/types/string/StringUtilsTest"
} external;

public function testStringValue() {
    string expected = "ascii~?£ßóµ¥ęЯλĢŃ☃✈௸ऴᛤ😀🄰🍺";
    string val = "ascii~?" + "£ßóµ¥" + "ęЯλĢŃ" + "☃✈௸ऴᛤ" + "😀🄰🍺";
    val = invokeStringValue(val);
    test:assertEquals(val, expected);
}

function invokeStringValue(any values) returns string = @java:Method {
    'class:"org/ballerinalang/test/types/string/StringUtilsTest"
} external;
