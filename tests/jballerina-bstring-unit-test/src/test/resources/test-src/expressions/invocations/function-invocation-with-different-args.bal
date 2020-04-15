function testDefaultableParams(int a, int b, int c = 5) returns (int) {
    int x = a + b + c;
    return x;
}