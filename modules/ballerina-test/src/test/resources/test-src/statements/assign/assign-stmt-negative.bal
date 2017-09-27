function testIncompatibleTypeAssign()  {
    boolean b;
    b = 12;
}

function testAssignCountMismatch1() (int, string, int) {
    int a;
    string name;
    int b;

    a, name = testMultiReturnValid();
    return a, name, b;
}

function testAssignCountMismatch2() (int, string, int) {
    int a;
    string name;
    int b;
    int c;

    a, name, b, c = testMultiReturnValid();
    return a, name, b;
}

function testAssignTypeMismatch1() (int, string, int) {
    int a;
    string name;
    int b;

    a, name, b= testMultiReturnInvalid();
    return a, name, b;
}

function testMultiReturnInvalid() (string, string, int) {
    return 5, "john", 6;
}

function testAssignTypeMismatch2() (int, string, int) {
    int a;
    int name;
    int b;

    a, name, b = testMultiReturnValid();
    return a, name, b;
}

function testMultiReturnValid() (int, string, int) {
    return 5, "john", 6;
}

