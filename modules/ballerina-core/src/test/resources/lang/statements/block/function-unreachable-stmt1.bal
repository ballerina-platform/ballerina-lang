function testUnreachableStmtInIfFunction() {
    int a = 2;
    if (a > 0) {
       string s1 = "hello if";
    } else {
       string s2 = "hello else";
    }
    return;
    if(a > 10) {
       int number = 10;
    }
    int i = 9;
}