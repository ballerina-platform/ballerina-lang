function getFunctionPointer() returns (function(int) returns float) {
    return test;
}

function test(int x) returns float {
    float f = 0.0;
    return f + x;
}
