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

import ballerina/jballerina.java;

# Converts a string to a boolean.
#
# Returns the boolean of which parameter `s` is a string representation.
# The accepted representations are `true`, `false`
# (in any combination of lower- and upper-case),
# and also `1` for true and `0` for `false`.
# This is the inverse of function ``value:toString`` applied to a `boolean`.
#
# ```ballerina
# boolean:fromString("true") ⇒ true
# 
# boolean:fromString("0") ⇒ false
# 
# boolean:fromString("01") ⇒ error
# ```
#
# + s - string representing a boolean value
# + return - boolean that parameter `s` represents, or an error if there is no such boolean
public isolated function fromString(string s) returns boolean|error = @java:Method {
    'class: "org.ballerinalang.langlib.bool.FromString",
    name: "fromString"
} external;

# Returns true if one or more of its arguments are true and false otherwise.
# In particular, it returns false if there are no arguments.
# 
# ```ballerina
# boolean:some(true, false) ⇒ true
# boolean:some(false, false) ⇒ false
# boolean:some() ⇒ false
# ```
# 
# + bs - boolean values to be evaluated
# + return - true if one or more of its arguments are true and false otherwise
public isolated function some(boolean... bs) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.bool.Some",
    name: "some"
} external;

# Returns true if all of its arguments are true and false otherwise.
# In particular, it returns true if there are no arguments.
# 
# ```ballerina
# boolean:every(true, false) ⇒ false
# boolean:every(true, true) ⇒ true
# boolean:every() ⇒ true    
# ```
# 
# + bs - boolean values to be evaluated
# + return - true if all of its arguments are true and false otherwise
public isolated function every(boolean... bs) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.bool.Every",
    name: "every"
} external;
