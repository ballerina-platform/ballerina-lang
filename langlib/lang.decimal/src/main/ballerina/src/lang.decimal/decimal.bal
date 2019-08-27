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

# Sum of all the arguments.
# 0 if no args.
#
# + xs - decimal numbers to sum
# + return - sum of the arguments
public function sum(decimal... xs) returns decimal = external;

# Maximum of all the arguments.
#
# + x - first paramter to check for max value
# + xs - rest of the parameter to check for max value
# + return - maximum value of all provided values
public function max(decimal x, decimal... xs) returns decimal = external;

# Minimum of all the arguments.
#
# + x - first paramter to check for min value
# + xs - rest of the parameter to check for min value
# + return - minimum value of all provided values
public function min(decimal x, decimal... xs) returns decimal = external;

# IEEE abs operation.
#
# + x - whose absolute value is to be determined
# + return - absolute value of the argument
public function abs(decimal x) returns decimal = external;

# Floating point value that is a mathematical integer and closest to `x`.
# If there are two such integers, choose the one that is even
# (this is the round-to-nearest rounding mode, which is the default for IEEE
# and for Ballerina).
# Same as Java Math.rint method
# Same as .NET Math.Round method
# IEEE 754 roundToIntegralTiesToEven operation
# Note that `<int>x` is the same as `<int>x.round()`
#
# + x - whose value to be rounded
# + return - rounded value
public function round(decimal x) returns decimal = external;

# Largest (closest to +∞) floating point value not greater than `x` that is a mathematical integer.
#
# + x - whose value to be floored
# + return - floored value
public function floor(decimal x) returns decimal = external;

# Smallest (closest to -∞) floating point value not less than `x` that is a mathematical integer
#
# + x - value to performe ceiling on
# + return - integer ceiling value of the argument in decimal
public function ceiling(decimal x) returns decimal = external;

# Return the decimal value represented by `s`.
# `s` must follow the syntax of DecimalFloatingPointNumber as defined by the Ballerina specification
# with the following modifications
# - the DecimalFloatingPointLiteral may have a leading `+` or `-` sign
# - a FloatingPointTypeSuffix is not allowed
# This is the inverse of `value:toString` applied to an `decimal`.
#
# + s - string representation of a decimal
# + return - decimal representation of the argument or error
public function fromString(string s) returns decimal|error = external;
