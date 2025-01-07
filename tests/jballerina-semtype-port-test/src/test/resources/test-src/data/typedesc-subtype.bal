// I<:U1
// I<:U2
// S<:U1
// S<:U2
// U1<:U2

type I typedesc<int>;
type S typedesc<string>;
type U1 I|S;
type U2 typedesc<int|string>;
