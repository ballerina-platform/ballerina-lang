// @type T1 < T2
// @type T1 <> T3
type T1 1|1.0|"foo";
type T2 int|float|string;
type T3 int|string;

// @type T4 = OneFoo
type T4 T3 & T1;
type OneFoo 1|"foo";

// @type T5 = One
// @type DoubleOne = One
// @type AnotherDoubleOne = One
type T5 1|1;
type One 1;
type DoubleOne One|One;
type AnotherDoubleOne One|1;
