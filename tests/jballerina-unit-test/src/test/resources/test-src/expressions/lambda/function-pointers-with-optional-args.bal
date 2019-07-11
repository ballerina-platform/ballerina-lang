function funcWithRestParams (int a, int b, int... c) returns [int, int, int[]] {
    return [a, b, c];
}

function funcWithNamedParams (int a, int b, string c="John") returns [int, int, string] {
    return [a, b, c];
}

function testFunctionPointerAssignmentWithRestParams() returns [int, int, int[]]{
    function (int, int, int[]) returns [int, int, int[]] func = funcWithRestParams;
    return func(1, 2, [3, 4]);
}

function testFunctionPointerAssignmentWithNamedParams() returns [int, int, string]{
    function (int, int, string) returns [int, int, string] func = funcWithNamedParams;
    return func(1, 2, "Alex");
}
