// I<:U1
// I<:U2
// S<:U1
// S<:U2
// U1<:U2

type I stream<int>;
type S stream<string>;
type U1 I|S;
type U2 stream<int|string>;
