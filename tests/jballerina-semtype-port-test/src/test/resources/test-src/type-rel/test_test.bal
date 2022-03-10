type I int;
type F float;
type S string;

// @type I <> F

type K decimal;

type M map<int>;

// @type M[S] = I

// @type L[I] = I
type L int[];

// @type SL[I] = S
type SL string[];
