byte globalByte;

function testByteValue() (byte){
    byte a;
    a = 34;
    return a;
}

function testByteParam(byte b) (byte) {
    byte a;
    a = b;
    return a;
}

function testGlobalByte(byte b) (byte){
    globalByte = b;
    return globalByte;
}

function testIntToByteCast(int b) (byte){
    byte a = b;
    return a;
}

function testByteToIntCast(byte a) (int) {
    int b = a;
    return b;
}

function testByteToAnyCast(byte i) (any, byte){
    byte a = i;
    type typeOfa = (typeof a);
    return typeOfa, a;
}

function testByteArrayToAny(byte[] caIn) (any, any) {
    any caOut = caIn;
    any typeOfca = (typeof caIn);
    return typeOfca, caOut;
}

function testIntToByteExplicitCast(int b) (byte) {
    byte a = (byte)b;
    return a;
}

function testIntToByteConversion(int b) (byte){
    var c = <byte>b;
    return c;
}

function testByteToIntConversion(byte b) (int){
    byte c = <int>b;
    return c;
}


function testByteArray() (byte[]){
    byte[] ba = [12, 24, 7];
    return ba;
}

function testByteArrayAssignment(byte[] cArrayIn) (byte[]) {
    byte[] cArray;
    cArray = cArrayIn;
    return cArray;
}

function testByteArrayZeroLength() (int) {
    byte[] a = [];
    return (lengthof a);
}

function testByteArrayLength() (int) {
    byte[] a = [4, 6, 27, 75];
    return (lengthof a);
}

function testByteArrayIncreaseSize() (int){
    byte[] a = [4, 6, 27, 75];
    a[9] = 56;
    return (lengthof a);
}

function testByteArrayOfArray() (int, int) {
    byte[][] aa = [[4, 6, 27, 75], [3, 7, 1], [5, 32, 98]];
    int a = (lengthof aa);
    int b = (lengthof aa[0]);
    return a, b;
}

function testByteBinaryOperation(byte a, byte b, byte c) (boolean, boolean) {
    boolean b1 = (a == b);
    boolean b2 = (a == c);
    return b1, b2;
}

function testByteBinaryNotEqualOperation(byte a, byte b, byte c) (boolean, boolean) {
    boolean b1 = (a != b);
    boolean b2 = (a != c);
    return b1, b2;
}

function testWorkerWithByteVariable() {
   worker w1 {
     int a = 10;
     byte b = 3;
     a, b -> w2;
     b <- w2;
   }
   worker w2 {
     int a = 0;
     int b = 15;
     byte c = 7;
     a, c <- w1;
     c -> w1;
   }
}
