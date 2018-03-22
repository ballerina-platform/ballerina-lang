function testIntAssignStmt(int a) returns int {
    int x;
    x = a;
    return x;
}

function testFloatAssignStmt(float a) returns float {
    float x;
    x = a;
    return x;
}

function testBooleanAssignStmt(boolean a) returns boolean {
    boolean x;
    x = a;
    return x;
}

function testStringAssignStmt(string a) returns string {
    string x;
    x = a;
    return x;
}

function testIntToArrayAssignStmt(int a) returns int {
    int[] arr = [];
    arr[0] = a;
    return arr[0];
}

function testArrayIndexToIntAssignStmt(int[] arr) returns int {
    int a;
    a = arr[0];
    return a;
}

function testMultiReturn() returns (int, string, int) {
    int a;
    string name;
    int b;

    (a, name, b) = testMultiReturnInternal();
    return (a, name, b);
}

function testMultiReturnInternal() returns (int, string, int) {
    return (5, "john", 6);
}

function testIntCastFloatStmt (int a) returns float {
    float x;
    //Cannot directly assign int to float, need to convert
    x = <float>a;
    return x;
}

function testBinaryExpressionIntAndFloatStmt (int a) returns float {
    float x;
    x = <float>a;
    return x + a;
}
