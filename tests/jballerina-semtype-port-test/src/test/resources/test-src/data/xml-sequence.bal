// E<:P
// E<:Q
// E<:U
// E<:V
// E<:X
// E<:XE
// E<:XEU
// E<:Y
// N<:E
// N<:P
// N<:Q
// N<:T
// N<:U
// N<:V
// N<:X
// N<:XE
// N<:XEU
// N<:XT
// N<:Y
// P<:Q
// P<:X
// P<:XE
// P<:XEU
// P<:Y
// Q<:P
// Q<:X
// Q<:XE
// Q<:XEU
// Q<:Y
// T<:P
// T<:Q
// T<:V
// T<:X
// T<:XE
// T<:XEU
// T<:XT
// T<:Y
// U<:E
// U<:P
// U<:Q
// U<:V
// U<:X
// U<:XE
// U<:XEU
// U<:Y
// V<:P
// V<:Q
// V<:X
// V<:XE
// V<:XEU
// V<:Y
// X<:XEU
// X<:Y
// XE<:P
// XE<:Q
// XE<:X
// XE<:XEU
// XE<:Y
// XEU<:X
// XEU<:Y
// XT<:P
// XT<:Q
// XT<:T
// XT<:V
// XT<:X
// XT<:XE
// XT<:XEU
// XT<:Y
// Y<:X
// Y<:XEU

type X xml;
type Y xml<xml>;
type P xml<xml:Element|xml:Comment|xml:ProcessingInstruction|xml:Text>;
type Q xml<P>;
type U xml<xml:Element>;
type V xml<xml:Text|xml:Element>;
type N xml<never>;
type E xml:Element;
type XE xml<P|E>;
type XEU xml|E;
type T xml:Text;
type XT xml<T>;
