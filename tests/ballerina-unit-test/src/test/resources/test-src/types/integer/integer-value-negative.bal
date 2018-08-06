function test () {
    // This is to test preceding syntax issues.
    int x = 1

    int a = 0xFFFFFFFFFFFFFFFF;
    int b = 9999999999999999999;
    int c = 0b1111111111111111111111111111111111111111111111111111111111111111;

    int d = -0xFFFFFFFFFFFFFFFF;
    int e = -9999999999999999999;
    int f = -0b1111111111111111111111111111111111111111111111111111111111111111;

    // This is to verify that the compilation continues.
    int y = 1
}
