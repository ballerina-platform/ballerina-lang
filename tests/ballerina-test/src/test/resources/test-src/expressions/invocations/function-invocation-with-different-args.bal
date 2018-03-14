function testDefaultableParams(int a, int b, int c = 5) (int) {
    int x = a + b + c;
    return x;
}