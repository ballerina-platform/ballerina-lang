// J1<:J2
// J1<:J3
// J2<:J1
// J2<:J3
// J3<:J1
// J3<:J2
type J1 [J2,J1]|record {| J1 x; J1 y; |}|()|int|string;
type J2 ()|int|string|[J1,J2]|record {| J2 x; J1 y; |};
type J3 ()|int|string|[J3,J3]|record {| J3 x; J3 y; |};


