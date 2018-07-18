function test () {
    // This is to test preceding syntax issues.
    int x = 1

    int a = 0xFFFFFFFFFFFFFFFF;
    int b = 9999999999999999999;
    int c = 07777777777777777777777;
    int d = 0b1111111111111111111111111111111111111111111111111111111111111111;

    int e = -0xFFFFFFFFFFFFFFFF;
    int f = -9999999999999999999;
    int g = -07777777777777777777777;
    int h = -0b1111111111111111111111111111111111111111111111111111111111111111;

    // This is to verify that the compilation continues.
    int y = 1
}
