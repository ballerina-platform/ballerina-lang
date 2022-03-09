const THREE = 3;
const FOUR = 4;
type Float float;
type Int int;
type ISF int|string|float;
type IF int|float;
type SF string|float;
type T02 0|2;
type T12 1|2;

type T1 [int, string, float...];

// @test T[THREE] = F
// @test T[FOUR] = F
// @test T[Int] = ISF
// @test T[T02] = IF
// @test T[T12] = SF
type T T1 & any[4];
