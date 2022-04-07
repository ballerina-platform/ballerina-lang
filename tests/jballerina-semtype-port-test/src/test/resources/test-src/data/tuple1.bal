// T1<:T2
// T3<:T4
// T4<:T3
type T1 [int];
type T2 [int?];
type T3 [int, int?];
type T4 [int, int] | [int, ()];