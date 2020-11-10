function test1() {
    int a = 0xFFFFFFFFFFFFFFFF;
    int b = 9999999999999999999;

    int d = -0xFFFFFFFFFFFFFFFF;
    int e = -9999999999999999999;
}

function test2() {
    // This is to test preceding syntax issues.
    int x = 1

    int g = 0672;
    int h = 0912;

    // This is to verify that the compilation continues.
    int y = 1
}

// This is to verify the correct error message
public int a;
