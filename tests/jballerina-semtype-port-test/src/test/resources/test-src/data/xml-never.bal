// C<:CS
// CS<:C
// E<:ENS
// E<:ES
// ENS<:E
// ENS<:ES
// ES<:E
// ES<:ENS
// N<:C
// N<:CS
// N<:E
// N<:ENS
// N<:ES
// N<:NS
// N<:P
// N<:PS
// N<:T
// N<:TS
// NS<:C
// NS<:CS
// NS<:E
// NS<:ENS
// NS<:ES
// NS<:N
// NS<:P
// NS<:PS
// NS<:T
// NS<:TS
// P<:PS
// PS<:P
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
