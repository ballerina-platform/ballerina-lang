// E1<:E
// E2<:E
// E2<:E4
// E3<:E
// E3<:E1
// E3<:E2
// E3<:E4
// E4<:E
// E4<:E2

type E error;

type E1 distinct error;

type E2 error<R1>;

type E3 E1 & E2;

type E4 error<R1>;

type R1 record {};
