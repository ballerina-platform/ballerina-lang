function invalidByteLiteral() {
    byte b1 = 345;
    byte b2 = -123;
    byte b3 = 256;
    byte b4 = -1;
    byte b5 = 12.3;
    byte b6 = -34.6;
    byte b7 = 0.0;
    byte[] a1 = [1, 2, -2];
    byte[] a2 = [1, 256, 45];
    byte[] a3 = [1, 2.3, 34, -56, 257];
    byte[] a4 = [1, -2.3, 3.4, 4, 46777];

    int x = 45;
    byte y = x;

    float w = 4.0;
    byte z = w;

    string r = "4";
    byte s = r;

    int x1 = -123;
    byte x2 = trap <byte> x1;

    int x3 = 256;
    byte x4 = trap <byte> x3;

    int x5 = 12345;
    byte x6 = trap <byte> x5;
}

function testUnreachableByteMatchStmt3() {
    var result = foo(2);
    int a = result is int ? 333 : (result is byte ? 777 : (result is string[] ? 666 : result));
}

function testUnreachableByteMatchStmt4() {
    var result = foo(2);
    int a = result is int ? 333 : (result is byte ? 777 : (result is string[] ? 666 : result));
}

function foo (int a) returns (byte|int|string[]) {
    if (a == 1) {
        return check trap <byte>12;
    } else if (a == 3) {
        return 267;
    }
    return ["ba", "le"];
}
