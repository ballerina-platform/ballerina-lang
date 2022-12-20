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

// IEEE refers to IEEE 754
// Constants

# The number π.
public const float PI = 3.141592653589793;
# Euler's number.
public const float E =  2.718281828459045;
# IEEE not-a-number value.
public const float NaN = 0.0/0.0;

# IEEE positive infinity.
public const float Infinity = 1.0/0.0;

# Tests whether a float is finite.
#
# Exactly one of isFinite, isInfinite and IsNaN will be true for any float value
#
# ```ballerina
# float f = 1.2;
# f.isFinite() ⇒ true
# 
# float:Infinity.isFinite() ⇒ false
# ```
#
# + x - the float to be tested
# + return - true if parameter `x` is finite, i.e., neither NaN nor +∞ nor -∞
public isolated function isFinite(float x) returns boolean = @java:Method {
     'class: "org.ballerinalang.langlib.floatingpoint.IsFinite",
     name: "isFinite"
} external;

# Tests whether a float is infinite.
#
# Exactly one of isFinite, isInfinite and IsNaN will be true for any float value
#
# ```ballerina
# float f = 3.21;
# f.isInfinite() ⇒ false
# 
# float:Infinity.isInfinite() ⇒ true
# ```
#
# + x - the float to be tested
# + return - true if parameter `x` is either +∞ or -∞
public isolated function isInfinite(float x) returns boolean = @java:Method {
   'class: "org.ballerinalang.langlib.floatingpoint.IsInfinite",
   name: "isInfinite"
} external;

# Tests whether a float is NaN.
#
# Exactly one of isFinite, isInfinite and IsNaN will be true for any float value.
#
# ```ballerina
# float f = 0.23;
# f.isNaN() ⇒ false
# 
# float:NaN.isNaN() ⇒ true
# ```
#
# + x - the float to be tested
# + return - true if parameter `x` is NaN
public isolated function isNaN(float x) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.IsNaN",
    name: "isNaN"
} external;

# Returns the sum of zero or more float values.
#
# Result is NaN if any arg is NaN
#
# ```ballerina
# float:sum(1.2, 2.3, 3.4) ⇒ 6.9
#
# float[] scores = [11.1, 22.2, 33.3];
# float:sum(...scores) ⇒ 66.6
#
# float f = 21.2;
# f.sum(10.5, 21, 32.4) ⇒ 85.1
# 
# float:sum(float:NaN, 2.3, 3.4) ⇒ NaN
# ```
#
# + xs - float values to sum
# + return - sum of all of parameter `xs`, +0.0 if parameter `xs` is empty
public isolated function sum(float... xs) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Sum",
    name: "sum"
} external;

# Returns the maximum of zero or more float values.
#
# Result is -∞ if no args
# NaN if any arg is NaN
#
# ```ballerina
# float:max(1.2, 12.3, 3.4) ⇒ 12.3
#
# float[] marks = [70.3, 80.5, 98.1, 92.3];
# float:max(...marks) ⇒ 98.1
#
# float f = 21.2;
# f.max(40.5, 21, 32.4) ⇒ 40.5
#
# float:max() ⇒ -Infinity
# 
# float:max(1.2, float:NaN, 3.4) ⇒ NaN
# ```
#
# + xs - float values to operate on
# + return - maximum value of all of parameter `xs`
public isolated function max(float... xs) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Max",
    name: "max"
} external;

# Returns the minimum of zero or more float values.
#
# Result is +∞ if no args
# Result is NaN if any arg is NaN
#
# ```ballerina
# float:min(5.2, 2.3, 3.4) ⇒ 2.3
#
# float[] marks = [90.3, 80.5, 98, 92.3];
# float:min(...marks) ⇒ 80.5
#
# float f = 1.2;
# f.min(10.5, 21, 32.4) ⇒ 1.2
#
# float:min() ⇒ Infinity
# 
# float:min(5.2, float:NaN, 3.4) ⇒ NaN
# ```
#
# + xs - float values to operate on
# + return - minimum value of all of parameter `xs`
public isolated function min(float... xs) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Min",
    name: "min"
} external;

# Returns the IEEE absolute value of a float value.
#
# ```ballerina
# float f = -3.21;
# f.abs() ⇒ 3.21
# ```
#
# + x - float value to operate on
# + return - absolute value of parameter `x`
public isolated function abs(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Abs",
    name: "abs"
} external;

