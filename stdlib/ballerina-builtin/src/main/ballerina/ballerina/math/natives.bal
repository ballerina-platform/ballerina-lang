package ballerina.math;

@Description {value:"The ratio of the circumference of a circle to its diameter"}
public const float PI = 3.141592653589793;

@Description {value:"The base of the natural logarithms"}
public const float E = 2.718281828459045;

@Description {value:"Returns Euler's number, that is 'e' raised to the power of exponent"}
@Param {value:"val: Exponent value to raise"}
@Return { value:"Exp value"}
documentation {
Returns Euler's number, that is 'e' raised to the power of exponent.
- #val Exponent value to raise
- #expVal Exp value
}
public native function exp (float val) (float expVal);

@Description {value:"Returns the value of the 'a' raised to the power of 'b'"}
@Param {value:"a: The base value"}
@Param {value:"b: The exponent value"}
@Return { value:"Result value"}
documentation {
Returns the value of the 'a' raised to the power of 'b'.
- #a The base value
- #b The exponent value
- #result Result value
}
public native function pow (float a, float b) (float result);

@Description {value:"Returns a random number between 0.0 and 1.0"}
@Return { value:"Random value"}
documentation {
Returns a random number between 0.0 and 1.0.
- #val Random value
}
public native function random () (float val);

@Description {value:"Returns a random number between given start(inclusive) and end(exclusive) values"}
@Param {value:"start: Range start value"}
@Param {value:"end: Range end value"}
@Return { value:"Random value"}
documentation {
Returns a random number between given start(inclusive) and end(exclusive) values.
- #start Range start value
- #end Range end value
- #val Random value
}
public native function randomInRange (int start, int end) (int val);

@Description {value:"Returns rounded positive square root of the given value"}
@Param {value:"val: Value to get square root"}
@Return { value:"Square root value"}
documentation {
Returns rounded positive square root of the given value.
- #val Value to get square root
- #sqVal Square root value
}
public native function sqrt (float val) (float sqVal);

@Description {value:"Returns the absolute value of a float value"}
@Param {value:"val: Value to get absolute value"}
@Return { value:"Absolute value"}
documentation {
Returns the absolute value of a float value.
- #val Value to get absolute value
- #absVal Absolute value
}
public native function absFloat (float val) (float absVal);

@Description {value:"Returns the absolute value of an int value"}
@Param {value:"val: Value to get the absolute value"}
@Return { value:"Absolute value"}
documentation {
Returns the absolute value of an int value.
- #val Value to get absolute value
- #absVal Absolute value
}
public native function absInt (int val) (int absVal);

@Description {value:"Returns the arc cosine of a value; the returned angle is in the range 0.0 through pi"}
@Param {value:"val: Value to get the arc cosine"}
@Return { value:"Arc cosine value"}
documentation {
Returns the arc cosine of a value; the returned angle is in the range 0.0 through pi.
- #val Value to get the arc cosine
- #cosVal Arc cosine value
}
public native function acos (float val) (float cosVal);

@Description {value:"Returns the arc sine of a value"}
@Param {value:"val: Value to get the arc sine"}
@Return { value:"Arc sine value"}
documentation {
Returns the arc sine of a value.
- #val Value to get the arc sine
- #sineVal Arc sine value
}
public native function asin (float val) (float sineVal);

@Description {value:"Returns the arc tangent of a value"}
@Param {value:"val: Value to get the arc tangent"}
@Return { value:"Arc tangent value"}
documentation {
Returns the arc tangent of a value.
- #val Value to get the arc tangent
- #tanVal Arc tangent value
}
public native function atan (float val) (float tanVal);

