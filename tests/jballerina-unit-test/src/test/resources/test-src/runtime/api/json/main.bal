// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

public function main() {
    string jsonStr = "[12, true, 123.4, \"hello\"]";
    json expected = [12, true, 123.4, "hello"];
    json|error result = trap testParsingWrongCharset(jsonStr);
    test:assertTrue(result is error, "Invalid error");
    error e = <error> result;
    test:assertEquals(e.message(), "error in parsing input stream: invalid charset");

    result =  testParsingWithProcessingMode(jsonStr);
    test:assertTrue(result is json, "Invalid json");

    result =  testParsingWithOnlyStream(jsonStr);
    test:assertTrue(result is json, "Invalid json");

    result =  testParsingWithStreamAndCharset(jsonStr);
    test:assertTrue(result is json, "Invalid json");

    result =  testBStringParsingWithProcessingMode(jsonStr);
    test:assertTrue(result is json, "Invalid json");

    result = trap testParsingNullString(jsonStr);
    test:assertTrue(result is error, "Invalid error");
    e = <error> result;
    test:assertEquals(e.message(), "Error reading JSON: Stream closed");

    result =  testStringParsingWithProcessingMode(jsonStr);
    test:assertTrue(result is json, "Invalid json");
}

public isolated function testParsingWrongCharset(string str) returns json|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.JsonValues"
} external;

public isolated function testParsingWithProcessingMode(string str) returns json|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.JsonValues"
} external;

public isolated function testParsingWithOnlyStream(string str) returns json|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.JsonValues"
} external;

public isolated function testParsingWithStreamAndCharset(string str) returns json|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.JsonValues"
} external;

public isolated function testStringParsingWithProcessingMode(string str) returns json|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.JsonValues"
} external;

public isolated function testParsingNullString(string str) returns json|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.JsonValues"
} external;

public isolated function testBStringParsingWithProcessingMode(string str) returns json|error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.JsonValues"
} external;