# Rounds a float value to a specified number of digits.
#
# Returns the float value that is an integral multiple of 10 raised to the power of `-fractionDigits` and closest to `x`.
# If there are two such values, choose the one whose final digit is even
# (this is the round-to-nearest rounding mode, which is the default for IEEE and for Ballerina).
# A value of `fractionDigits` greater than 0 thus corresponds to the number of digits after the decimal
# point being `fractionDigits`; a value of 0 for `fractionDigits` rounds to an integer.
# If `x` is NaN, +0, -0, +∞ or -∞, then the result is `x`.
# When `fractionDigits` is 0, this is
# the same as Java Math.rint method, .NET Math.Round method and
# IEEE roundToIntegralTiesToEven operation
# Note that `<int>x` is the same as `<int>x.round(0)`.
#
# ```ballerina
# float f = 3.55;
# f.round() ⇒ 4.0
# 
# float g = 4.55555;
# g.round(3) ⇒ 4.556
# 
# float h = 2.5;
# h.round(0) ⇒ 2.0
# 
# float i = 3.5;
# i.round(0) ⇒ 4.0
# ```
#
# + x - float value to operate on
# + fractionDigits - the number of digits after the decimal point
# + return - float value closest to `x` that is an integral multiple of 10 raised to the power of `-fractionDigits`
public isolated function round(float x, int fractionDigits = 0) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Round",
    name: "round"
} external;

# Rounds a float down to the closest integral value.
#
# ```ballerina
# float f = 3.51;
# f.floor() ⇒ 3.0
# ```
#
# + x - float value to operate on
# + return - largest (closest to +∞) float value not greater than parameter `x` that is a mathematical integer
public isolated function floor(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Floor",
    name: "floor"
} external;

# Rounds a float up to the closest integral value.
#
# ```ballerina
# float f = 3.51;
# f.ceiling() ⇒ 4.0
# ```
#
# + x - float value to operate on
# + return - smallest (closest to -∞) decimal value not less than parameter `x` that is a mathematical integer
public isolated function ceiling(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Ceiling",
    name: "ceiling"
} external;

# Returns the square root of a float value.
#
# Corresponds to IEEE squareRoot operation.
#
# ```ballerina
# float f = 1.96;
# f.sqrt() ⇒ 1.4
# ```
#
# + x - float value to operate on
# + return - square root of parameter `x`
public isolated function sqrt(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Sqrt",
    name: "sqrt"
} external;

# Returns the cube root of a float value.
#
# Corresponds to IEEE rootn(x, 3) operation.
#
# ```ballerina
# float f = 0.125;
# f.cbrt() ⇒ 0.5
# ```
#
# + x - float value to operate on
# + return - cube root of parameter `x`
public isolated function cbrt(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Cbrt",
    name: "cbrt"
} external;

# Raises one float value to the power of another float values.
#
# Corresponds to IEEE pow(x, y) operation.
#
# ```ballerina
# float f = 2.1;
# f.pow(2) ⇒ 4.41
# ```
#
# + x - base value
# + y - the exponent
# + return - `x` raised to the power of parameter `y`
public isolated function pow(float x, float y) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Pow",
    name: "pow"
} external;

# Returns the natural logarithm of a float value.
#
# Corresponds to IEEE log operation.
#
# ```ballerina
# float f = 234.56;
# f.log() ⇒ 5.4577114186982865
# ```
#
# + x - float value to operate on
# + return - natural logarithm of parameter `x`
public isolated function log(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Log",
    name: "log"
} external;

# Returns the base 10 logarithm of a float value.
#
# Corresponds to IEEE log10 operation.
#
# ```ballerina
# float f = 0.1;
# f.log10() ⇒ -1.0
# ```
#
# + x - float value to operate on
# + return - base 10 logarithm of parameter `x`
public isolated function log10(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Log10",
    name: "log10"
} external;

# Raises Euler's number to a power.
#
# Corresponds to IEEE exp operation.
#
# ```ballerina
# float f = 2.3;
# f.exp() ⇒ 9.974182454814718
# ```
#
# + x - float value to operate on
# + return - Euler's number raised to the power parameter `x`
public isolated function exp(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Exp",
    name: "exp"
} external;

# Returns the sine of a float value.
#
# Corresponds to IEEE sin operation.
#
# ```ballerina
# float f = 2.3;
# f.sin() ⇒ 0.7457052121767203
# ```
#
# + x - float value, specifying an angle in radians
# + return - the sine of parameter `x`
public isolated function sin(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Sin",
    name: "sin"
} external;

