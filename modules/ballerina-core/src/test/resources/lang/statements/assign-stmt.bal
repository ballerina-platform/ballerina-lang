function testIntAssignStmt(int a) (int) {
    int x;
    x = a;
    return x;
}

function testLongAssignStmt(long a) (long) {
    long x;
    x = a;
    return x;
}

function testFloatAssignStmt(float a) (float) {
    float x;
    x = a;
    return x;
}

function testDoubleAssignStmt(double a) (double) {
    double x;
    x = a;
    return x;
}

function testBooleanAssignStmt(boolean a) (boolean) {
    boolean x;
    x = a;
    return x;
}

function testStringAssignStmt(string a) (int) {
    string x;
    x = a;
    return x;
}

function testIntToArrayAssignStmt(int a) (int) {
    int[] arr;
    arr[0] = a;
    return arr[0];
}

function testArrayIndexToIntAssignStmt(int[] arr) (int) {
    int a;
    a = arr[0];
    return a;
}



