var _ = error("");

function testVarNegativeCases() returns (int) {
    string _ = 100;
    var _ = 200;
    error _ = error("Custom error");
    var _ = error("Custom error");
    return 0;
}
