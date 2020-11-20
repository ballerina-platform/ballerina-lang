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

# Returns rounded positive square root of the given value.
#
# ```ballerina
#   float squareRoot = sqrt(6.4);
# ```
#
# + val - Value to get square root
# + return - Calculated square root value
public isolated function sqrt(float val) returns float = @java:Method {
    name: "sqrt",
    'class: "java.lang.Math"
} external;

# Calculates the smallest (closest to negative infinity)
# double value that is greater than or equal to the argument
# and is equal to a mathematical integer.
#
# ```ballerina
#   float ceilingValue = ceil(6.4);
# ```
#
# + val - Value to get the ceil
# + return - Calculated smallest double value
public isolated function ceil(float val) returns float = @java:Method {
    name: "ceil",
    'class: "java.lang.Math"
} external;

# Calculates the closest int to the argument, with ties rounding to positive infinity.
#
# ```ballerina
#   int roundedIntegerValue = round(6.4);
# ```
#
# + val - A floating-point value to be rounded to an integer
# + return - Calculated value of the argument rounded to the nearest int value
public isolated function round(float val) returns int = @java:Method {
    name: "round",
    'class: "java.lang.Math",
    paramTypes: ["double"]
} external;
