type NEVER never;
type INT int;
type FLOAT float;

type FirstFive 0|1|2|3|4|5;
type FFFloat FirstFive|float;

type T1 [int...];
type T2 [int, int, int...];

// @type T3[0] = NEVER
// @type T3[1] = NEVER
// @type T3[1] = NEVER
type T3 T1 & !T2;

type T4 [FirstFive|float...];
type T5 [int, int, FirstFive...];

// @type T6[0] = FFFloat
// @type T6[1] = FFFloat
// @type T6[3] = FFFloat
type T6 T4 & !T5;
