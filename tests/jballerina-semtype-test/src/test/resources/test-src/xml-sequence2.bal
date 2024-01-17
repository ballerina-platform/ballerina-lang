// P<:X
// X<:P
// P<:Q
// Q<:P
// X<:Q
// Q<:X

type Q xml<P>;

type P xml<xml:Element|xml:Comment|xml:ProcessingInstruction|xml:Text>;

type X xml;
