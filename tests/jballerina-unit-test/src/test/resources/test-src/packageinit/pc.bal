
import testorg/packageinit.expressions.invocations.pkg.a;
import testorg/packageinit.expressions.invocations.pkg.b as _;

function testInitInvocation() returns (int) {
    return a:getA1();
}

future<int> intFuture = getFuture();

public function getFuture() returns future<int> {
    return start getInt();
}

public function getInt() returns int {
    return 899;
}

public function testGetIntValue() returns int {
    return checkpanic wait intFuture;
}
