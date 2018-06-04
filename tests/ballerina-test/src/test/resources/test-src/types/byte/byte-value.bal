byte globalByte;

function testByteValue() returns byte {
    byte a;
    a = 34;
    return a;
}

function testByteDefaultValue() returns byte {
    byte a;
    return a;
}

function testByteParam(byte b) returns byte {
    byte a;
    a = b;
    return a;
}

function testGlobalByte(byte b) returns byte {
    globalByte = b;
    return globalByte;
}

function testByteAddition() returns byte {
    byte a = 14;
    byte b = 23;
    return (a + b);
}

function testIntToByteCast(int b) returns byte {
    byte a = <byte> b;
    return a;
}

function testByteToIntCast(byte a) returns int {
    int b = a;
    return b;
}

function testIntToByteExplicitCast(int b) returns byte {
    byte a = <byte> b;
    return a;
}

function testIntToByteConversion(int b) returns byte {
    var c = <byte> b;
    return c;
}

function testByteToIntConversion(byte b) returns int {
    int c = <int> b;
    return c;
}


function testByteArray() returns byte[] {
    byte[] ba = [12, 24, 7];
    return ba;
}

function testByteArrayAssignment(byte[] cArrayIn) returns byte[] {
    byte[] cArray;
    cArray = cArrayIn;
    return cArray;
}

function testByteArrayZeroLength() returns int {
    byte[] a = [];
    return (lengthof a);
}

function testByteArrayLength() returns int {
    byte[] a = [4, 6, 27, 75];
    return (lengthof a);
}

function testByteArrayIncreaseSize() returns int {
    byte[] a = [4, 6, 27, 75];
    a[9] = 56;
    return (lengthof a);
}

function testByteArrayOfArray() returns (int, int) {
    byte[][] aa = [[4, 6, 27, 75], [3, 7, 1], [5, 32, 98]];
    int[][] ab = [[4, 6, 27, 75], [3, 7, 1], [5, 32, 98]];
    int a = (lengthof aa);
    int b = (lengthof aa[0]);
    return (a, b);
}

function testByteBinaryOperation(byte a, byte b, byte c) returns (boolean, boolean) {
    boolean b1 = (a == b);
    boolean b2 = (a == c);
    return (b1, b2);
}

function testByteBinaryNotEqualOperation(byte a, byte b, byte c) returns (boolean, boolean) {
    boolean b1 = (a != b);
    boolean b2 = (a != c);
    return (b1, b2);
}

function testWorkerWithByteVariable() {
  worker w1 {
    byte a = 10;
    byte b = 12;
    a -> w2;
    b <- w2;
  }
  worker w2 {
    byte a = 0;
    byte b = 15;
    a <- w1;
    b -> w1;
  }
}
