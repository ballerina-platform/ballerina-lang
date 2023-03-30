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

type testType1 int:Signed32;
type testType2 int:Signed16;
type testType3 int:Signed8;

function testStaticTypeOfUnaryExpr() {
    int:Signed8 a = -128;
    int:Signed8 _ = -a;
    int:Signed16 b = -32768;
    int:Signed16 _ = -b;
    int:Signed32 c = -2147483648;
    int:Signed32 _ = -c;

    testType1 d = -2147483648;
    int:Signed32 _ = -d;
    testType2 e = -32768;
    int:Signed16 _ = -e;
    testType3 f = -128;
    int:Signed8 _ = -f;
}
