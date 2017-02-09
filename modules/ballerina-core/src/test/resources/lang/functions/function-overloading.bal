function testOverloading() {
    string dummy = "dummy";
}

function testOverloading(string a) (string) {
    return a;
}

function testOverloading(int a) (int) {
    return a;
}

function testOverloading(long a) {
    string dummy = "dummy";
}

function testOverloading(string a, int b) (string) {
    return a + b;
}

function testOverloading(int a, int b) (int) {
    return a + b;
}