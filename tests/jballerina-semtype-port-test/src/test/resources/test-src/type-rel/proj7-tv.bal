type NEVER never;
type BOOL boolean;
type BOOLOPT boolean?;
type INT int;
type NIL ();
type INTOPT int?;
type STROPT string?;
type INTSTROPT int|string?;
type INT_FLOAT_BOOL_OPT int|float|boolean?;
type STR_FLOAT_BOOL_OPT string|float|boolean?;
type INT_STR_FLOAT_BOOL_OPT int|string|float|boolean?;

type C01 0|1;
type C02 0|2;
type C12 1|2;

type T1 [int?, string?, float|boolean...];
type T2 [int, (string|float)...];
// @type T3[0] = INTOPT
// @type T3[1] = STROPT
// @type T3[C01] = INTSTROPT
// @type T3[C02] = INT_FLOAT_BOOL_OPT
// @type T3[C12] = STR_FLOAT_BOOL_OPT
// @type T3[INT] = INT_STR_FLOAT_BOOL_OPT
type T3 T1 & !T2;
