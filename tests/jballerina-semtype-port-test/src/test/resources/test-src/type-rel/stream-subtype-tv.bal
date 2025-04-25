// @type I < U1
// @type I < U2
// @type S < U1
// @type S < U2
// @type U1 < U2

type I stream<int>;
type S stream<string>;
type U1 I|S;
type U2 stream<int|string>;
