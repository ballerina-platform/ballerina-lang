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

import ballerina/jballerina.java;

# Returns the sum of zero or more decimal values.
#
# + xs - decimal values to sum
# + return - sum of all the parameter `xs`; 0 if parameter `xs` is empty
public isolated function sum(decimal... xs) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Sum",
    name: "sum"
} external;

# Returns the maximum of one or more decimal values.
#
# + x - first decimal value
# + xs - other decimal values
# + return - maximum value of parameter `x` and all the parameter `xs`
public isolated function max(decimal x, decimal... xs) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Max",
    name: "max"
} external;

# Returns the minimum of one or more decimal values.
#
# + x - first decimal value
# + xs - other decimal values
# + return - minimum value of parameter `x` and all the parameter `xs`.
public isolated function min(decimal x, decimal... xs) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Min",
    name: "min"
} external;

# Returns the IEEE absolute value of a decimal value.
#
# + x - decimal value to operate on
# + return - absolute value of parameter `x`
public isolated function abs(decimal x) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Abs",
    name: "abs"
} external;

# Round a decimal to a specified number of digits.
# Returns the decimal value that has an exponent of `-fractionDigits`
# and is closest to `x`.
# If there are two such values, returns the one whose final digit is even
# (this is the round-to-nearest rounding mode, which is the default for IEEE
# and for Ballerina).
# A value of `fractionDigits` greater than 0 thus corresponds to the number of digits after the decimal
# point being `fractionDigits`; a value of 0 for `fractionDigits` rounds to an integer.
# Note that IEEE 754 roundToIntegralTiesToEven operation differs from `round` with `fractionDigits` as 0, in that
# roundToIntegralTiesToEven will return a value with a positive exponent when the operand has a positive exponent.
# Note that `<int>x` is the same as `<int>x.round(0)`.
#
# + x - decimal value to operate on
# + fractionDigits - the number of digits after the decimal point
# + return - closest decimal value to `x` that is an integral multiple of 10 raised to the power of `-fractionDigits`
public isolated function round(decimal x, int fractionDigits = 0) returns decimal =  @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Round",
    name: "round"
} external;

# IEEE quantize operation.

# + x - decimal value to operate on
# + y - decimal value from which to get the quantum
# + return - `x` with the quantum of `y`
public isolated function quantize(decimal x, decimal y) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Quantize",
    name: "quantize"
} external;

# Rounds a decimal down to the closest integral value.
#
# + x - decimal value to operate on
# + return - largest (closest to +∞) decimal value not greater than parameter `x` that is a mathematical integer.
public isolated function floor(decimal x) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Floor",
    name: "floor"
} external;

# Rounds a decimal up to the closest integral value.
#
# + x - decimal value to operate on
# + return - smallest (closest to -∞) decimal value not less than parameter `x` that is a mathematical integer
public isolated function ceiling(decimal x) returns decimal = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.Ceiling",
    name: "ceiling"
} external;

# Returns the decimal value represented by a string.
#
# `s` must follow the syntax of DecimalFloatingPointNumber as defined by the Ballerina specification
# with the following modifications
# - the DecimalFloatingPointLiteral may have a leading `+` or `-` sign
# - a FloatingPointTypeSuffix is not allowed
# This is the inverse of function ``value:toString`` applied to an `decimal`.
#
# + s - string representation of a decimal
# + return - decimal representation of the argument or error
public isolated function fromString(string s) returns decimal|error = @java:Method {
    'class: "org.ballerinalang.langlib.decimal.FromString",
    name: "fromString"
} external;
