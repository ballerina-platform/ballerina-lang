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


documentation {
    The ratio of the circumference of a circle to its diameter.
}
@final public float PI = 3.141592653589793;

documentation {
    The base of the natural logarithms.
}
@final public float E = 2.718281828459045;

documentation {
    Returns Euler's number, that is 'e' raised to the power of exponent.

    P{{val}} Exponent value to raise
    R{{}} Exp value
}
public extern function exp(float val) returns (float);

documentation {
    Returns the value of the 'a' raised to the power of 'b'.

    P{{a}} The base value
    P{{b}} The exponent value
    R{{}} Result value
}
public extern function pow(float a, float b) returns (float);

documentation {
    Returns a random number between 0.0 and 1.0.

    R{{}} Random value
}
public extern function random() returns (float);

documentation {
    Returns a random number between given start(inclusive) and end(exclusive) values.

    P{{startRange}} Range start value
    P{{endRange}} Range end value
    R{{}} Random value
}
public extern function randomInRange(int startRange, int endRange) returns (int);

documentation {
    Returns rounded positive square root of the given value.

    P{{val}} Value to get square root
    R{{}} Square root value
}
public extern function sqrt(float val) returns (float);

documentation {
    Returns the absolute value of a float value.

    P{{val}} Value to get absolute value
    R{{}} Absolute value
}
public extern function absFloat(float val) returns (float);

documentation {
    Returns the absolute value of an int value.

    P{{val}} Value to get the absolute value
    R{{}} bsolute value
}
public extern function absInt(int val) returns (int);

documentation {
    Returns the arc cosine of a value; the returned angle is in the range 0.0 through pi.

    P{{val}} Value to get the arc cosine
    R{{}} rc cosine value
}
public extern function acos(float val) returns (float);

documentation {
    Returns the arc sine of a value.

    P{{val}} Value to get the arc sine
    R{{}} rc sine value
}
public extern function asin(float val) returns (float);

documentation {
    Returns the arc tangent of a value.

    P{{val}} Value to get the arc tangent
    R{{}} rc tangent value
}
public extern function atan(float val) returns (float);

documentation {
    Returns the angle theta from the conversion of rectangular coordinates (a, b) to polar coordinates (r, theta).

    P{{a}} The ordinate coordinate
    P{{b}} The abscissa coordinate
    R{{}} he result
}
public extern function atan2(float a, float b) returns (float);

documentation {
    Returns the cube root of a float value.

    P{{val}} Value to get the cube root
    R{{}} ube root value
}
public extern function cbrt(float val) returns (float);

documentation {
    Returns the smallest (closest to negative infinity) double value that is greater than orequal to the argument and
    is equal to a mathematical integer.

    P{{val}} Value to get the ceil
    R{{}} he result
}
public extern function ceil(float val) returns (float);

documentation {
    Returns the first floating-point argument with the sign of the second floating-point argument.

    P{{a}} The parameter providing the magnitude of the result
    P{{b}} The parameter providing the sign of the result
    R{{}} he result
}
public extern function copySign(float a, float b) returns (float);

documentation {
    Returns the trigonometric cosine of an angle.

    P{{val}} Value to get the trigonometric cosine
    R{{}} he result
}
public extern function cos(float val) returns (float);

documentation {
    Returns the hyperbolic cosine of a float value.

    P{{val}} The number whose hyperbolic cosine is to be returned
    R{{}} he hyperbolic cosine of given float value
}
public extern function cosh(float val) returns (float);

documentation {
    Returns (e to the power of x) -1.

    P{{val}} The exponent to raise e to in the computation
    R{{}} he result
}
public extern function expm1(float val) returns (float);

documentation {
    Returns the largest (closest to positive infinity) float value that is less than or equal to the argument and is
    equal to a mathematical integer.

    P{{val}} A float value
    R{{}} he result
}
public extern function floor(float val) returns (float);

documentation {
    Returns the largest (closest to positive infinity) int value that is less than or equal to the algebraic quotient.

    P{{a}} The dividend
    P{{b}} The divisor
    R{{}} he result
}
public extern function floorDiv(int a, int b) returns (int);

documentation {
    Returns the floor modulus of the long arguments.

    P{{a}} The dividend
    P{{b}} The divisor
    R{{}} he result
}
public extern function floorMod(int a, int b) returns (int);

