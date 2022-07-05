// Y<:X
// X<:Y
// P<:X
// X<:P
// P<:Y
// Y<:P

type X xml;

type Y xml<xml>;

type P xml<xml:Element|xml:Comment|xml:ProcessingInstruction|xml:Text>;
