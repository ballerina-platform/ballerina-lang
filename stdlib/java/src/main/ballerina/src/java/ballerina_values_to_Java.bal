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
public function isNull(handle value) returns boolean = @Method {
    class: "org.ballerinalang.nativeimpl.java.IsNull"
} external;

# Returns a `handle` that refers to Java null.
#
# + return - the `handle` that refers to Java null
public function createNull() returns handle = @Method {
    class: "org.ballerinalang.nativeimpl.java.CreateNull"
} external;

# Returns a `handle` that refers to the Java Class object associated
# with the class or interface with the given string name.
#
# This function performs a Java `class.forName(name)` except for following cases:
#
#   name        output
#   boolean     the Java Class instance representing the Java primitive type boolean
#   byte        the Java Class instance representing the Java primitive type byte
#   char        the Java Class instance representing the Java primitive type char
#   short       the Java Class instance representing the Java primitive type short
#   int         the Java Class instance representing the Java primitive type int
#   long        the Java Class instance representing the Java primitive type long
#   float       the Java Class instance representing the Java primitive type float
#   double      the Java Class instance representing the Java primitive type double
# + name - the name of the Java class
# + return - the Java Class object for the class with the given name.
public function getClass(string name) returns handle | error = @Method {
    class: "org.ballerinalang.nativeimpl.java.JavaUtils"
} external;

