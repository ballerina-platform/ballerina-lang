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

# Prints `any` or `error` value(s) to the STDOUT.
#
#```ballerina
#   print("Start processing the CSV file from ", srcFileName);
#```
#
# + values - The value(s) to be printed.
public isolated function print((any|error)... values) = @java:Method {
    name: "print",
    'class: "org.ballerinalang.benchmark.nativeimpl.Utils"
} external;

# Prints `any` or `error` value(s) to the STDOUT followed by a new line.
#
#```ballerina
#   println("Start processing the CSV file from ", srcFileName);
#```
#
# + values - The value(s) to be printed.
public isolated function println((any|error)... values) = @java:Method {
    name: "println",
    'class: "org.ballerinalang.benchmark.nativeimpl.Utils"
} external;

# Returns a formatted string using the specified format string and arguments.
#
# ```ballerina
#   string s8 = sprintf("%s scored %d for %s and has an average of %.2f.", name, marks, subjects[0], average);
# ```
#
# + format - A format string
# + args   - Arguments referred by the format specifiers in the format string
# + return - The formatted string
public isolated function sprintf(string format, (any|error)... args) returns string = @java:Method {
    name: "sprintf",
    'class: "org.ballerinalang.benchmark.nativeimpl.Utils"
} external;
