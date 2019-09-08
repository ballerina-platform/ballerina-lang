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

# The number π
public const float PI = 3.141592653589793;
# Euler's number
public const float E =  2.718281828459045;
# IEEE not-a-number value
public const float NaN = 0.0/0.0;

# IEEE positive infinity
public const float Infinity = 1.0/0.0;

# Tests whether a float is finite.
# Exactly one of isFinite, isInfinite and IsNaN will be true for any float value
#
# + x - the float to be tested
# + return - true if `x` is finite, i.e. neither NaN nor +∞ nor -∞
public function isFinite(float x) returns boolean = external;

# Tests whether a float is infinite.
# Exactly one of isFinite, isInfinite and IsNaN will be true for any float value
#
# + x - the float to be tested
# + return - true if `x` is either +∞ or -∞
public function isInfinite(float x) returns boolean = external;

# Tests whether a float is NaN.
# Exactly one of isFinite, isInfinite and IsNaN will be true for any float value.
#
# + x - the float to be tested
# + return - true if `x` is NaN
public function isNaN(float x) returns boolean = external;

# Sum of zero or more float values.
# Result is NaN if any arg is NaN
#
# + xs - float values to sum
# + return - sum of all the `xs`, +0.0 if `xs` is empty
public function sum(float... xs) returns float = external;

# Maximum of zero or more float values.
# Result is -∞ if no args
# NaN if any arg is NaN
#
# + xs - float values to operate on
# + return - maximum value of all the `xs`
public function max(float... xs) returns float = external;

# Minimum of zero or more float values.
# Result is +∞ if no args
# Result is NaN if any arg is NaN
#
# + xs - float values to operate on
# + return - minimum value of all the `xs`
public function min(float... xs) returns float = external;

# IEEE abs operation.
#
# + x - float value to operate on
# + return - absolute value of `x`
public function abs(float x) returns float = external;

# Round a float value to the closest integral value.
# Returns the float value that is a mathematical integer and closest to `x`.
# If there are two such values, choose the one that is even
# (this is the round-to-nearest rounding mode, which is the default for IEEE and for Ballerina).
# Same as Java Math.rint method
# Same as .NET Math.Round method
# IEEE roundToIntegralTiesToEven operation
# Note that `<int>x` is the same as `<int>x.round()`
#
# + x - float value to operate on
# + return - closest float value to `x` that is a mathematical integer
public function round(float x) returns float = external;

# Rounds a float down to the closest integral value.
#
# + x - float value to operate on
# + return - largest (closest to +∞) float value not greater than `x` that is a mathematical integer.
public function floor(float x) returns float = external;

# Rounds a float up to the closest integral value.
#
# + x - float value to operate on
# + return - smallest (closest to -∞) decimal value not less than `x` that is a mathematical integer
public function ceiling(float x) returns float = external;

# Returns the square root of a float value.
# Corresponds to IEEE squareRoot operation.
#
# + x - float value to operate on
# + return - square root of `x`
public function sqrt(float x) returns float = external;

# Returns the cube root of a float value.
# Corresponds to IEEE rootn(x, 3) operation.
#
# + x - float value to operate on
# + return - cube root of `x`
public function cbrt(float x) returns float = external;

# Raises one float value to the power of another float values.
# Corresponds to IEEE pow(x, y) operation.
#
# + x - base value
# + y - the exponent
# + return - `x` raised to the power of `y`
public function pow(float x, float y) returns float = external;

# Returns the natural logarithm of a float value
# Corresponds to IEEE log operation.
#
# + x - float value to operate on
# + return - natural logarithm of `x`
public function log(float x) returns float = external;

# Returns the base 10 logarithm of a float value.
# Corresponds to IEEE log10 operation.
#
# + x - float value to operate on
# + return - base 10 logarithm of `x`
public function log10(float x) returns float = external;

# Raises Euler's number to a power.
# Corresponds to IEEE exp operation.
#
# + x - float value to operate on
# + return - Euler's number raised to the power `x`
public function exp(float x) returns float = external;

