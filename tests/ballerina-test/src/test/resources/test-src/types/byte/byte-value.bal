byte globalByte;

function testByteValue() returns byte {
    byte a;
    a = 34;
    return a;
}

function testByteValueSpace() returns byte {
    byte a;
    a = 234;
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

function testIntToByteCast(int b) returns byte {
    byte a = check <byte> b;
    return a;
}

function testByteToIntCast(byte a) returns int {
    int b = <int>a;
    return b;
}

function testIntToByteExplicitCast(int b) returns byte {
    byte a = check <byte> b;
    return a;
}

function testIntToByteConversion(int b) returns byte {
    var c = check <byte> b;
    return c;
}

function testByteToIntConversion(byte b) returns int {
    int c = <int> b;
    return c;
}

function testSafeCasting() returns int {
  any abc = byteReturn();
  int val = check <int> abc;
  return val;
}

function byteReturn() returns any {
  byte val = 6;
  return val;
}

function testAnyToByteCasting() returns byte {
  any val = 45;
  byte i = check <byte> val;
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

function testByteOrIntMatch1() returns byte|int {
    match byteOrInt(true) {
        byte c => {
            return c;
        }
        int b => {
            return b;
        }
    }
}

function testByteOrIntMatch2() returns byte|int {
    match byteOrInt(false) {
        byte c => {
            return c;
        }
        int b => {
            return b;
        }
    }
}

function byteOrInt(boolean boo) returns (int|byte) {
    if (boo) {
        return check <byte>12;
    }
    return 266;
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

function testBitwiseAndOperator(byte a, byte b, int i, int j) returns (byte, int, int, int, int){
    byte b1 = a & b;
    int r1 = <int>(a & b);
    int r2 = <int>a & i;
    int r3 = i & j;
    int r4 = <int>a & i & <int>b & j;
    return (b1, r1, r2, r3, r4);
}

function testBitwiseOrOperator(byte a, byte b, int i, int j) returns (byte, int, int, int, int){
    byte b1 = a | b;
    int r1 = <int>(a | b);
    int r2 = <int>a | i;
    int r3 = i | j;
    int r4 = <int>a | i | <int>b | j;
    return (b1, r1, r2, r3, r4);
}

function testBitwiseXorOperator(byte a, byte b, int i, int j) returns (byte, int, int, int, int){
    byte b1 = a ^ b;
    int r1 = <int>(a ^ b);
    int r2 = <int>a ^ i;
    int r3 = i ^ j;
    int r4 = <int>a ^ i ^ <int>b ^ j;
    return (b1, r1, r2, r3, r4);
}

function testBitwiseRightShiftOperator(byte a, byte b, int i, int j) returns (int, int){
    int r1 = a >> b;
    int r2 = i >> j;
    return (r1, r2);
}

function testBitwiseLeftShiftOperator(byte a, byte b, int i, int j) returns (int, int){
    int r1 = a << b;
    int r2 = i << j;
    return (r1, r2);
}
