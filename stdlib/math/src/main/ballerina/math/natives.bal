// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


# The ratio of the circumference of a circle to its diameter.
@final public float PI = 3.141592653589793;

# The base of the natural logarithms.
@final public float E = 2.718281828459045;

# Returns Euler's number, that is 'e' raised to the power of exponent.
#
# + val - Exponent value to raise
# + return - Exp value
public extern function exp(float val) returns (float);

# Returns the value of the 'a' raised to the power of 'b'.
#
# + a - The base value
# + b - The exponent value
# + return - Result value
public extern function pow(float a, float b) returns (float);

# Returns a random number between 0.0 and 1.0.
#
# + return - Random value
public extern function random() returns (float);

# Returns a random number between given start(inclusive) and end(exclusive) values.
#
# + startRange - Range start value
# + endRange - Range end value
# + return - Random value
public extern function randomInRange(int startRange, int endRange) returns (int);

# Returns rounded positive square root of the given value.
#
# + val - Value to get square root
# + return - Square root value
public extern function sqrt(float val) returns (float);

# Returns the absolute value of a float value.
#
# + val - Value to get absolute value
# + return - Absolute value
public extern function absFloat(float val) returns (float);

# Returns the absolute value of an int value.
#
# + val - Value to get the absolute value
# + return - Absolute value
public extern function absInt(int val) returns (int);

# Returns the arc cosine of a value; the returned angle is in the range 0.0 through pi.
#
# + val - Value to get the arc cosine
# + return - Arc cosine value
public extern function acos(float val) returns (float);

# Returns the arc sine of a value.
#
# + val - Value to get the arc sine
# + return - Arc sine value
public extern function asin(float val) returns (float);

# Returns the arc tangent of a value.
#
# + val - Value to get the arc tangent
# + return - Arc tangent value
public extern function atan(float val) returns (float);

# Returns the angle theta from the conversion of rectangular coordinates (a, b) to polar coordinates (r, theta).
#
# + a - The ordinate coordinate
# + b - The abscissa coordinate
# + return - The result
public extern function atan2(float a, float b) returns (float);

# Returns the cube root of a float value.
#
# + val - Value to get the cube root
# + return - Cube root value
public extern function cbrt(float val) returns (float);

# Returns the smallest (closest to negative infinity) double value that is greater than orequal to the argument and
#    is equal to a mathematical integer.

# + val - Value to get the ceil
# + return - The result
public extern function ceil(float val) returns (float);

# Returns the first floating-point argument with the sign of the second floating-point argument.
#
# + a - The parameter providing the magnitude of the result
# + b - The parameter providing the sign of the result
# + return - The result
public extern function copySign(float a, float b) returns (float);

# Returns the trigonometric cosine of an angle.
#
# + val - Value to get the trigonometric cosine
# + return - The result
public extern function cos(float val) returns (float);

# Returns the hyperbolic cosine of a float value.
#
# + val - The number whose hyperbolic cosine is to be returned
# + return - The hyperbolic cosine of given float value
public extern function cosh(float val) returns (float);

# Returns (e to the power of x) -1.
#
# + val - The exponent to raise e to in the computation
# + return - The result
public extern function expm1(float val) returns (float);

# Returns the largest (closest to positive infinity) float value that is less than or equal to the argument and is
#    equal to a mathematical integer.

# + val - A float value
# + return - The result
public extern function floor(float val) returns (float);

# Returns the largest (closest to positive infinity) int value that is less than or equal to the algebraic quotient.
#
# + a - The dividend
# + b - The divisor
# + return - The result
public extern function floorDiv(int a, int b) returns (int);

# Returns the floor modulus of the long arguments.
#
# + a - The dividend
# + b - The divisor
# + return - The result
public extern function floorMod(int a, int b) returns (int);

# Returns the unbiased exponent used in the representation of a float.
#
# + val - Float value
# + return - The unbiased exponent of the argument
public extern function getExponent(float val) returns (int);

# Returns sqrt(a squared +b squared) without intermediate overflow or underflow.
#
# + a - Float value
# + b - Float value
# + return - The result
public extern function hypot(float a, float b) returns (float);

# Computes the remainder operation on two arguments as prescribed by the IEEE 754 standard.
#
# + a - The dividend
# + b - The divisor
# + return - The remainder when a is divided by b
public extern function IEEEremainder(float a, float b) returns (float);

# Returns the natural logarithm (base e) of a float value.
#
# + val - A float value
# + return - The result
public extern function log(float val) returns (float);

# Returns the base 10 logarithm of a float value.
#
# + val - A float value
# + return - The base 10 logarithm of a given float value
public extern function log10(float val) returns (float);

# Returns the natural logarithm of the sum of the argument and 1.
#
# + val - A float value
# + return - The natural log of x + 1
public extern function log1p(float val) returns (float);

# Returns the negation of the argument.
#
# + val - The value to negate
# + return - The result
public extern function negateExact(int val) returns (int);

# Returns the floating-point number adjacent to the first argument in the direction of the second argument.
#
# + a - Starting floating-point value
# + b - Value indicating which of start's neighbors or start should be returned
# + return - The result
public extern function nextAfter(float a, float b) returns (float);

# Returns the adjacent floating-point value closer to negative infinity.
#
# + val - Starting floating-point value
# + return - The result
public extern function nextDown(float val) returns (float);

# Returns the adjacent floating-point value closer to positive infinity.
#
# + val - Starting floating-point value
# + return - The result
public extern function nextUp(float val) returns (float);

# Returns the double value that is closest in value to the argument and is equal to a mathematical integer.
#
# + val - A float value
# + return - The result
public extern function rint(float val) returns (float);

# Returns the closest int to the argument, with ties rounding to positive infinity.
#
# + val - A floating-point value to be rounded to an integer
# + return - The value of the argument rounded to the nearest int value
public extern function round(float val) returns (int);

# Returns a × (2 to the power of b) rounded as if performed by a single correctly rounded floating-point
# multiply to a member of the float value set.
#
# + a - Number to be scaled by a power of two
# + b - Power of 2 used to scale a
# + return - The result
public extern function scalb(float a, int b) returns (float);

# Returns the signum function of the argument.
#
# + val - The floating-point value whose signum is to be returned
# + return - The signum function of the argument
public extern function signum(float val) returns (float);

# Returns the trigonometric sine of an angle.
#
# + val - An angle, in radians
# + return - The sine of the argument
public extern function sin(float val) returns (float);

# Returns the hyperbolic sine of a float value.
#
# + val - The number whose hyperbolic sine is to be returned
# + return - The hyperbolic sine of a given float
public extern function sinh(float val) returns (float);

# Returns the trigonometric tangent of an angle.
#
# + val - An angle, in radians
# + return - The tangent of the argument
public extern function tan(float val) returns (float);

# Returns the hyperbolic tangent of a double value.
#
# + val - The number whose hyperbolic tangent is to be returned
# + return - The hyperbolic tangent of x
public extern function tanh(float val) returns (float);

# Converts an angle measured in radians to an approximately equivalent angle measured in degrees.
#
# + val - An angle, in radians
# + return - The measurement of the angle angrad in degrees
public extern function toDegrees(float val) returns (float);

# Converts an angle measured in degrees to an approximately equivalent angle measured in radians.
#
# + val - An angle, in degrees
# + return - The measurement of the angle angdeg in radians
public extern function toRadians(float val) returns (float);

# Returns the size of an ulp of the argument.
#
# + val - The floating-point value whose ulp is to be returned
# + return - The size of an ulp of the argument
public extern function ulp(float val) returns (float);
