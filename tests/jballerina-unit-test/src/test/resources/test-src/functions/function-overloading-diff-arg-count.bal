function testOverloading(float a) {
    string dummy = "dummy";
}

function testOverloading(string a, int b) returns (string) {
    return a + b.toString();
}
