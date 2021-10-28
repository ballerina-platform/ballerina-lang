// EL<:E
// ER1<:E
// ER1<:ER2
// ER2<:E
// ER2<:ER1
type EL error<record {| int[] codes; |}>;
type ER1 error<record {| ER1? e; |}>;
type ER2 error<record {| ER2|() e; |}>;
type E error;
