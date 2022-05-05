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

import ballerina/jballerina.java;

public type GenericError distinct error;

public type UserError distinct GenericError;

public function getError(string errorName) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;

public function getTypeIds(error errorVal) returns string[] = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;

public type IOError distinct error;

public function getDistinctErrorNegative(string msg) returns IOError = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;

public function getErrorNegative1(string msg) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;

public function getErrorNegative2(string msg) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;

public function getErrorWithTypeNegative(string msg) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;

public function getDistinctErrorWithNullDetailNegative(string msg) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;

public function getErrorWithEmptyDetailNegative(string msg) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;

public function getErrorWithNullDetailNegative(string msg) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;

public function getErrorWithEmptyDetailNegative2(string msg) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;

public function getErrorWithNullDetailNegative2(string msg) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;

public function getDistinctErrorWithEmptyDetailNegative2(string msg) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;

public function getDistinctErrorWithNullDetailNegative2(string msg) returns error = @java:Method {
    'class: "org.ballerinalang.nativeimpl.jvm.runtime.api.tests.Errors"
} external;