# Returns the cosine of a float value.
#
# Corresponds to IEEE cos operation.
#
# ```ballerina
# float f = 0.7;
# f.cos() ⇒ 0.7648421872844885
# ```
#
# + x - float value, specifying an angle in radians
# + return - the cosine of parameter `x`
public isolated function cos(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Cos",
    name: "cos"
} external;

# Returns the tangent of a float value.
#
# Corresponds to IEEE tan operation
#
# ```ballerina
# float f = 0.2;
# f.tan() ⇒ 0.2027100355086725
# ```
#
# + x - float value, specifying an angle in radians
# + return - the tangent of parameter `x`
public isolated function tan(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Tan",
    name: "tan"
} external;

# Returns the arccosine of a float value.
#
# Corresponds to IEEE acos operation.
#
# ```ballerina
# float f = 0.5;
# f.acos() ⇒ 1.0471975511965979
# ```
#
# + x - float value to operate on
# + return - the arccosine of parameter `x` in radians
public isolated function acos(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Acos",
    name: "acos"
} external;

# Returns the arctangent of a float value.
#
# ```ballerina
# float f = 243.25;
# f.atan() ⇒ 1.5666853530369307
# ```
#
# Corresponds to IEEE atan operation.
#
# + x - float value to operate on
# + return - the arctangent of parameter `x` in radians
public isolated function atan(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Atan",
    name: "atan"
} external;

# Returns the arcsine of a float value.
#
# Corresponds to IEEE asin operation.
#
# ```ballerina
# float f = 0.5;
# f.asin() ⇒ 0.5235987755982989
# ```
#
# + x - float value to operate on
# + return - the arcsine of parameter `x` in radians
public isolated function asin(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Asin",
    name: "asin"
} external;

# Performs the 2-argument arctangent operation.
#
# Corresponds IEEE atan2(y, x) operation.
#
# ```ballerina
# float:atan2(24.21, 12.345) ⇒ 1.0992495979622232
# ```
#
# + y - the y-coordinate
# + x - the x-coordinate
# + return - the angle in radians from the positive x-axis to the point
#   whose Cartesian coordinates are `(x, y)`
public isolated function atan2(float y, float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Atan2",
    name: "atan2"
} external;

# Returns the hyperbolic sine of a float value.
#
# Corresponds to IEEE sinh operation.
#
# ```ballerina
# float f = 0.71;
# f.sinh() ⇒ 0.7711735305928927
# ```
#
# + x - float value to operate on
# + return - hyperbolic sine of parameter `x`
public isolated function sinh(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Sinh",
    name: "sinh"
} external;

# Returns the hyperbolic cosine of a float value.
#
# Corresponds to IEEE cosh operation.
#
# ```ballerina
# float f = 0.52;
# f.cosh() ⇒ 1.1382740988345403
# ```
#
# + x - float value to operate on
# + return - hyperbolic cosine of parameter `x`
public isolated function cosh(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Cosh",
    name: "cosh"
} external;

# Returns the hyperbolic tangent of a float value.
#
# Corresponds to IEEE tanh operation.
#
# ```ballerina
# float f = 0.9;
# f.tanh() ⇒ 0.7162978701990245
# ```
#
# + x - float value to operate on
# + return - hyperbolic tangent of parameter `x`
public isolated function tanh(float x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.Tanh",
    name: "tanh"
} external;

# Returns the float value represented by a string.
#
# parameter `s` must follow the syntax of DecimalFloatingPointNumber as defined by the Ballerina specification
# with the following modifications
# - the DecimalFloatingPointNumber may have a leading `+` or `-` sign
# - `NaN` is allowed
# - `Infinity` is allowed with an optional leading `+` or `-` sign
# - a FloatingPointTypeSuffix is not allowed
# This is the inverse of function ``value:toString`` applied to an `float`.
#
# ```ballerina
# float:fromString("0.2453") ⇒ 0.2453
# 
# float:fromString("-10") ⇒ -10.0
# 
# float:fromString("123f") ⇒ error
# ```
#
# + s - string representation of a float
# + return - float value or error
public isolated function fromString(string s) returns float|error = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.FromString",
    name: "fromString"
} external;

