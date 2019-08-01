function funcWithNamedParams (int a, int b, int c=4) returns [int, int, int[]] {
    return [a, b, c];
}

function test1() returns [int, int, int[]]{
    function (int, int, int c = 6) returns [int, int, int[]] func = funcWithRestArgs;
    return func(1, 2);
}
