// WHY<:S
// WHY<:Y
// WHY<:Y1
// X1<:S
// X1<:X
// X1<:XES
// X2<:S
// X2<:XES
// X<:S
// X<:X1
// X<:XES
// XES<:S
// Y1<:S
// Y1<:WHY
// Y1<:Y
// Y<:S
// Y<:WHY
// Y<:Y1

type X "X";
type X1 "X";
type X2 "x";
type Y "Why";
type Y1 "Why";
type XES X|X1|X2;
type WHY Y|Y1;
type S string;
