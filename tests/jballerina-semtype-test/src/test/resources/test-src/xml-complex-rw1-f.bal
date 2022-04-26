// X<:UX
// This relation is not correct
// BUG: #34779

type X xml;
type XE xml<xml:Element>;
type XP xml<xml:ProcessingInstruction>;
type XC xml<xml:Comment>;
type UX XE|XP|XC|xml:Text;
