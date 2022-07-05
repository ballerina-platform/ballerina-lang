// U<:X
// V<:X
// N<:X
// U<:V
// N<:U
// N<:V

type X xml;

type U xml<xml:Element>;

type V xml<xml:Text|xml:Element>;

type N xml<never>;
