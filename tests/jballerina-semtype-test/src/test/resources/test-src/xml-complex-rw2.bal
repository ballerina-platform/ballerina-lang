// XUX<:X
// X<:XUX

// XE<:X
// XP<:X
// XC<:X

// XE<:XUX
// XP<:XUX
// XC<:XUX

type X xml;
type XE xml<xml:Element>;
type XP xml<xml:ProcessingInstruction>;
type XC xml<xml:Comment>;
type XUX xml<XE|XP|XC|xml:Text>;
