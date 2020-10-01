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

import ballerina/java;

# Sum of zero or more decimal values.
#
# + xs - decimal values to sum
# + return - sum of all the `xs`; 0 if `xs` is empty
public isolated function sum(decimal... xs) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Sum",
    name: "sum"
} external;

# Maximum of one or more decimal values.
#
# + x - first decimal value
# + xs - other decimal values
# + return - maximum value of `x` and all the `xs`
public isolated function max(decimal x, decimal... xs) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Max",
    name: "max"
} external;

# Minimum of one or more decimal values
#
# + x - first decimal value
# + xs - other decimal values
# + return - minimum value of `x` and all the `xs`.
public isolated function min(decimal x, decimal... xs) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Min",
    name: "min"
} external;

# IEEE abs operation.
#
# + x - decimal value to operate on
# + return - absolute value of `x`
public isolated function abs(decimal x) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Abs",
    name: "abs"
} external;

# Round a decimal to the closest integral value.
# Returns the decimal value that is a mathematical integer and closest to `x`.
# If there are two such values, choose the one that is even
# (this is the round-to-nearest rounding mode, which is the default for IEEE
# and for Ballerina).
# Same as Java Math.rint method
# Same as .NET Math.Round method
# IEEE 754 roundToIntegralTiesToEven operation
# Note that `<int>x` is the same as `<int>x.round()`
#
# + x - decimal value to operate on
# + return - closest decimal value to `x` that is a mathematical integer
public isolated function round(decimal x) returns decimal =  @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Round",
    name: "round"
} external;

# Rounds a decimal down to the closest integral value.
#
# + x - decimal value to operate on
# + return - largest (closest to +∞) decimal value not greater than `x` that is a mathematical integer.
public isolated function floor(decimal x) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Floor",
    name: "floor"
} external;

# Rounds a decimal up to the closest integral value.
#
# + x - decimal value to operate on
# + return - smallest (closest to -∞) decimal value not less than `x` that is a mathematical integer
public isolated function ceiling(decimal x) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Ceiling",
    name: "ceiling"
} external;

# Return the decimal value represented by `s`.
# `s` must follow the syntax of DecimalFloatingPointNumber as defined by the Ballerina specification
# with the following modifications
# - the DecimalFloatingPointLiteral may have a leading `+` or `-` sign
# - a FloatingPointTypeSuffix is not allowed
# This is the inverse of `value:toString` applied to an `decimal`.
#
# + s - string representation of a decimal
# + return - decimal representation of the argument or error
public isolated function fromString(string s) returns decimal|error = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.FromString",
    name: "fromString"
} external;
