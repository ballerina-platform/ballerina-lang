package ballerina.lang.math;

import ballerina.doc;


@doc:Description { value:"Returns Euler's number, that is 'e' raised to the power of exponent"}
@doc:Param { value:"val: exponent value to raise" }
@doc:Return { value:"float: exp value" }
native function exp (float val) (float);

@doc:Description { value:"Returns the value of the 'a' raised to the power of 'b'"}
@doc:Param { value:"a: the base value" }
@doc:Param { value:"b: the exponent value" }
@doc:Return { value:"float: result value" }
native function pow (float a, float b) (float);

@doc:Description { value:"Returns a random number between 0.0 and 1.0"}
@doc:Return { value:"float: random value" }
native function random () (float);

@doc:Description { value:"Returns a random number between given start(inclusive) and end(exclusive) values"}
@doc:Param { value:"start: range start value" }
@doc:Param { value:"end: range end value" }
@doc:Return { value:"float: random value" }
native function randomInRange (int start, int end) (int);

@doc:Description { value:"Returns rounded positive square root of the given value"}
@doc:Param { value:"val: value to get square root" }
@doc:Return { value:"float: square root value" }
native function sqrt (float val) (float);

@doc:Description { value:"Returns the absolute value of a float value"}
@doc:Param { value:"val: value to get absolute value" }
@doc:Return { value:"float: absolute value" }
native function absFloat (float val) (float);

@doc:Description { value:"Returns the absolute value of an int value"}
@doc:Param { value:"val: value to get the absolute value" }
@doc:Return { value:"int: absolute value" }
native function absInt (int val) (int);

@doc:Description { value:"Returns the arc cosine of a value; the returned angle is in the range 0.0 through pi"}
@doc:Param { value:"val: value to get the arc cosine" }
@doc:Return { value:"float: arc cosine value" }
native function acos (float val) (float);

@doc:Description { value:"Returns the sum of its arguments"}
@doc:Param { value:"a: the first value" }
@doc:Param { value:"b: the second value" }
@doc:Return { value:"float: result value" }
native function addExact (int a, int b) (int);

@doc:Description { value:"Returns the arc sine of a value"}
@doc:Param { value:"val: value to get the arc sine" }
@doc:Return { value:"float: arc sine value" }
native function asin (float val) (float);

@doc:Description { value:"Returns the arc tangent of a value"}
@doc:Param { value:"val: value to get the arc tangent" }
@doc:Return { value:"float: arc tangent value" }
native function atan (float val) (float);

@doc:Description { value:"Returns the angle theta from the conversion of rectangular coordinates (a, b) to polar
coordinates (r, theta)"}
@doc:Param { value:"a: the ordinate coordinate" }
@doc:Param { value:"b: the abscissa coordinate" }
@doc:Return { value:"float: result value" }
native function atan2 (float a, float b) (float);

@doc:Description { value:"Returns the cube root of a float value"}
@doc:Param { value:"val: value to get the cube root" }
@doc:Return { value:"float: cube root value" }
native function cbrt (float val) (float);

@doc:Description { value:"Returns the smallest (closest to negative infinity) double value that is greater than or
equal to the argument and is equal to a mathematical integer"}
@doc:Param { value:"val: value to get the ceil" }
@doc:Return { value:"float: result value" }
native function ceil (float val) (float);

@doc:Description { value:"Returns the first floating-point argument with the sign of the second floating-point
argument"}
@doc:Param { value:"magnitude: the parameter providing the magnitude of the result" }
@doc:Param { value:"sign: the parameter providing the sign of the result" }
@doc:Return { value:"float: result value" }
native function copySign (float a, float b) (float);

@doc:Description { value:"Returns the trigonometric cosine of an angle"}
@doc:Param { value:"val: value to get the trigonometric cosine" }
@doc:Return { value:"float: result value" }
native function cos (float val) (float);

@doc:Description { value:"Returns the hyperbolic cosine of a float value"}
@doc:Param { value:"val: The number whose hyperbolic cosine is to be returned" }
@doc:Return { value:"float: The hyperbolic cosine of given float value" }
native function cosh (float val) (float);

@doc:Description { value:"Returns the argument decremented by one"}
@doc:Param { value:"val: the value to decrement" }
@doc:Return { value:"int: result value" }
native function decrementExact (int val) (int);

@doc:Description { value:"Returns (e to the power of x) -1"}
@doc:Param { value:"val: The exponent to raise e to in the computation" }
@doc:Return { value:"float: Result value" }
native function expm1 (float val) (float);