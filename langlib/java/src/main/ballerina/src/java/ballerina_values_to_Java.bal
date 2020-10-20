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

# Returns a `handle`, which refers to the Java string representation of the Ballerina `string`.
# ```ballerina
# handle header = java:fromString("Content-Type");
# ```
#
# + value - The Ballerina `string` with which the `handle` is created
# + return - The `handle`, which refers to the Java String representation of the Ballerina `string`
public isolated function fromString(string value) returns handle = @Method {
    'class: "org.ballerinalang.langlib.java.FromString",
    name: "fromString"
} external;

# Returns a Ballerina `string` representation of the Java object referred by the `handle`.
# If the `handle` refers to Java null, then this function returns a `nil` value.
# ```ballerina
# string? version = java:toString(versionProperty);
# ```
#
# + value - The `handle` of which the referred value is to be converted to a Ballerina `string`
# + return - The Ballerina `string` representation of the Java object referred by the `handle` or else
#            returns `()` if the `handle` refers to Java null
public isolated function toString(handle value) returns string? = @Method {
    'class: "org.ballerinalang.langlib.java.ToString",
    name: "toString",
    paramTypes: ["io.ballerina.runtime.api.values.BHandle"]
} external;

# Returns `true` if this handle refers to Java null.
# ```ballerina
# boolean status = java:isNull(value);
# ```
#
# + value - The `handle` of which the referred value is to be tested with Java null
# + return - `true` if this handle refers to Java null
public isolated function isNull(handle value) returns boolean = @Method {
    'class: "org.ballerinalang.langlib.java.IsNull",
    name: "isNull"
} external;

# Returns a `handle`, which refers to Java null.
# ```ballerina
# handle nullHandle = java:createNull();
# ```
#
# + return - The `handle`, which refers to Java null
public isolated function createNull() returns handle = @Method {
    'class: "org.ballerinalang.langlib.java.CreateNull",
    name: "createNull"
} external;

# Returns a `handle`, which refers to the Java Class object associated with the class or interface with the given
# string name.
# ```ballerina
# handle|error intClass = java:getClass("int");
# ```
#
# This function performs a Java `class.forName(name)` except for the following cases:
#
# | Name     |   Output                                                      |
# |:---------|:--------------------------------------------------------------|
# | boolean  |   Java Class instance representing the primitive type boolean |
# | byte     |   Java Class instance representing the primitive type byte    |
# | char     |   Java Class instance representing the primitive type char    |
# | short    |   Java Class instance representing the primitive type short   |
# | int      |   Java Class instance representing the primitive type int     |
# | long     |   Java Class instance representing the primitive type long    |
# | float    |   Java Class instance representing the primitive type float   |
# | double   |   Java Class instance representing the primitive type double  |
#
# + name - The name of the Java class
# + return - The Java Class object for the class with the given name
public isolated function getClass(string name) returns handle | error = @Method {
    'class: "org.ballerinalang.langlib.java.JavaUtils",
    name: "getClass",
    paramTypes: ["io.ballerina.runtime.api.values.BString"]
} external;

# Returns an `JObject|error`, which is obtained after casting the provided `JObject` instance
# to the given `JObject` type depending on assignability.
# ```ballerina
# JObject|error obj = java:cast(inputStream, typedesc<FileInputStream>);
# if (obj is JObject) {
#   FileInputStream fileInputStream = <FileInputStream>obj;
# }
# ```
#
# + value - The `JObject` instance which is to be casted
# + castType - The `JObject` implementation type ``typedesc<JObject>`` to which the given object is casted to if assignable
# + return - The `JObject|error`, which refers to the new `JObject` instance or an `error`
public isolated function cast(JObject value, typedesc<JObject> castType) returns JObject|error = @Method {
    'class: "org.ballerinalang.langlib.java.Cast",
    name: "cast"
} external;
