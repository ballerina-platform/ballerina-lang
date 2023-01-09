// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Calls a function dynamically.
#
# If the arguments specified in `args` are not of the type required by `func`,
# then this will panic.
#
# ```ballerina
# function getGreeting(string? name = ()) returns string => name is () ? "Hello" : string `Hello ${name}!`;
#
# function:call(getGreeting) ⇒ Hello
#
# function:call(getGreeting, "David") ⇒ Hello David!
#
# function:call(getGreeting, 1) ⇒ panic
# ```
#
# + func - the function to be called
# + args - the arguments to be passed to the function
# + return - the return value of the call to the function
public isolated function call(@isolatedParam function func, any|error... args) returns any|error = @java:Method {
    'class: "org.ballerinalang.langlib.function.Call"
} external;
