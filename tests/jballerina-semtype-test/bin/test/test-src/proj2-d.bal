type I int;
type I2 int?;

// @type A[I] = I2
// @type A[0] = I
// @type A[1] = I
type A [int?, int?] & ![(), ()] & ![int, ()] & ![(), int];
