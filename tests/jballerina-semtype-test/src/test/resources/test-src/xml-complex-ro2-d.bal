// XUX<:RX
// RX<:XUX

type X xml;
type RX readonly & X;

type T xml:Text;
type E readonly & xml:Element;
type P readonly & xml:ProcessingInstruction;
type C readonly & xml:Comment;
// Compiler crashes for above intersections.
// BUG #34760
type XE xml<E>;
type XP xml<P>;
type XC xml<C>;

type UX XE|XP|XC|T;

type XUX xml<UX>;
