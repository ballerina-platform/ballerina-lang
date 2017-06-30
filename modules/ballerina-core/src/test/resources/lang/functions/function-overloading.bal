function testOverloading(string a, int b) (string) {
    return a + b;
}

function testOverloading(int a, int b) (int) {
    return a + b;
}