type INT int;
type INTFLOAT int|float;
type NEVER never;

type C01 0|1;
type C02 0|2;
type C12 1|2;

type NOTC01 !C01 & int;
 
type T1 int[100000];
type T2 [C01, C01, C01, (int|float)...];


// @type T3[0] = NOTC01
// @type T3[100] = INT
// @type T3[1000] = INT
// @type T3[10000] = INT
// @type T3[99999] = INT
// @type T3[100000] = NEVER
type T3 T1 & !T2;

// @type T4[0] = C01
// @type T4[1] = C01
// @type T4[2] = C01
// @type T4[3] = INTFLOAT
// @type T4[100] = INTFLOAT
// @type T4[1000] = INTFLOAT
// @type T4[100000] = INTFLOAT
type T4 T2 & !T1;
