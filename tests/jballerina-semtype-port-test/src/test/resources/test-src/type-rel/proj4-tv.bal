const ZERO = 0;
const ONE = 1;
type Index ZERO|ONE;
type Int int;
type Str string;
type IntStr Int|Str;

// @type T[ZERO] = Int
// @type T[ONE] = Str
// @type T[Index] = IntStr
type T [int, string];
