// Z<:F
// Z<:ZO
// Z<:ZOT
// Z<:ZT
// ZO<:F
// ZO<:ZOT
// ZOT<:F
// ZT<:F
// ZT<:ZOT
// O<:F
// O<:ZO
// O<:ZOT
// T<:F
// T<:ZOT
// T<:ZT

type Z 0.0;
type O 1.0;
type T -2.0;

type ZOT Z|O|T;
type ZO Z|O;
type ZT Z|T;

type F float;
