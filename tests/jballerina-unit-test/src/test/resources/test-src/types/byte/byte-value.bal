byte globalByte = 0;

function testByteValue() returns byte {
    byte a = 0;
    a = 34;
    return a;
}

function testByteValueSpace() returns byte {
    byte a = 0;
    a = 234;
    return a;
}

function testByteDefaultValue() returns byte {
    byte a = 0;
    return a;
}

function testByteParam(byte b) returns byte {
    byte a = 0;
    a = b;
    return a;
}

function testGlobalByte(byte b) returns byte {
    globalByte = b;
    return globalByte;
}

function testIntToByteCast(int b) returns byte|error {
    byte a = check trap <byte> b;
    return a;
}

function testByteToIntCast(byte a) returns int {
    int b = <int>a;
    return b;
}

function testIntToByteExplicitCast(int b) returns byte|error {
    byte a = check trap <byte> b;
    return a;
}

function testIntToByteConversion(int b) returns byte|error {
    var c = check trap <byte> b;
    return c;
}

function testByteToIntConversion(byte b) returns int {
    int c = <int> b;
    return c;
}

function testSafeCasting() returns int|error {
  any abc = byteReturn();
  int val = <int>abc;
  return val;
}

function byteReturn() returns any {
  byte val = 6;
  return val;
}

function testAnyToByteCasting() returns byte|error {
  any val = 45;
  byte i = <byte>val;
  return i;
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
    return (a.length());
}

function testByteArrayLength() returns int {
    byte[] a = [4, 6, 27, 75];
    return (a.length());
}

function testByteArrayIncreaseSize() returns int {
    byte[] a = [4, 6, 27, 75];
    a[9] = 56;
    return (a.length());
}

function testByteArrayOfArray() returns [int, int] {
    byte[][] aa = [[4, 6, 27, 75], [3, 7, 1], [5, 32, 98]];
    int a = (aa.length());
    int b = (aa[0].length());
    return [a, b];
}

function testByteBinaryOperation(byte a, byte b, byte c) returns [boolean, boolean] {
    boolean b1 = (a == b);
    boolean b2 = (a == c);
    return [b1, b2];
}

function testByteArrayIteration(byte[] b) returns byte[] {
    byte[] a = [];
    foreach var i in b {
        a[a.length()] = i;
    }
    return a;
}

function testByteBinaryNotEqualOperation(byte a, byte b, byte c) returns [boolean, boolean] {
    boolean b1 = (a != b);
    boolean b2 = (a != c);
    return [b1, b2];
}

function testByteOrIntMatch1() returns byte|int|string[]|Foo|error {
    byte|int|string[]|Foo|error result = byteOrInt(1);
    if (result is byte) {
        return result;
    } else if (result is int) {
        return result;
    } else if (result is string[]) {
        return result;
    } else if (result is Foo) {
        return result;
    } else {
        return result;
    }
}

function testByteOrIntMatch2() returns byte|int|string[]|Foo|error {
    byte|int|string[]|Foo|error result = byteOrInt(2);
    if (result is byte) {
        return result;
    } else if (result is int) {
        return result;
    } else if (result is string[]) {
        return result;
    } else if (result is Foo) {
        return result;
    } else {
        return result;
    }
}

function testByteOrIntMatch3() returns int {
    var result = byteOrInt(1);
    int x = result is byte ? 456 : (result is int ? -123 : (result is string[] ? 789 : (result is Foo ? 8765 : 1135)));
    return x;
}

function testByteOrIntMatch4() returns int {
    var result = byteOrInt(2);
    int x = result is byte ? 456 : (result is int ? -123 : (result is string[] ? 789 : (result is Foo ? 8765 : 1135)));
    return x;
}

function byteOrInt(int a) returns byte|int|string[]|Foo|error {
    if (a == 1) {
        return check trap <byte>12;
    } else if (a == 2) {
        return 266;
    } else if (a == 3) {
        return ["DD", "GG"];
    }
    Foo foo = { name: "bar" };
    return foo;
}

type Foo record {
    string name;
};

function testWorkerWithByteVariable() {
  worker w1 {
    byte a = 10;
    byte b = 12;
    a -> w2;
    b = <- w2;
  }
  worker w2 {
    byte a = 0;
    byte b = 15;
    a = <- w1;
    b -> w1;
  }
}

function testBitwiseAndOperator(byte a, byte b, int i, int j) returns [byte, int, int, int, int]{
    byte b1 = a & b;
    int r1 = <int>(a & b);
    int r2 = <int>a & i;
    int r3 = i & j;
    int r4 = <int>a & i & <int>b & j;
    return [b1, r1, r2, r3, r4];
}

function testBitwiseOrOperator(byte a, byte b, int i, int j) returns [byte, int, int, int, int]{
    byte b1 = a | b;
    int r1 = <int>(a | b);
    int r2 = <int>a | i;
    int r3 = i | j;
    int r4 = <int>a | i | <int>b | j;
    return [b1, r1, r2, r3, r4];
}

function testBitwiseXorOperator(byte a, byte b, int i, int j) returns [byte, int, int, int, int]{
    byte b1 = a ^ b;
    int r1 = <int>(a ^ b);
    int r2 = <int>a ^ i;
    int r3 = i ^ j;
    int r4 = <int>a ^ i ^ <int>b ^ j;
    return [b1, r1, r2, r3, r4];
}

function testBitwiseRightShiftOperator1(byte a, byte b, int i, int j) returns [byte, int, byte]{
    byte r1 = a >> b;
    int r2 = i >> j;
    byte r3 = a >> j;
    return [r1, r2, r3];
}

function testBitwiseRightShiftOperator2(byte a, byte b, int i, int j) returns [int, int, int]{
    int r1 = <int> (a >> b);
    int r2 = i >> j;
    int r3 = <int> (a >> j);
    return [r1, r2, r3];
}

function testBitwiseLeftShiftOperator1(byte a, byte b, int i, int j) returns [int, int, int]{
    int r1 = a << b;
    int r2 = i << j;
    int r3 = a << j;
    return [r1, r2, r3];
}

function testBitwiseLeftShiftOperator2(byte a, byte b, int i, int j) returns [int, int, int]{
    int r1 = <int> (a << b);
    int r2 = i << j;
    int r3 = <int> (a << j);
    return [r1, r2, r3];
}

function testBitwiseUnsignedRightShiftOperator(byte a, int i, int j) returns [int, int]{
    int r1 = i >>> j;
    int r2 = i >>> a;
    return [r1, r2];
}

function testByteShift() returns int {
    byte a = 129;
    int c = a << 1;
    int d = c >> 1;
    return d;
}

function testBitwiseNotOperator(byte b, int i) returns [byte, int] {
    byte a = ~b;
    int j = ~i;
    return [a, j];
}

function testBitwiseOperatorPrecedence1(byte a, byte b, byte c) returns byte {
    byte d = ~a & b >> c;
    return d;
}

function testBitwiseOperatorPrecedence2(byte a, byte b, byte c) returns byte {
    byte d = b & ~a >> c;
    return d;
}

function testBitwiseOperatorPrecedence3(byte a, byte b, byte c) returns byte {
    byte d = b >> c & ~a;
    return d;
}

function testBitwiseOperatorPrecedence4(int a, int b, int c) returns int {
    int d = ~a & b >> c;
    return d;
}

function testBitwiseOperatorPrecedence5(int a, int b, int c) returns int {
    int d = b & ~a >> c;
    return d;
}

function testBitwiseOperatorPrecedence6(int a, int b, int c) returns int {
    int d = b >> c & ~a;
    return d;
}
