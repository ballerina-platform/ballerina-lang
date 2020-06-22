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

# Tests value equality for two values.
#
# + actual - actual value
# + expected - expected value
# + message - Assertion error message
public function assertEquals(anydata actual, anydata expected, string? message = ()) = external;

# Tests whether a value is of type 'error'.
#
# + value - the passed value to be asserted
# + typ - Assertion error message
# + expectedErrorMessage - Expected error message
# + message - Assertion error message
public function assertError(any|error value, string? expectedErrorMessage = (), string? message = ()) = external;

# Tests whether a value is of a non-error type.
#
# + value - the passed value to be asserted
# + message - Assertion error message
public function assertNotError(any|error value, string? message = ()) = external;

# Tests whether a value is 'true'.
#
# + value - the passed value to be asserted
# + message - Assertion error message
public function assertTrue(boolean value, string? message = ()) = external;

# Tests whether a value is 'false'.
#
# + value - the passed value to be asserted
# + message - Assertion error message
public function assertFalse(boolean value, string? message = ()) = external;

# Tests two objects refer to the same object.
#
# + actual - the passed value to be asserted
# + expected - expected value
# + message - Assertion error message
public function assertSame(any|error actual, any|error expected, string? message = ()) = external;

# Asserts that two objects do not refer to the same object.
#
# + actual - the passed value to be asserted
# + expected - expected value
# + message - Assertion error message
public function assertNotSame(any|error actual, any|error expected, string? message = ()) = external;

# Assert failure is triggered based on user discretion. AssertError is thrown with the given errorMessage.
#
# + message - Assertion error message
public function fail(string? message = ()) = external;

# Tests value inequality for two values.
#
# + actual - actual value
# + expected - expected value
# + message - Assertion error message
public function assertNotEquals(anydata actual, anydata expected, string? message = ()) = external;
