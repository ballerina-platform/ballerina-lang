var _ = error("");
error _ = "not an error";
error _ = error("Custom error");
int _ = 100;

function testVarNegativeCases() returns (int) {
    string _ = 100;
    var _ = 200;
    error _ = error("Custom error");
    var _ = error("Custom error");
    error _ = "not an error";
    return 0;
}
