// C<:CTE
// C<:X
// CTE<:X
// E<:CTE
// E<:X
// T<:CTE
// T<:X

type C xml:Comment;
type E xml:Element;
type CTE C|T|E;
type T xml:Text;
type X xml;
