type INT int;
type STRING string;
type STROPT string?;
type NIL ();

type C01 0|1;
type C02 0|2;
type C12 1|2;

type T1 [int?, string...];
type T2 [int, (string|float)...];
// @type T3[0] = NIL
// @type T3[1] = STRING
// @type T3[2] = STRING
// @type T3[100] = STRING
// @type T3[C01] = STROPT
// @type T3[C02] = STROPT
// @type T3[C12] = STRING
// @type T3[INT] = STROPT
type T3 T1 & !T2;
