var _ = error("");
error _ = "not an error";
error _ = error("Custom error");
int _ = 100;

[int, int|error] m = [1, error("")];

record {|
    int i;
    error e;
|} n = {
    i: 1,
    e: error("")
};

var [x, _] = m;
var {i, e: _} = n;

function testVarNegativeCases() returns (int) {
    string _ = 100;
    var _ = 200;
    error _ = error("Custom error");
    var _ = error("Custom error");
    error _ = "not an error";

    var [x2, _] = m;
    var {i: i2, e: _} = n;

    (int|error)[2] o = [1, 2];
    var [_, y] = o;

    return 0;
}
