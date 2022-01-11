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
// Prints `Hello, World!`.

import ballerina/jballerina.java;
import ballerina/lang.test as test;

public function main() {

    object {}|error invalidObject = trap getInvalidObject("Object");
    anydata|error invalidRecord = trap getInvalidRecord("Record");
    error invalidError = trap getInvalidError("Error");

    test:assertTrue(invalidObject is error);
    test:assertTrue(invalidRecord is error);

    error e1 = <error>invalidObject;
    error e2 = <error>invalidRecord;

    test:assertValueEqual("No such object: Object", e1.message());
    test:assertValueEqual("No such record: Record", e2.message());
    test:assertValueEqual("No such error: Error", invalidError.message());
}

function getInvalidObject(string objectName) returns object {} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getInvalidRecord(string recordName) returns record {} = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;

function getInvalidError(string errorName) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;
