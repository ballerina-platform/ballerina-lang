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

// IEEE refers to IEEE 754
// Constants

public const float PI = 3.141592653589793;
public const float E =  2.718281828459045;
// todo: fix this
//public const float NaN = 0.0/0.0;

// XXX Infinity or INFINITY or POSITIVE_INFINITY (and NEGATIVE_INFINITY);
// todo: fix this
//public const float Infinity = 1.0/0.0;


# Tests whether a float is finite.
# x - the float to be tested
# return - true if `x` is finite, i.e. neither NaN nor +∞ nor -∞
# Exactly one of isFinite, isInfinite and IsNaN will be true for any float value
public function isFinite(float x) returns boolean = external;

# Tests whether a float is infinite.
# x - the float to be tested
# return - true if `x` is either +∞ nor -∞
# Exactly one of isFinite, isInfinite and IsNaN will be true for any float value
public function isInfinite(float x) returns boolean = external;

# Tests whether a float is NaN.
# x - the float to be tested
# return - true if `x` is NaN
# Exactly one of isFinite, isInfinite and IsNaN will be true for any float value
public function isNaN(float x) returns boolean = external;

# Sum of all the arguments. +0.0 if no args
# NaN if any arg is NaN
public function sum(float... xs) returns float = external;

# Maximum of all the arguments
# -∞ if no args
# NaN if any arg is NaN
public function max(float... xs) returns float = external;

# Minimum of all the arguments. +∞ if no args
# NaN if any arg is NaN
public function min(float... xs) returns float = external;

# IEEE abs operation
public function abs(float x) returns float = external;

# Floating point value that is a mathematical integer and closest to `x`.
# If there are two such integers, choose the one that is even
# (this is the round-to-nearest rounding mode, which is the default for IEEE
# and for Ballerina).
# Same as Java Math.rint method
# Same as .NET Math.round method
# IEEE roundToIntegralTiesToEven operation
# XXX different from Java round which returns a long, and rounds ties to +∞
# Note that `<int>x` is the same as `<int>x.round()`
public function round(float x) returns float = external;

# Largest (closest to +∞) floating point value not greater than `x` that is a mathematical integer
public function floor(float x) returns float = external;

# Smallest (closest to -∞) floating point value not less than `x` that is a mathematical integer
public function ceiling(float x) returns float = external;

# IEEE squareRoot operation
public function sqrt(float x) returns float = external;

# Cube root
# IEEE rootn(x, 3)
public function cbrt(float x) returns float = external;

# `x` to the power of `y`
# IEEE pow(x, y)
public function pow(float x, float y) returns float = external;

# Natural logarithm
# IEEE log operation
public function log(float x) returns float = external;

# Base 10 log
# IEEE log10 operation
public function log10(float x) returns float = external;

# IEEE exp operation
public function exp(float x) returns float = external;

# IEEE sin operation
public function sin(float x) returns float = external;

# IEEE cos operation
public function cos(float x) returns float = external;

# IEEE tan operation
public function tan(float x) returns float = external;

# IEEE acos operation
public function acos(float x) returns float = external;

# IEEE atan operation
public function atan(float x) returns float = external;

# IEEE asin operation
public function asin(float x) returns float = external;

# IEEE atan2(y, x) operation
public function atan2(float y, float x) returns float = external;

# IEEE sinh operation
public function sinh(float x) returns float = external;

# IEEE cosh operation
public function cosh(float x) returns float = external;

# IEEE tanh operation
public function tanh(float x) returns float = external;

# Return the float value represented by `s`.
# `s` must follow the syntax of DecimalFloatingPointNumber as defined by the Ballerina specification
# with the following modifications
# - the DecimalFloatingPointNumber may have a leading `+` or `-` sign
# - `NaN` is allowed
# - `Infinity` is allowed with an optional leading `+` or `-` sign
# - a FloatingPointTypeSuffix is not allowed
# This is the inverse of `value:toString` applied to an `float`.
public function fromString(string s) returns float|error = external;

# Returns a string that represents `x` as a hexadecimal floating point number.
# The returned string will comply to the grammar of HexFloatingPointLiteral
# in the Ballerina spec with the following modifications:
# - it will have a leading `-` sign if negative
# - positive infinity will be represented by `Infinity`
# - negative infinity will be represented by `-Infinity`
# - NaN will be represented by `NaN`
# The representation includes `0x` for finite numbers.
public function toHexString(float x) returns string = external;

# Return the float value represented by `s`.
# `s` must follow the syntax of HexFloatingPointLiteral as defined by the Ballerina specification
# with the following modifications
# - the HexFloatingPointLiteral may have a leading `+` or `-` sign
# - `NaN` is allowed
# - `Infinity` is allowed with an optional leading `+` or `-` sign
public function fromHexString(string s) returns float|error = external;

# Returns IEEE 64-bit binary floating point format representation of `x` as an int.
public function toBitsInt(float x) returns int = external;
# Returns the float that is represented in IEEE 64-bit floating point by `x`.
# All bit patterns that IEEE defines to be NaNs will all be mapped to the single float NaN value.
public function fromBitsInt(int x) returns float = external;