# Returns a string that represents a float value as a hexadecimal floating point number.
#
# The returned string will comply to the grammar of HexFloatingPointLiteral
# in the Ballerina spec with the following modifications:
# - it will have a leading `-` sign if negative
# - positive infinity will be represented by `Infinity`
# - negative infinity will be represented by `-Infinity`
# - NaN will be represented by `NaN`
# The representation includes `0x` for finite numbers.
#
# ```ballerina
# float f = -10.2453;
# f.toHexString() ⇒ -0x1.47d97f62b6ae8p3
# ```
#
# + x - float value
# + return - hexadecimal floating point hex string representation
public isolated function toHexString(float x) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.ToHexString",
    name: "toHexString"
} external;

# Return the float value represented by a string.
#
# parameter `s` must follow the syntax of HexFloatingPointLiteral as defined by the Ballerina specification
# with the following modifications
# - the HexFloatingPointLiteral may have a leading `+` or `-` sign
# - `NaN` is allowed
# - `Infinity` is allowed with an optional leading `+` or `-` sign
#
# ```ballerina
# float:fromHexString("0x1.0a3d70a3d70a4p4") ⇒ 16.64
# 
# float:fromHexString("0x1J") ⇒ error
# ```
#
# + s - hexadecimal floating point hex string representation
# + return - float value or error
public isolated function fromHexString(string s) returns float|error = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.FromHexString",
    name: "fromHexString"
} external;

# Returns IEEE 64-bit binary floating point format representation of a float value as an int.
#
# ```ballerina
# float f = 4.16;
# f.toBitsInt() ⇒ 4616369762039853220
# ```
#
# + x - float value
# + return - parameter `x` bit pattern as an int
public isolated function toBitsInt(float x) returns int = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.ToBitsInt",
    name: "toBitsInt"
} external;

# Returns the float that is represented in IEEE 64-bit floating point by an int.
#
# All bit patterns that IEEE defines to be NaNs will all be mapped to the single float NaN value.
#
# ```ballerina
# float:fromBitsInt(4) ⇒ 2.0E-323
# ```
#
# + x - int value
# + return - parameter `x` bit pattern as a float
public isolated function fromBitsInt(int x) returns float = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.FromBitsInt",
    name: "fromBitsInt"
} external;

# Returns a string that represents `x` using fixed-point notation.
# The returned string will be in the same format used by `value:toString`,
# except that it will not include an exponent.
# If `x` is NaN or infinite, the result will be the same as `value:toString`.
# This will panic if `fractionDigits` is less than 0.
# If `fractionDigits` is zero, there will be no decimal point.
# Any necessary rounding will use the roundTiesToEven rounding direction.
#
# ```ballerina
# float f = 12.456;
# f.toFixedString(2) ⇒ 12.46
# 
# float g = 12.456;
# g.toFixedString(0) ⇒ 12
# 
# float h = 12.456;
# h.toFixedString(-3) ⇒ panic
# ```
#
# + x - float value
# + fractionDigits - number of digits following the decimal point; `()` means to use
#    the minimum number of digits required to accurately represent the value
# + return - string representation of `x` in fixed-point notation
public isolated function toFixedString(float x, int? fractionDigits) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.ToFixedString",
    name: "toFixedString"
} external;

# Returns a string that represents `x` using scientific notation.
# The returned string will be in the same format used by `value:toString`,
# except that it will always include an exponent and there will be exactly
# one digit before the decimal point.
# But if `x` is NaN or infinite, the result will be the same as `value:toString`.
# The digit before the decimal point will be zero only if all other digits
# are zero.
# This will panic if `fractionDigits` is less than 0.
# If `fractionDigits` is zero, there will be no decimal point.
# Any necessary rounding will use the roundTiesToEven rounding direction.
# The exponent in the result uses lower-case `e`, followed by a `+` or `-` sign,
# followed by at least two digits, and only as many more digits as are needed
# to represent the result. If `x` is zero, the exponent is zero. A zero exponent
# is represented with a `+` sign.
#
# ```ballerina
# float f = 12.456;
# f.toExpString(2) ⇒ 1.25e+1
# 
# float g = 12.456;
# g.toExpString(()) ⇒ 1.2456e+1
# 
# float h = 12.456;
# h.toExpString(-2) ⇒ panic
# ```
#
# + x - float value
# + fractionDigits - number of digits following the decimal point; `()` means to use
#    the minimum number of digits required to accurately represent the value
# + return - string representation of `x` in scientific notation
public isolated function toExpString(float x, int? fractionDigits) returns string = @java:Method {
    'class: "org.ballerinalang.langlib.floatingpoint.ToExpString",
    name: "toExpString"
} external;
