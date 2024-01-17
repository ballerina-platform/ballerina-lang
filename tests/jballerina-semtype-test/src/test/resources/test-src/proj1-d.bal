type S string;
type I int;
type N 2;
const ONE = 1;

// @type BL[1] = B
// @type BL[2] = B
// @type BL[I] = B
// @type BL[N] = B
// @type BL[ONE] = B
type BL boolean[];

// @type M[S] = B
type M map<boolean>;

type f1 "f1";
type f2 "f2";
const FOO = "f2";

// @type R[f1] = INTEGER
// @type R[f2] = B
// @type R[FOO] = B
type R record {|
    int f1;
    boolean f2;
|};
