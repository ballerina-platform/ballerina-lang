
import ballerina/doc;


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