documentation {
    Returns the unbiased exponent used in the representation of a float.

    P{{val}} Float value
    R{{}} he unbiased exponent of the argument
    }
public extern function getExponent(float val) returns (int);

documentation {
    Returns sqrt(a squared +b squared) without intermediate overflow or underflow.

    P{{a}} Float value
    P{{b}} Float value
    R{{}} he result
}
public extern function hypot(float a, float b) returns (float);

documentation {
    Computes the remainder operation on two arguments as prescribed by the IEEE 754 standard.

    P{{a}} The dividend
    P{{b}} The divisor
    R{{}} he remainder when a is divided by b
}
public extern function IEEEremainder(float a, float b) returns (float);

documentation {
    Returns the natural logarithm (base e) of a float value.

    P{{val}} A float value
    R{{}} he result
}
public extern function log(float val) returns (float);

documentation {
    Returns the base 10 logarithm of a float value.

    P{{val}} A float value
    R{{}} he base 10 logarithm of a given float value
}
public extern function log10(float val) returns (float);

documentation {
    Returns the natural logarithm of the sum of the argument and 1.

    P{{val}} A float value
    R{{}} he natural log of x + 1
}
public extern function log1p(float val) returns (float);

documentation {
    Returns the negation of the argument.

    P{{val}} The value to negate
    R{{}} he result
}
public extern function negateExact(int val) returns (int);

documentation {
    Returns the floating-point number adjacent to the first argument in the direction of the second argument.

    P{{a}} Starting floating-point value
    P{{b}} Value indicating which of start's neighbors or start should be returned
    R{{}} he result
}
public extern function nextAfter(float a, float b) returns (float);

documentation {
    Returns the adjacent floating-point value closer to negative infinity.

    P{{val}} Starting floating-point value
    R{{}} he result
}
public extern function nextDown(float val) returns (float);

documentation {
    Returns the adjacent floating-point value closer to positive infinity.

    P{{val}} Starting floating-point value
    R{{}} he result
}
public extern function nextUp(float val) returns (float);

documentation {
    Returns the double value that is closest in value to the argument and is equal to a mathematical integer.

    P{{val}} A float value
    R{{}} he result
}
public extern function rint(float val) returns (float);

documentation {
    Returns the closest int to the argument, with ties rounding to positive infinity.

    P{{val}} A floating-point value to be rounded to an integer
    R{{}} he value of the argument rounded to the nearest int value
}
public extern function round(float val) returns (int);

documentation {
    Returns a × (2 to the power of b) rounded as if performed by a single correctly rounded floating-point
    multiply to a member of the float value set.

    P{{a}} Number to be scaled by a power of two
    P{{b}} Power of 2 used to scale a
    R{{}} he result
}
public extern function scalb(float a, int b) returns (float);

documentation {
    Returns the signum function of the argument.

    P{{val}} The floating-point value whose signum is to be returned
    R{{}} he signum function of the argument
}
public extern function signum(float val) returns (float);

documentation {
    Returns the trigonometric sine of an angle.

    P{{val}} An angle, in radians
    R{{}} he sine of the argument
}
public extern function sin(float val) returns (float);

documentation {
    Returns the hyperbolic sine of a float value.

    P{{val}} The number whose hyperbolic sine is to be returned
    R{{}} he hyperbolic sine of a given float
}
public extern function sinh(float val) returns (float);

documentation {
    Returns the trigonometric tangent of an angle.

    P{{val}} An angle, in radians
    R{{}} he tangent of the argument
}
public extern function tan(float val) returns (float);

documentation {
    Returns the hyperbolic tangent of a double value.

    P{{val}} The number whose hyperbolic tangent is to be returned
    R{{}} he hyperbolic tangent of x
}
public extern function tanh(float val) returns (float);

documentation {
    Converts an angle measured in radians to an approximately equivalent angle measured in degrees.

    P{{val}} An angle, in radians
    R{{}} he measurement of the angle angrad in degrees
}
public extern function toDegrees(float val) returns (float);

documentation {
    Converts an angle measured in degrees to an approximately equivalent angle measured in radians.

    P{{val}} An angle, in degrees
    R{{}} he measurement of the angle angdeg in radians
}
public extern function toRadians(float val) returns (float);

documentation {
    Returns the size of an ulp of the argument.

    P{{val}} The floating-point value whose ulp is to be returned
    R{{}} he size of an ulp of the argument
}
public extern function ulp(float val) returns (float);
