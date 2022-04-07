// C<:EC
// C<:X
// E<:EC
// E<:ET
// E<:X
// EC<:X
// ET<:X

type E xml<xml:Element>;
type C xml<xml:Comment>;
type EC xml<xml:Comment|xml:Element>;
type ET xml<xml:Element|xml:Text>;
type X xml;
