function testReturnStmtLocation1() {
    int a = 2;
    if (a > 0) {
        string s1 = "hello if";
    } else {
        string s2 = "hello else";
    }
    return;
}

function testReturnStmtLocation2() returns (int) {
    int a = 2;
    if (a > 0) {
        string s1 = "hello if";
    } else {
        string s2 = "hello else";
    }
    return a;
}

function testReturnStmtLocation3() {
    int a = 2;
    if (a > 0) {
        string s1 = "hello if";
        return;
    } else {
        string s2 = "hello else";
    }
}

function testReturnStmtLocation4() {
    int a = 2;
    while(a < 4) {
        a = a + 1;
        if (a == 3) {
            return;
        }
    }
}

function testCommentAfterReturnStmt() returns (int) {
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
}

function testVariableShadowingBasic() returns int{
    int a = 5;
    int b = 4;

    if (a > 3) {
        int a = 6;
        b = a + b;
    }
    return b;
}


function testVariableShadowingInCurrentScope1(int a) returns int{
    int b = 4;

    if (a > 3) {
        int a = 6;
        b = a + b;
    }
    return b;
}

function testVariableShadowingInCurrentScope2(int a) returns function (float) returns (string){
    int b = 4;

    if (a < 10) {
        int a = 4;
        b = a + b;
    }

    var foo = (float f) => (string) {
        if (a > 8) {
            int a = 6;
            b = a + <int>f + b;
        }
        return "K" + b;
    };
    return foo;
}

function test1() returns int {
    int a = testVariableShadowingBasic();
    return a;
}

function test2() returns int {
    int a = testVariableShadowingInCurrentScope1(4);
    return a;
}

function test3() returns string {
    var foo = testVariableShadowingInCurrentScope2(9);
    string a = foo(3.4);
    return a;
}
