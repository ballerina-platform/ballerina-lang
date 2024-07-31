// @type EL < E
// @type ER1 < E
// @type ER1 = ER2
// @type EL <> ER1
// @type ER2 < E
type EL error<record {| int[] codes; |}>;
type ER1 error<record {| ER1? e; |}>;
type ER2 error<record {| ER2|() e; |}>;
type E error;
