type T1 [@annot {} int, @annot {} string];

type T2 [int, @annot {} string];

function foo (string inMsg) returns [@annot int, @annot string] {
           return [1, "s"];
}
