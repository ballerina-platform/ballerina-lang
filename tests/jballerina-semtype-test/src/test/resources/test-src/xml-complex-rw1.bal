// XE<:UX
// XP<:UX
// XC<:UX

// XE<:XUX
// XP<:XUX
// XC<:XUX
// UX<:XUX

type XE xml<xml:Element>;
type XP xml<xml:ProcessingInstruction>;
type XC xml<xml:Comment>;
type UX XE|XP|XC|xml:Text;
type XUX xml<UX>;
