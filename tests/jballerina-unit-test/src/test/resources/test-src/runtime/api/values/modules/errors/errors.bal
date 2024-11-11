// Copyright (c) 2023 WSO2 LLC.
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

public type ErrorDetail record {
    map<string|string[]> headers?;
    anydata body?;
};

public type StatusCodeError distinct error<ErrorDetail>;
public type ResourceDispatchingError RequestDispatchingError;
public type RequestDispatchingError distinct error;
public type ResourceNotFoundError StatusCodeError & ResourceDispatchingError;

// Validate create error API for intersection types and type reference types.
public function validateAPI() {
    error errorVar = getErrorValue("ResourceDispatchingError");
    test:assertTrue(errorVar is ResourceDispatchingError);

    errorVar = getErrorValue("ResourceNotFoundError");
    test:assertTrue(errorVar is ResourceNotFoundError);

    errorVar = getErrorValue("RequestDispatchingError");
    test:assertTrue(errorVar is RequestDispatchingError);

    errorVar = getErrorValue("StatusCodeError");
    test:assertTrue(errorVar is StatusCodeError);
}

function getErrorValue(string errorTypeName) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Values"
} external;
