// C<:CS
// E<:ENS
// E<:ES
// ENS<:ES
// ES<:ENS
// N<:CS
// N<:ENS
// N<:ES
// N<:NS
// N<:PS
// N<:T
// N<:TS
// NS<:CS
// NS<:ENS
// NS<:ES
// NS<:N
// NS<:PS
// NS<:T
// NS<:TS
// P<:PS
// T<:TS
// TS<:T

type N xml<never>;
type NS xml<N>;
type E xml:Element;
type T xml:Text;
type C xml:Comment;
type P xml:ProcessingInstruction;
type ES xml<xml:Element>;
type CS xml<xml:Comment>;
type TS xml<xml:Text>;
type PS xml<xml:ProcessingInstruction>;
type ENS xml<xml:Element|never>;
