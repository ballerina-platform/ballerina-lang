// INT_MIN<:Int
// ONE<:Int
// ONE<:ZERO_OR_ONE
// ZERO<:Int
// ZERO<:ZERO_OR_ONE
// ZERO_OR_ONE<:Int
const ONE = 1;
const ZERO = 0;
const INT_MIN = -9223372036854775807 - 1;

type ZERO_OR_ONE ZERO|ONE;
type Int int;

