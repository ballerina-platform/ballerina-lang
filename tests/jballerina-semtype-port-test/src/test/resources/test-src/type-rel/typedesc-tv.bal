// @type I < U1
// @type I < U2
// @type S < U1
// @type S < U2
// @type U1 < U2

type I typedesc<int>;

type S typedesc<string>;

type U1 I|S;

type U2 typedesc<int|string>;

// @type TS <> T_ANYDATA
type TS typedesc<stream>;

type T_ANYDATA typedesc<anydata>;
