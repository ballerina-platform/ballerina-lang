function testIntAssignStmt(int a) (int) {
    int x;
    x = a;
    return x;
}

function testFloatAssignStmt(float a) (float) {
    float x;
    x = a;
    return x;
}

function testBooleanAssignStmt(boolean a) (boolean) {
    boolean x;
    x = a;
    return x;
}

function testStringAssignStmt(string a) (string) {
    string x;
    x = a;
    return x;
}

function testIntToArrayAssignStmt(int a) (int) {
    int[] arr = [];
    arr[0] = a;
    return arr[0];
}

function testArrayIndexToIntAssignStmt(int[] arr) (int) {
    int a;
    a = arr[0];
    return a;
}

function testMultiReturn() (int, string, int) {
    int a;
    string name;
    int b;

    a, name, b = testMultiReturnInternal();
    return a, name, b;
}

function testMultiReturnInternal() (int, string, int) {
    return 5, "john", 6;
}

function testIntCastFloatStmt (int a) (float) {
    float x;
    //Cannot directly assign int to float, need to convert
    x = <float>a;
    return x;
}

function testBinaryExpressionIntAndFloatStmt (int a) (float) {
    float x;
    x = <float>a;
    return x + a;
}