@Description {value:"Returns the angle theta from the conversion of rectangular coordinates (a, b) to polar
coordinates (r, theta)"}
@Param {value:"a: The ordinate coordinate"}
@Param {value:"b: The abscissa coordinate"}
@Return { value:"The result"}
documentation {
Returns the angle theta from the conversion of rectangular coordinates (a, b) to polar coordinates (r, theta).
- #a The ordinate coordinate
- #b The abscissa coordinate
- #result The result
}
public native function atan2 (float a, float b) (float result);

@Description {value:"Returns the cube root of a float value"}
@Param {value:"val: Value to get the cube root"}
@Return { value:"Cube root value"}
documentation {
Returns the cube root of a float value.
- #val Value to get the cube root
- #cubeVal Cube root value
}
public native function cbrt (float val) (float cubeVal);

@Description {value:"Returns the smallest (closest to negative infinity) double value that is greater than or
equal to the argument and is equal to a mathematical integer"}
@Param {value:"val: Value to get the ceil"}
@Return { value:"The result"}
documentation {
Returns the smallest (closest to negative infinity) double value that is greater than or equal to the argument and is equal to a mathematical integer.
- #val Value to get the ceil
- #result The result
}
public native function ceil (float val) (float result);

@Description {value:"Returns the first floating-point argument with the sign of the second floating-point
argument"}
@Param {value:"magnitude: The parameter providing the magnitude of the result"}
@Param {value:"sign: The parameter providing the sign of the result"}
@Return { value:"The result"}
documentation {
Returns the first floating-point argument with the sign of the second floating-point argument.
- #magnitude The parameter providing the magnitude of the result
- #sign The parameter providing the sign of the result
- #result The result
}
public native function copySign (float magnitude, float sign) (float result);

@Description {value:"Returns the trigonometric cosine of an angle"}
@Param {value:"val: Value to get the trigonometric cosine"}
@Return { value:"The result"}
documentation {
Returns the trigonometric cosine of an angle.
- #val Value to get the trigonometric cosine
- #result The result
}
public native function cos (float val) (float result);

@Description {value:"Returns the hyperbolic cosine of a float value"}
@Param {value:"val: The number whose hyperbolic cosine is to be returned"}
@Return { value:"The hyperbolic cosine of given float value"}
documentation {
Returns the hyperbolic cosine of a float value.
- #val The number whose hyperbolic cosine is to be returned
- #result The hyperbolic cosine of given float value
}
public native function cosh (float val) (float result);

@Description {value:"Returns (e to the power of x) -1"}
@Param {value:"val: The exponent to raise e to in the computation"}
@Return { value:"The result"}
documentation {
Returns (e to the power of x) -1.
- #val The exponent to raise e to in the computation
- #result The result
}
public native function expm1 (float val) (float result);

@Description {value:"Returns the largest (closest to positive infinity) float value that is less than or equal
to the argument and is equal to a mathematical integer"}
@Param {value:"val: A float value"}
@Return { value:"The result"}
documentation {
Returns the largest (closest to positive infinity) float value that is less than or equal to the argument and is equal to a mathematical integer.
- #val A float value
- #result The result
}
public native function floor (float val) (float result);

@Description {value:"Returns the largest (closest to positive infinity) int value that is less than or equal
to the algebraic quotient"}
@Param {value:"a: The dividend"}
@Param {value:"b: The divisor"}
@Return { value:"The result"}
documentation {
Returns the largest (closest to positive infinity) int value that is less than or equal to the algebraic quotient.
- #a The dividend
- #b The divisor
- #result The result
}
public native function floorDiv (int a, int b) (int result);

@Description {value:"Returns the floor modulus of the long arguments"}
@Param {value:"a: The dividend"}
@Param {value:"b: The divisor"}
@Return { value:"The result"}
documentation {
Returns the floor modulus of the long arguments.
- #a The dividend
- #b The divisor
- #result The result
}
public native function floorMod (int a, int b) (int result);

@Description {value:"Returns the unbiased exponent used in the representation of a float"}
@Param {value:"val: Float value"}
@Return { value:"The unbiased exponent of the argument"}
documentation {
Returns the unbiased exponent used in the representation of a float.
- #val Float value
- #expVal The unbiased exponent of the argument
}
public native function getExponent (float val) (int expVal);

@Description {value:"Returns sqrt(a squared +b squared) without intermediate overflow or underflow"}
@Param {value:"a: Float value"}
@Param {value:"b: Float value"}
@Return { value:"the result"}
documentation {
Returns sqrt(a squared +b squared) without intermediate overflow or underflow.
- #a Float value
- #b Float value
- #result The result
}
public native function hypot (float a, float b) (float result);

@Description {value:"Computes the remainder operation on two arguments as prescribed by the IEEE 754 standard"}
@Param {value:"a: The dividend"}
@Param {value:"b: The divisor"}
@Return { value:"The remainder when a is divided by b"}
documentation {
Computes the remainder operation on two arguments as prescribed by the IEEE 754 standard.
- #a The dividend
- #b The divisor
- #result The remainder when a is divided by b
}
public native function IEEEremainder (float a, float b) (float result);

@Description {value:"Returns the natural logarithm (base e) of a float value"}
@Param {value:"val: A float value"}
@Return { value:"The result"}
documentation {
Returns the natural logarithm (base e) of a float value.
- #val A float value
- #result The result
}
public native function log (float val) (float result);

@Description {value:"Returns the base 10 logarithm of a float value"}
@Param {value:"val: A float value"}
@Return { value:"The base 10 logarithm of a given float value"}
documentation {
Returns the base 10 logarithm of a float value.
- #val A float value
- #result The base 10 logarithm of a given float value
}
public native function log10 (float val) (float result);

@Description {value:"Returns the natural logarithm of the sum of the argument and 1"}
@Param {value:"val: A float value"}
@Return { value:"The natural log of x + 1"}
documentation {
Returns the natural logarithm of the sum of the argument and 1.
- #val A float value
- #result The natural log of x + 1
}
public native function log1p (float val) (float result);

@Description {value:"Returns the negation of the argument"}
@Param {value:"val: The value to negate"}
@Return { value:"The result"}
documentation {
Returns the negation of the argument.
- #val The value to negate
- #result The result
}
public native function negateExact (int val) (int result);

@Description {value:"Returns the floating-point number adjacent to the first argument in the direction of the
second argument"}
@Param {value:"a: Starting floating-point value"}
@Param {value:"b: Value indicating which of start's neighbors or start should be returned"}
@Return { value:"The result"}
documentation {
Returns the floating-point number adjacent to the first argument in the direction of the second argument.
- #a Starting floating-point value
- #a Value indicating which of start's neighbors or start should be returned
- #result The result
}
public native function nextAfter (float a, float b) (float result);

@Description {value:"Returns the adjacent floating-point value closer to negative infinity"}
@Param {value:"val: Starting floating-point value"}
@Return { value:"The result"}
documentation {
Returns the adjacent floating-point value closer to negative infinity.
- #val Starting floating-point value
- #result The result
}
public native function nextDown (float val) (float result);

@Description {value:"Returns the adjacent floating-point value closer to positive infinity"}
@Param {value:"val: Starting floating-point value"}
@Return { value:"The result"}
documentation {
Returns the adjacent floating-point value closer to positive infinity.
- #val Starting floating-point value
- #result The result
}
public native function nextUp (float val) (float result);

@Description {value:"Returns the double value that is closest in value to the argument and is equal to a
mathematical integer"}
@Param {value:"val: A float value"}
@Return { value:"The result"}
documentation {
Returns the double value that is closest in value to the argument and is equal to a mathematical integer.
- #val A float value
- #result The result
}
public native function rint (float val) (float result);

@Description {value:"Returns the closest int to the argument, with ties rounding to positive infinity"}
@Param {value:"val: A floating-point value to be rounded to an integer"}
@Return { value:"The value of the argument rounded to the nearest int value"}
documentation {
Returns the closest int to the argument, with ties rounding to positive infinity.
- #val A floating-point value to be rounded to an integer
- #result The value of the argument rounded to the nearest int value
}
public native function round (float val) (int result);

@Description {value:"Returns a × (2 to the power of scaleFactor) rounded as if performed by a single correctly
rounded floating-point  multiply to a member of the float value set"}
@Param {value:"a: Number to be scaled by a power of two"}
@Param {value:"scaleFactor: Power of 2 used to scale a"}
@Return { value:"The result"}
documentation {
Returns a × (2 to the power of scaleFactor) rounded as if performed by a single correctly
rounded floating-point  multiply to a member of the float value set.
- #val Number to be scaled by a power of two
- #scaleFactor Power of 2 used to scale a
- #result The result
}
public native function scalb (float val, int scaleFactor) (float result);

@Description {value:"Returns the signum function of the argument"}
@Param {value:"val: The floating-point value whose signum is to be returned"}
@Return { value:"The signum function of the argument"}
documentation {
Returns the signum function of the argument.
- #val The floating-point value whose signum is to be returned
- #result The signum function of the argument
}
public native function signum (float val) (float result);

@Description {value:"Returns the trigonometric sine of an angle"}
@Param {value:"val: An angle, in radians"}
@Return { value:"The sine of the argument"}
documentation {
Returns the trigonometric sine of an angle.
- #val An angle, in radians
- #result The sine of the argument
}
public native function sin (float val) (float result);

@Description {value:"Returns the hyperbolic sine of a float value"}
@Param {value:"val: The number whose hyperbolic sine is to be returned"}
@Return { value:"The hyperbolic sine of a given float"}
documentation {
Returns the hyperbolic sine of a float value.
- #val The number whose hyperbolic sine is to be returned
- #result The hyperbolic sine of a given float
}
public native function sinh (float val) (float result);

@Description {value:"Returns the trigonometric tangent of an angle"}
@Param {value:"val: An angle, in radians"}
@Return { value:"The tangent of the argument"}
documentation {
Returns the trigonometric tangent of an angle.
- #val An angle, in radians
- #result The tangent of the argument
}
public native function tan (float val) (float result);

@Description {value:"Returns the hyperbolic tangent of a double value"}
@Param {value:"val: The number whose hyperbolic tangent is to be returned"}
@Return { value:"The hyperbolic tangent of x"}
documentation {
Returns the hyperbolic tangent of a double value.
- #val The number whose hyperbolic tangent is to be returned
- #result The hyperbolic tangent of x
}
public native function tanh (float val) (float result);

@Description {value:"Converts an angle measured in radians to an approximately equivalent angle measured in
degrees"}
@Param {value:"val: An angle, in radians"}
@Return { value:"The measurement of the angle angrad in degrees"}
documentation {
Converts an angle measured in radians to an approximately equivalent angle measured in degrees.
- #val An angle, in radians
- #result The measurement of the angle angrad in degrees
}
public native function toDegrees (float val) (float result);

@Description {value:"Converts an angle measured in degrees to an approximately equivalent angle measured in
radians"}
@Param {value:"val: An angle, in degrees"}
@Return { value:"The measurement of the angle angdeg in radians"}
documentation {
Converts an angle measured in degrees to an approximately equivalent angle measured in radians.
- #val An angle, in degrees
- #result The measurement of the angle angdeg in radians
}
public native function toRadians (float val) (float result);

@Description {value:"Returns the size of an ulp of the argument"}
@Param {value:"val: The floating-point value whose ulp is to be returned"}
@Return { value:"The size of an ulp of the argument"}
documentation {
Returns the size of an ulp of the argument.
- #val The floating-point value whose ulp is to be returned
- #result The size of an ulp of the argument
}
public native function ulp (float val) (float result);
