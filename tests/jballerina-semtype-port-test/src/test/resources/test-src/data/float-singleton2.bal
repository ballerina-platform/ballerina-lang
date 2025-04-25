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

const INFINITY = 1.0/0.0;
const NEGATIVE_INFINITY = -1.0/0.0;
const NAN = 0f/0f;
const ALSO_NAN = -NAN;

type Zero 0.0;
type NegativeZero -0.0;

type Float float;
