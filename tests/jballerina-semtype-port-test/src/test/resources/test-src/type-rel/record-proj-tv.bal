type A record {|
    int x;
    string y;
|};

// @type B[XorY] = IorS
// @type B[other] = NEVER
type B record {|
    string x;
    int y;
|};

// @type C[other] = BOOLEAN
// @type C[XorY] = IorS
// @type C[XorYorOther] = IorSOrB
type C record {|
    string x;
    int y;
    float z;
    boolean...;
|};

type IorS int|string;
type IorSOrB IorS|boolean;
type IorSorF IorS|float;

const x = "x";
const z = "z";
const other = "other";
type XorY "x"|"y";
type NEVER never;
type BOOLEAN boolean;
type FLOAT float;

type XorYorOther XorY|other;

// @type AorB[x] = IorS
// @type AorB[XorY] = IorS
type AorB A|B;

// @type AorBorC[x] = IorS
// @type AorBorC[z] = FLOAT
// @type AorBorC[other] = BOOLEAN
// @type AorBorC[XorYorOther] = IorSOrB
type AorBorC AorB|C;
