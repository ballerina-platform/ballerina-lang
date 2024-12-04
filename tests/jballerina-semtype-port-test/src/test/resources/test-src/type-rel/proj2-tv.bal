type I int;

// -@type A[I] = I
// -@type A[0] = I
// -@type A[1] = I
type A [int?, int?] & ![(), ()] & ![int, ()] & ![(), int];
