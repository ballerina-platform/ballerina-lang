const THREE = 3;
type F float;
type Int int;
type ISF int|string|float;
type IF int|float;
type SF string|float;
type T02 0|2;
type T12 1|2;

// @type T[THREE] = F
// @type T[Int] = ISF
// @type T[T02] = IF
// @type T[T12] = SF
type T [int, string, float...];
