function testOverloading(float a) {
    string dummy = "dummy";
}

function testOverloading(string a, int b) (string) {
    return a + b;
}
