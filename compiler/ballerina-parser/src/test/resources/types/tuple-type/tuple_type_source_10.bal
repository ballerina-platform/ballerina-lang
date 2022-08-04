type T1 [@annot {} int, @annot {} string, @annot T];

type T2 [int, @annot {} string];

function foo(string inMsg) returns [@annot int, @annot string] {
    return [1, "s"];
}

function bar() returns [@annot {} int, [@annot {} int, string]] {
    return [1, [1, "s"]];
}
