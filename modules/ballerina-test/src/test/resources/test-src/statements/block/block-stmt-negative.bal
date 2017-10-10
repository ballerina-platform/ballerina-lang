function testUnreachableStmtInIfFunction1() {
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

function testUnreachableStmtInIfFunction2() {
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

function testUnreachableStmtInWhileBlock() {
    int a = 2;
    while(a < 4) {
        a = a + 1;
        if (a == 3) {
            return;
            string s = "hello";
        }
    }
}

function testCommentAfterReturnStmt()(int) {
    int a = 2;
    if (a > 0) {
        string s1 = "hello if";
        return 1;
        //comment after return stmt
    } else {
        string s2 = "hello else";
    }
    return a;
    //comment after return stmt
    int x = 10;
    return x;
}