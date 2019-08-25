
import expressions.invocations.pkg.a;
import expressions.invocations.pkg.b as _;

function testInitInvocation() returns (int) {
    return a:getA1();
}

future<int> intFuture = start getInt();

public function getInt() returns int {
    return 899;
}

public function testGetIntValue() returns int {
    return wait intFuture;
}
