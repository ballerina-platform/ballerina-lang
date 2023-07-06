// N<:V
// N<:X
// E<:V
// E<:X
// V<:X

type N xml<never>;

type E xml:Element;

type V xml<xml:Text|xml:Element>;

type X xml;
