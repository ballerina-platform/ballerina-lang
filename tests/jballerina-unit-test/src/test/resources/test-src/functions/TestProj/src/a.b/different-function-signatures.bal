
import c.d;

public function testInvokePkgFunctionInMixOrder() returns [int, float, string, int, string, int[]] {
    return d:functionWithAllTypesParams(e="Bob", a=10, d=30, c="Alex", b=20.0);
}

public function testInvokePkgFunctionInOrderWithRestParams() returns [int, float, string, int, string, int[]] {
    return d:functionWithAllTypesParams(10, 20.0, "Alex", 30, "Bob", 40, 50, 60);
}

public function testInvokePkgFunctionWithRequiredArgsOnly() returns  [int, float, string, int, string, int[]] {
    return d:functionWithAllTypesParams(10, 20.0);
}
