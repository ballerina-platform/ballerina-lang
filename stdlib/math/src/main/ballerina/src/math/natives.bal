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

import ballerinax/java;

# The ratio of the circumference of a circle to its diameter.
public const float PI = 3.141592653589793;

# The base of the natural logarithms.
public const float E = 2.718281828459045;

# Returns Euler's number, that is 'e' raised to the power of exponent.
#
# + val - Exponent value to raise
# + return - Exp value
public function exp(float val) returns float {
    return externExp(val);
}

function externExp(float val) returns float = @java:Method {
    name: "exp",
    class: "java.lang.Math"
} external;

# Returns the value of the 'a' raised to the power of 'b'.
#
# + a - The base value
# + b - The exponent value
# + return - Result value
public function pow(float a, float b) returns float {
    return externPow(a, b);
}

function externPow(float a, float b) returns float = @java:Method {
   name: "pow",
   class: "java.lang.Math"
} external;

# Returns a random number between 0.0 and 1.0.
#
# + return - Random value
public function random() returns float {
    return externRandom();
}

function externRandom() returns float = @java:Method {
   name: "random",
   class: "java.lang.Math"
} external;

# Returns a random number between given start(inclusive) and end(exclusive) values.
#
# + startRange - Range start value
# + endRange - Range end value
# + return - Random value or `Error` if start range is greater than the end range
public function randomInRange(int startRange, int endRange) returns int|Error {
    return externRandomInRange(startRange, endRange);
}

function externRandomInRange(int startRange, int endRange) returns int|Error = @java:Method {
    name: "randomInRange",
    class: "org.ballerinalang.stdlib.math.nativeimpl.ExternMethods"
} external;

# Returns rounded positive square root of the given value.
#
# + val - Value to get square root
# + return - Square root value
public function sqrt(float val) returns float {
    return externSqrt(val);
}

function externSqrt(float val) returns float = @java:Method {
    name: "sqrt",
    class: "java.lang.Math"
} external;

# Returns the absolute value of a float value.
#
# + val - Value to get absolute value
# + return - Absolute value
public function absFloat(float val) returns float {
    return externAbsFloat(val);
}

function externAbsFloat(float val) returns float = @java:Method {
    name: "abs",
    class: "java.lang.Math",
    paramTypes: ["double"]
} external;

# Returns the absolute value of an int value.
#
# + val - Value to get the absolute value
# + return - Absolute value
public function absInt(int val) returns int {
    return externAbsInt(val);
}

function externAbsInt(int val) returns int = @java:Method {
    name: "abs",
    class: "java.lang.Math",
    paramTypes: ["long"]
} external;

# Returns the arc cosine of a value; the returned angle is in the range 0.0 through pi.
#
# + val - Value to get the arc cosine
# + return - Arc cosine value
public function acos(float val) returns float {
    return externAcos(val);
}

function externAcos(float val) returns float = @java:Method {
    name: "acos",
    class: "java.lang.Math"
} external;

# Returns the arc sine of a value.
#
# + val - Value to get the arc sine
# + return - Arc sine value
public function asin(float val) returns float {
    return externAsin(val);
}

function externAsin(float val) returns float = @java:Method {
    name: "asin",
    class: "java.lang.Math"
} external;

# Returns the arc tangent of a value.
#
# + val - Value to get the arc tangent
# + return - Arc tangent value
public function atan(float val) returns float {
    return externAtan(val);
}

function externAtan(float val) returns float = @java:Method {
    name: "atan",
    class: "java.lang.Math"
} external;

# Returns the angle theta from the conversion of rectangular coordinates (a, b) to polar coordinates (r, theta).
#
# + a - The ordinate coordinate
# + b - The abscissa coordinate
# + return - The result
public function atan2(float a, float b) returns float {
    return externAtan2(a, b);
}

function externAtan2(float a, float b) returns float = @java:Method {
    name: "atan2",
    class: "java.lang.Math"
} external;

# Returns the cube root of a float value.
#
# + val - Value to get the cube root
# + return - Cube root value
public function cbrt(float val) returns float {
    return externCbrt(val);
}

function externCbrt(float val) returns float = @java:Method {
    name: "cbrt",
    class: "java.lang.Math"
} external;

# Returns the smallest (closest to negative infinity) double value that is greater than orequal to the argument and
#    is equal to a mathematical integer.

# + val - Value to get the ceil
# + return - The result
public function ceil(float val) returns float {
    return externCeil(val);
}

function externCeil(float val) returns float = @java:Method {
    name: "ceil",
    class: "java.lang.Math"
} external;

# Returns the first floating-point argument with the sign of the second floating-point argument.
#
# + a - The parameter providing the magnitude of the result
# + b - The parameter providing the sign of the result
# + return - The result
public function copySign(float a, float b) returns float {
    return externCopySign(a, b);
}

