// N<:R
// N<:X
// T<:R
// T<:X
// N<:T
type R readonly;
type X xml;
type N xml<never>;

type T xml:Text;
