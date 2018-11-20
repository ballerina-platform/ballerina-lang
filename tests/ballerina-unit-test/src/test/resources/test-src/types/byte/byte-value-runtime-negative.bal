function invalidByteLiteral1() {
    int a = -12;
    byte b = check <byte>a;
}


function invalidByteLiteral2() {
    int c = -257;
    byte d = check <byte>c;
}


function invalidByteLiteral3() {
    int e = 12345;
    byte f = check <byte>e;
}
