// T2<:T1
// T2<:T4
// T3<:T1
// T3<:T2
// T3<:T4

type T1 [int...];
type T2 [int,int...];

type T4 [int?,int?...];
type T3 [int, int, int, int...];
