function testUnreachableStmtInIfBlock()(int) {
    int a = 2;
    if (a > 0) {
      string s1 = "hello if";
      return a;
      int b = 10;
    } else {
      string s2 = "hello else";
    }
    return a;
}