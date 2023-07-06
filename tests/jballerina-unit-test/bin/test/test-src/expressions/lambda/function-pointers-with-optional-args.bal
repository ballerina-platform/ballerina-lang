function funcWithNamedParams (int a, int b, string c="John") returns [int, int, string] {
    return [a, b, c];
}

function testFunctionPointerAssignmentWithNamedParams() returns [int, int, string]{
    function (int, int, string) returns [int, int, string] func = funcWithNamedParams;
    return func(1, 2, "Alex");
}
