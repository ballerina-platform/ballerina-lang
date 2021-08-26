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

# Prints `any|error` value to the STDOUT.
#
# + value - The value to be printed
public isolated function println(any|error value) {
    externPrintln(out(), java:fromString(value is error ? value.toString() : value.toString()));
}

# Retrieves the current system output stream.
#
# + return - The current system output stream
isolated function out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

# Calls `println` method of the `PrintStream`.
#
# + receiver - The `handle`, which refers to the current system output stream
# + message - The `handle`, which refers to the Java String representation of the Ballerina `string`
isolated function externPrintln(handle receiver, handle message) = @java:Method {
    paramTypes: ["java.lang.String"],
    'class: "java.io.PrintStream",
    name: "println"
} external;