# Returns the sine of a float value.
# Corresponds to IEEE sin operation.
#
# + x - float value, specifying an angle in radians
# + return - the sine of `x`
public function sin(float x) returns float = external;

# Returns the cosine of a float value.
# Corresponds to IEEE cos operation.
#
# + x - float value, specifying an angle in radians
# + return - the cosine of `x`
public function cos(float x) returns float = external;

# Returns the tangent of a float value.
# Corresponds to IEEE tan operation
#
# + x - float value, specifying an angle in radians
# + return - the tangent of `x`
public function tan(float x) returns float = external;

# Returns the arccosine of a float value.
# Corresponds to IEEE acos operation
#
# + x - float value to operate on
# + return - the arccosine of `x` in radians
public function acos(float x) returns float = external;

# Returns the arctangent of a float value.
# Corresponds to IEEE atan operation.
#
# + x - float value to operate on
# + return - the arctangent of `x` in radians
public function atan(float x) returns float = external;

# Returns the arcsine of a float value.
# Corresponds to IEEE asin operation.
#
# + x - float value to operate on
# + return - the arcsine of `x` in radians
public function asin(float x) returns float = external;

# Performs the 2-argument arctangent operation.
# Corresponds IEEE atan2(y, x) operation.
#
# + y - the y-coordinate
# + x - the x-coordinate
# + return - the angle in radians from the positive x-axis to the point
#   whose Cartesian coordinates are `(x, y)`
public function atan2(float y, float x) returns float = external;

# Returns the hyperbolic sine of a float value.
# Corresponds to IEEE sinh operation.
#
# + x - float value to operate on
# + return - hyperbolic sine of `x`
public function sinh(float x) returns float = external;

# Returns the hyperbolic cosine of a float value.
# Corresponds to IEEE cosh operation.
#
# + x - float value to operate on
# + return - hyperbolic cosine of `x`
public function cosh(float x) returns float = external;

# Returns the hyperbolic tangent of a float value.
# Corresponds to IEEE tanh operation.
#
# + x - float value to operate on
# + return - hyperbolic tangent of `x`
public function tanh(float x) returns float = external;

# Return the float value represented by `s`.
# `s` must follow the syntax of DecimalFloatingPointNumber as defined by the Ballerina specification
# with the following modifications
# - the DecimalFloatingPointNumber may have a leading `+` or `-` sign
# - `NaN` is allowed
# - `Infinity` is allowed with an optional leading `+` or `-` sign
# - a FloatingPointTypeSuffix is not allowed
# This is the inverse of `value:toString` applied to an `float`.
#
# + s - string representation of a float
# + return - float value or error
public function fromString(string s) returns float|error = external;

# Returns a string that represents `x` as a hexadecimal floating point number.
# The returned string will comply to the grammar of HexFloatingPointLiteral
# in the Ballerina spec with the following modifications:
# - it will have a leading `-` sign if negative
# - positive infinity will be represented by `Infinity`
# - negative infinity will be represented by `-Infinity`
# - NaN will be represented by `NaN`
# The representation includes `0x` for finite numbers.
#
# + x - float value
# + return - hexadecimal floating point hex string representation
public function toHexString(float x) returns string = external;

# Return the float value represented by `s`.
# `s` must follow the syntax of HexFloatingPointLiteral as defined by the Ballerina specification
# with the following modifications
# - the HexFloatingPointLiteral may have a leading `+` or `-` sign
# - `NaN` is allowed
# - `Infinity` is allowed with an optional leading `+` or `-` sign
#
# + s - hexadecimal floating point hex string representation
# + return - float value or error
public function fromHexString(string s) returns float|error = external;

# Returns IEEE 64-bit binary floating point format representation of `x` as an int.
#
# + x - float value
# + return - `x` bit pattern as an int
public function toBitsInt(float x) returns int = external;

# Returns the float that is represented in IEEE 64-bit floating point by `x`.
# All bit patterns that IEEE defines to be NaNs will all be mapped to the single float NaN value.
#
# + x - int value
# + return - `x` bit pattern as a float
public function fromBitsInt(int x) returns float = external;
