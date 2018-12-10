function returnTaintedValue() returns @tainted int {
    return 45;
}

function testSensitiveArg(@sensitive int intArg) {
    int c = intArg;
}

public function cloneTaintedValue() {
    int x = returnTaintedValue();
    int y = x.clone();
    testSensitiveArg(y);
}
