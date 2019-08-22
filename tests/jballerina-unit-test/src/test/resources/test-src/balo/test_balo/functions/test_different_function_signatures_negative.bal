import testorg/foo;

function testInvokeFunctionInMixOrder1() returns [int, float, string, int, string, int[]] {
    return foo:functionWithAllTypesParams(10, e="Bob", 20.0, c="Alex", 40, 50);
}

function testInvokeFunctionInMixOrder2() returns [int, float, string, int, string, int[]] {
    int[] array = [40, 50, 60];
    return foo:functionWithAllTypesParams(10, e="Bob", 20.0, c="Alex", ...array, d=30);
}

function funcInvocAsRestArgs() returns [int, float, string, int, string, int[]] {
    return foo:functionWithAllTypesParams(10, 20.0, c="Alex", d=30, e="Bob", ...getIntArray());
}

function testInvokeFunctionWithRequiredAndRestArgs() returns [int, float, string, int, string, int[]] {
    return foo:functionWithAllTypesParams(10, 20.0, 40, 50, 60);
}



