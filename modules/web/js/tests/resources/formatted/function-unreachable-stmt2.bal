function testUnreachableStmtInIfFunction() {
    int a = 2;
    int b;
    if (a > 0) {
       string s1 = "hello if";
    } else {
       string s2 = "hello else";
    }
    b = 7;
    return;
    int i = 9;
}