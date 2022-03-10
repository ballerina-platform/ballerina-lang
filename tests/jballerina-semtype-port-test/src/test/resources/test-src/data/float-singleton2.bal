// ALSO_NAN<:Float
// ALSO_NAN<:NAN
// INFINITY<:Float
// NAN<:ALSO_NAN
// NAN<:Float
// NEGATIVE_INFINITY<:Float
// NegativeZero<:Float
// NegativeZero<:Zero
// Zero<:Float
// Zero<:NegativeZero

// JBUG this is highlighted as an error
const float INFINITY = 1.0/0.0;
const float NEGATIVE_INFINITY = -1.0/0.0;
const float NAN = 0f/0f;
const float ALSO_NAN = -NAN;

type Zero 0.0;
type NegativeZero -0.0;

type Float float;
