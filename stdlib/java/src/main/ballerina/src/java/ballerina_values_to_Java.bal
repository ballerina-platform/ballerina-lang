// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Returns a `handle` that refers to the Java string representation of the Ballerina `string`.
#
# + value - the Ballerina `string` with which the `handle` is created
# + return - the `handle` that refers to the Java String representation of the Ballerina `string`
public function fromString(string value) returns handle = external;

# Returns a Ballerina `string` representation of the Java object referred by the `handle`.
#
# If the `handle` refers to Java null, then this function returns a `nil` value.
#
# + value - the `handle` whose referred value to be converted to Ballerina `string`
# + return - the Ballerina `string` representation of the Java object referred by the `handle` or
#            returns `nil` of the `handle` refers to Java null
public function toString(handle value) returns string? = external;

# Returns `true` if this handle refers to Java null.
#
# + value - the `handle` whose referred value to be tested with Java null
# + return - `true` if this handle refers to Java null
public function isNull(handle value) returns boolean = external;

# Returns a `handle` that refers to Java null.
#
# + return - the `handle` that refers to Java null
public function createNull() returns handle = external;

# Returns a `handle` that refers to the element at the specified index in the given Java array.
#
# This function panics with a `JavaNullReferenceError` error if the `handle` refers to Java null.
#
# + receiver - the `handle` which referes to the Java array
# + index - the index of the element to be returned
# + return - the `handle` that refers to the element at the specified position in the Java array
public function getArrayElement(handle receiver, int index) returns handle = external;

# Replaces the element at the specified index in the given Java array with the specified element.
#
# This function panics with a `JavaNullReferenceError` error if the `handle` refers to Java null.
#
# + receiver - the `handle` which referes to the Java array
# + index - the index of the element to be replaced
# + element - the element to be stored at the specified index
public function setArrayElement(handle receiver, int index, handle element) = external;

# Returns the length of the given Java array.
#
# This function panics with a `JavaNullReferenceError` error if the `handle` refers to Java null.
#
# + receiver - the `handle` which referes to the Java array
# + return - the length of the given Java array
public function getArrayLength(handle receiver) returns int = external;

