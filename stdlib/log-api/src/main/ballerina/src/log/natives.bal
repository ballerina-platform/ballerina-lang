// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Logs the specified value at DEBUG level.
# ```ballerina
# log:printDebug("debug log");
# ```
#
# + msg - The message to be logged
public function printDebug(any msg) = @java:Method {
    class: "org.ballerinalang.stdlib.log.Utils"
} external;

# Logs the specified message at ERROR level.
# ```ballerina
# error e = error("error occurred");
# log:printError("error log with cause", err = e);
# ```
# 
# + msg - The message to be logged
# + err - The error struct to be logged
public function printError(any msg, public error? err = ()) = @java:Method {
    class: "org.ballerinalang.stdlib.log.Utils"
} external;

# Logs the specified message at INFO level.
# ```ballerina
# log:printInfo("info log");
# ```
# 
# + msg - The message to be logged
public function printInfo(any msg) = @java:Method {
    class: "org.ballerinalang.stdlib.log.Utils"
} external;

# Logs the specified message at TRACE level.
# ```ballerina
# log:printTrace("trace log");
# ```
# 
# + msg - The message to be logged
public function printTrace(any msg) = @java:Method {
    class: "org.ballerinalang.stdlib.log.Utils"
} external;

# Logs the specified message at WARN level.
# ```ballerina
# log:printWarn("warn log");
# ```
# 
# + msg - The message to be logged
public function printWarn(any msg) = @java:Method {
    class: "org.ballerinalang.stdlib.log.Utils"
} external;

# Logs a formatted string using the specified format string and arguments at DEBUG level.
#
# b - boolean
#
# B - boolean (ALL_CAPS)
#
# d - int
#
# f - float
#
# x - hex
#
# X - HEX (ALL_CAPS)
#
# s - string (This specifier is applicable for any of the supported types in Ballerina.
#             These values will be converted to their string representation.)
#
# ```ballerina
# log:sprintDebug("Employee %s is %d years old", "John", 28);
# ```
#
# + format - A format string
# + args   - The value(s) to be logged.
public function sprintDebug(string format, (any|error)... args) {
    var result = sprintDebugExtern(java:fromString(format), ...args);
}

# Logs a formatted string using the specified format string and arguments at ERROR level.
#
# b - boolean
#
# B - boolean (ALL_CAPS)
#
# d - int
#
# f - float
#
# x - hex
#
# X - HEX (ALL_CAPS)
#
# s - string (This specifier is applicable for any of the supported types in Ballerina.
#             These values will be converted to their string representation.)
#
# ```ballerina
# log:sprintError("Employee %s is %d years old", "John", 28);
# ```
#
# + format - A format string
# + args   - The value(s) to be logged.
public function sprintError(string format, (any|error)... args) {
    var result = sprintErrorExtern(java:fromString(format), ...args);
}

# Logs a formatted string using the specified format string and arguments at INFO level.
#
# b - boolean
#
# B - boolean (ALL_CAPS)
#
# d - int
#
# f - float
#
# x - hex
#
# X - HEX (ALL_CAPS)
#
# s - string (This specifier is applicable for any of the supported types in Ballerina.
#             These values will be converted to their string representation.)
#
# ```ballerina
# log:sprintInfo("Employee %s is %d years old", "John", 28);
# ```
#
# + format - A format string
# + args   - The value(s) to be logged.
public function sprintInfo(string format, (any|error)... args) {
    var result = sprintInfoExtern(java:fromString(format), ...args);
}

# Logs a formatted string using the specified format string and arguments at TRACE level.
#
# b - boolean
#
# B - boolean (ALL_CAPS)
#
# d - int
#
# f - float
#
# x - hex
#
# X - HEX (ALL_CAPS)
#
# s - string (This specifier is applicable for any of the supported types in Ballerina.
#             These values will be converted to their string representation.)
#
# ```ballerina
# log:sprintTrace("Employee %s is %d years old", "John", 28);
# ```
#
# + format - A format string
# + args   - The value(s) to be logged.
public function sprintTrace(string format, (any|error)... args) {
    var result = sprintTraceExtern(java:fromString(format), ...args);
}

# Logs a formatted string using the specified format string and arguments at WARN level.
#
# b - boolean
#
# B - boolean (ALL_CAPS)
#
# d - int
#
# f - float
#
# x - hex
#
# X - HEX (ALL_CAPS)
#
# s - string (This specifier is applicable for any of the supported types in Ballerina.
#             These values will be converted to their string representation.)
#
# ```ballerina
# log:sprintWarn("Employee %s is %d years old", "John", 28);
# ```
#
# + format - A format string
# + args   - The value(s) to be logged.
public function sprintWarn(string format, (any|error)... args) {
    var result = sprintWarnExtern(java:fromString(format), ...args);
}

function sprintDebugExtern(handle format, (any|error)... args) = @java:Method {
    name: "sprintDebug",
    class: "org.ballerinalang.stdlib.log.Utils"
} external;

function sprintErrorExtern(handle format, (any|error)... args) = @java:Method {
    name: "sprintError",
    class: "org.ballerinalang.stdlib.log.Utils"
} external;

function sprintInfoExtern(handle format, (any|error)... args) = @java:Method {
    name: "sprintInfo",
    class: "org.ballerinalang.stdlib.log.Utils"
} external;

function sprintTraceExtern(handle format, (any|error)... args) = @java:Method {
    name: "sprintTrace",
    class: "org.ballerinalang.stdlib.log.Utils"
} external;

function sprintWarnExtern(handle format, (any|error)... args) = @java:Method {
    name: "sprintWarn",
    class: "org.ballerinalang.stdlib.log.Utils"
} external;
