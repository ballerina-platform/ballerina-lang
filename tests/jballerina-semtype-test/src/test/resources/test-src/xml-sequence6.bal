// X<:P
// P<:X
// E<:X
// X<:XE
// XE<:X
// E<:P
// P<:XE
// XE<:P
// E<:XE

type XE xml<P|E>;

type X xml;

type P xml<xml:Element|xml:Comment|xml:ProcessingInstruction|xml:Text>;

type E xml:Element;