function externCopySign(float a, float b) returns float = @java:Method {
    name: "copySign",
    class: "java.lang.Math",
    paramTypes: ["double", "double"]
} external;

# Returns the trigonometric cosine of an angle.
#
# + val - Value to get the trigonometric cosine
# + return - The result
public function cos(float val) returns float {
    return externCos(val);
}

function externCos(float val) returns float = @java:Method {
    name: "cos",
    class: "java.lang.Math"
} external;

# Returns the hyperbolic cosine of a float value.
#
# + val - The number whose hyperbolic cosine is to be returned
# + return - The hyperbolic cosine of given float value
public function cosh(float val) returns float {
    return externCosh(val);
}

function externCosh(float val) returns float = @java:Method {
    name: "cosh",
    class: "java.lang.Math"
} external;

# Returns (e to the power of x) -1.
#
# + val - The exponent to raise e to in the computation
# + return - The result
public function expm1(float val) returns float {
    return externExpm1(val);
}

function externExpm1(float val) returns float = @java:Method {
    name: "expm1",
    class: "java.lang.Math"
} external;

# Returns the largest (closest to positive infinity) float value that is less than or equal to the argument and is
#    equal to a mathematical integer.

# + val - A float value
# + return - The result
public function floor(float val) returns float {
    return externFloor(val);
}

function externFloor(float val) returns float = @java:Method {
    name: "floor",
    class: "java.lang.Math"
} external;

# Returns the largest (closest to positive infinity) int value that is less than or equal to the algebraic quotient.
#
# + a - The dividend
# + b - The divisor
# + return - The result or `Error` if b is 0
public function floorDiv(int a, int b) returns int|Error {
    return externFloorDiv(a, b);
}

function externFloorDiv(int a, int b) returns int|Error = @java:Method {
    name: "floorDiv",
    class: "org.ballerinalang.stdlib.math.nativeimpl.ExternMethods"
} external;

# Returns the floor modulus of the long arguments.
#
# + a - The dividend
# + b - The divisor
# + return - The result or `Error` if b is 0
public function floorMod(int a, int b) returns int|Error {
    return externFloorMod(a, b);
}

function externFloorMod(int a, int b) returns int|Error = @java:Method {
    name: "floorMod",
    class: "org.ballerinalang.stdlib.math.nativeimpl.ExternMethods"
} external;

# Returns the unbiased exponent used in the representation of a float.
#
# + val - Float value
# + return - The unbiased exponent of the argument
public function getExponent(float val) returns int {
    return externGetExponent(val);
}

function externGetExponent(float val) returns int = @java:Method {
    name: "getExponent",
    class: "java.lang.Math",
    paramTypes: ["double"]
} external;

# Returns sqrt(a squared +b squared) without intermediate overflow or underflow.
#
# + a - Float value
# + b - Float value
# + return - The result
public function hypot(float a, float b) returns float {
    return externHypot(a, b);
}

function externHypot(float a, float b) returns float = @java:Method {
    name: "hypot",
    class: "java.lang.Math"
} external;

# Computes the remainder operation on two arguments as prescribed by the IEEE 754 standard.
#
# + a - The dividend
# + b - The divisor
# + return - The remainder when a is divided by b
public function remainder(float a, float b) returns float {
    return externRemainder(a, b);
}

function externRemainder(float a, float b) returns float = @java:Method {
    name: "IEEEremainder",
    class: "java.lang.Math"
} external;

# Returns the natural logarithm (base e) of a float value.
#
# + val - A float value
# + return - The result
public function log(float val) returns float {
    return externLog(val);
}

function externLog(float val) returns float = @java:Method {
    name: "log",
    class: "java.lang.Math"
} external;

# Returns the base 10 logarithm of a float value.
#
# + val - A float value
# + return - The base 10 logarithm of a given float value
public function log10(float val) returns float {
    return externLog10(val);
}

function externLog10(float val) returns float = @java:Method {
    name: "log10",
    class: "java.lang.Math"
} external;

# Returns the natural logarithm of the sum of the argument and 1.
#
# + val - A float value
# + return - The natural log of x + 1
public function log1p(float val) returns float {
    return externLog1p(val);
}

function externLog1p(float val) returns float = @java:Method {
    name: "log1p",
    class: "java.lang.Math"
} external;

# Returns the negation of the argument.
#
# + val - The value to negate
# + return - The result or `Error` if overflow occurred
public function negateExact(int val) returns int|Error {
    return externNegateExact(val);
}

function externNegateExact(int val) returns int|Error = @java:Method {
    name: "negateExact",
    class: "org.ballerinalang.stdlib.math.nativeimpl.ExternMethods"
} external;

