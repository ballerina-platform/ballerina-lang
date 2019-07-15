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

# Sum of all the arguments
# 0 if no args
public function sum(decimal... xs) returns decimal = external;

# Maximum of all the arguments
public function max(decimal x, decimal... xs) returns decimal = external;

# Minimum of all the arguments
public function min(decimal x, decimal... xs) returns decimal = external;

# IEEE abs operation
public function abs(decimal x) returns decimal = external;

# Floating point value that is a mathematical integer and closest to `x`.
# If there are two such integers, choose the one that is even
# (this is the round-to-nearest rounding mode, which is the default for IEEE
# and for Ballerina).
# Same as Java Math.round method
# Same as .NET Math.Round method
# IEEE 754 roundToIntegralTiesToEven operation
# Note that `<int>x` is the same as `<int>x.round()`
public function round(decimal x) returns decimal = external;

# Largest (closest to +∞) floating point value not greater than `x` that is a mathematical integer
public function floor(decimal x) returns decimal = external;

# Smallest (closest to -∞) floating point value not less than `x` that is a mathematical integer
public function ceiling(decimal x) returns decimal = external;

# Return the decimal value represented by `s`.
# `s` must follow the syntax of DecimalFloatingPointNumber as defined by the Ballerina specification
# with the following modifications
# - the DecimalFloatingPointLiteral may have a leading `+` or `-` sign
# - a FloatingPointTypeSuffix is not allowed
# This is the inverse of `value:toString` applied to an `decimal`.
public function fromString(string s) returns decimal|error = external;
