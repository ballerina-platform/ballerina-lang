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

# Returns the environment variable value associated with the provided name.
# ```ballerina
# string port = system:getEnv("HTTP_PORT");
# ```
#
# + name - Name of the environment variable
# + return - Environment variable value if it exists or else an empty string
public function getEnv(@untainted string name) returns string {
    var value = java:toString(nativeGetEnv(java:fromString(name)));
    if (value is string) {
        return value;
    }
    return "";
}

function nativeGetEnv(handle key) returns handle = @java:Method {
    name: "getenv",
    'class: "java.lang.System",
    paramTypes: ["java.lang.String"]
} external;

# Returns the current user's name.
# ```ballerina
# string username = system:getUsername();
# ```
#
# + return - Current user's name if it can be determined or else an empty string
public function getUsername() returns string = @java:Method {
    name: "getUsername",
    'class: "org.ballerinalang.stdlib.system.nativeimpl.GetUsername"
} external;

# Returns the current user's home directory path.
# ```ballerina
# string userHome = system:getUserHome();
# ```
#
# + return - Current user's home directory if it can be determined or else an empty string
public function getUserHome() returns string = @java:Method {
    name: "getUserHome",
    'class: "org.ballerinalang.stdlib.system.nativeimpl.GetUserHome"
} external;

# Returns a random UUID string.
# ```ballerina
# string providerId = system:uuid();
# ```
#
# + return - The random string
public function uuid() returns string {
    var result = java:toString(nativeUuid());
    if (result is string) {
        return result;
    } else {
        panic error("Error occured when converting the UUID to string.");
    }
}

function nativeUuid() returns handle = @java:Method {
    name: "randomUUID",
    'class: "java.util.UUID"
} external;

# Executes an operating system command as a subprocess of the current process.
# ```ballerina
# system:Process|system:Error proc = system:exec("ls", {}, "/", "-la")
# ```
#
# + command - The name of the command to be executed
# + env - Environment variables to be set to the process
# + dir - The current working directory to be set to the process
# + args - Command arguments to be passed in
# + return - A `system:Process` object if successful or else a `system:Error` if a failure occurs
public function exec(@untainted string command, @untainted map<string> env = {},
                     @untainted string? dir = (), @untainted string... args) returns Process|Error = @java:Method {
    name: "exec",
    'class: "org.ballerinalang.stdlib.system.nativeimpl.Exec"
} external;