# Returns the floating-point number adjacent to the first argument in the direction of the second argument.
#
# + a - Starting floating-point value
# + b - Value indicating which of start's neighbors or start should be returned
# + return - The result
public function nextAfter(float a, float b) returns float {
    return externNextAfter(a, b);
}

function externNextAfter(float a, float b) returns float = @java:Method {
    name: "nextAfter",
    class: "java.lang.Math",
    paramTypes: ["double", "double"]
} external;

# Returns the adjacent floating-point value closer to negative infinity.
#
# + val - Starting floating-point value
# + return - The result
public function nextDown(float val) returns float {
    return externNextDown(val);
}

function externNextDown(float val) returns float = @java:Method {
    name: "nextDown",
    class: "java.lang.Math",
    paramTypes: ["double"]
} external;

# Returns the adjacent floating-point value closer to positive infinity.
#
# + val - Starting floating-point value
# + return - The result
public function nextUp(float val) returns float {
    return externNextUp(val);
}

function externNextUp(float val) returns float = @java:Method {
    name: "nextUp",
    class: "java.lang.Math",
    paramTypes: ["double"]
} external;

# Returns the double value that is closest in value to the argument and is equal to a mathematical integer.
#
# + val - A float value
# + return - The result
public function rint(float val) returns float {
    return externRint(val);
}

function externRint(float val) returns float = @java:Method {
    name: "rint",
    class: "java.lang.Math"
} external;

# Returns the closest int to the argument, with ties rounding to positive infinity.
#
# + val - A floating-point value to be rounded to an integer
# + return - The value of the argument rounded to the nearest int value
public function round(float val) returns int {
    return externRound(val);
}

function externRound(float val) returns int = @java:Method {
    name: "round",
    class: "java.lang.Math",
    paramTypes: ["double"]
} external;

# Returns a × (2 to the power of b) rounded as if performed by a single correctly rounded floating-point
# multiply to a member of the float value set.
#
# + a - Number to be scaled by a power of two
# + b - Power of 2 used to scale a
# + return - The result
public function scalb(float a, int b) returns float {
    return externScalb(a, b);
}

function externScalb(float a, int b) returns float = @java:Method {
    name: "scalb",
    class: "org.ballerinalang.stdlib.math.nativeimpl.ExternMethods"
} external;

# Returns the signum function of the argument.
#
# + val - The floating-point value whose signum is to be returned
# + return - The signum function of the argument
public function signum(float val) returns float {
    return externSignum(val);
}

function externSignum(float val) returns float = @java:Method {
    name: "signum",
    class: "java.lang.Math",
    paramTypes: ["double"]
} external;

# Returns the trigonometric sine of an angle.
#
# + val - An angle, in radians
# + return - The sine of the argument
public function sin(float val) returns float {
    return externSin(val);
}

function externSin(float val) returns float = @java:Method {
    name: "sin",
    class: "java.lang.Math"
} external;

# Returns the hyperbolic sine of a float value.
#
# + val - The number whose hyperbolic sine is to be returned
# + return - The hyperbolic sine of a given float
public function sinh(float val) returns float {
    return externSinh(val);
}

function externSinh(float val) returns float = @java:Method {
    name: "sinh",
    class: "java.lang.Math"
} external;

# Returns the trigonometric tangent of an angle.
#
# + val - An angle, in radians
# + return - The tangent of the argument
public function tan(float val) returns float {
    return externTan(val);
}

function externTan(float val) returns float = @java:Method {
    name: "tan",
    class: "java.lang.Math"
} external;

# Returns the hyperbolic tangent of a double value.
#
# + val - The number whose hyperbolic tangent is to be returned
# + return - The hyperbolic tangent of x
public function tanh(float val) returns float {
    return externTanh(val);
}

function externTanh(float val) returns float = @java:Method {
    name: "tanh",
    class: "java.lang.Math"
} external;

# Converts an angle measured in radians to an approximately equivalent angle measured in degrees.
#
# + val - An angle, in radians
# + return - The measurement of the angle angrad in degrees
public function toDegrees(float val) returns float {
    return externToDegrees(val);
}

function externToDegrees(float val) returns float = @java:Method {
    name: "toDegrees",
    class: "java.lang.Math"
} external;

# Converts an angle measured in degrees to an approximately equivalent angle measured in radians.
#
# + val - An angle, in degrees
# + return - The measurement of the angle angdeg in radians
public function toRadians(float val) returns float {
    return externToRadians(val);
}

function externToRadians(float val) returns float = @java:Method {
    name: "toRadians",
    class: "java.lang.Math"
} external;

# Returns the size of an ulp of the argument.
#
# + val - The floating-point value whose ulp is to be returned
# + return - The size of an ulp of the argument
public function ulp(float val) returns float {
    return externUlp(val);
}

function externUlp(float val) returns float = @java:Method {
    name: "ulp",
    class: "java.lang.Math",
    paramTypes: ["double"]
} external;
