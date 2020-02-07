function invalidByteLiteral1() returns error? {
    int a = -12;
    byte b = check trap <byte>a;
    return;
}


function invalidByteLiteral2() returns error? {
    int c = -257;
    byte d = check trap <byte>c;
    return;
}


function invalidByteLiteral3() returns error? {
    int e = 12345;
    byte f = check trap <byte>e;
    return;
}
