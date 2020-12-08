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

import ballerina/java;

# Tests value equality for two values.
#
# + expected - expected value
# + actual - actual value
public function assertValueEqual(anydata expected, anydata actual) = @java:Method {
    'class: "org.ballerinalang.langlib.test.AssertValueEqual",
    name: "assertValueEqual"
} external;

# Tests whether a value is of type 'error'.
#
# + value - the passed value to be asserted
public function assertError(any|error value) = @java:Method {
    'class: "org.ballerinalang.langlib.test.AssertError",
    name: "assertError"
} external;

# Tests whether a value is of a non-error type.
#
# + value - the passed value to be asserted
public function assertNotError(any|error value) = @java:Method {
    'class: "org.ballerinalang.langlib.test.AssertNotError",
    name: "assertNotError"
} external;

# Tests whether a value is 'true'.
#
# + value - the passed value to be asserted
public function assertTrue(boolean value) = @java:Method {
    'class: "org.ballerinalang.langlib.test.AssertTrue",
    name: "assertTrue"
} external;

# Tests whether a value is 'false'.
#
# + value - the passed value to be asserted
public function assertFalse(boolean value) = @java:Method {
    'class: "org.ballerinalang.langlib.test.AssertFalse",
    name: "assertFalse"
} external;
