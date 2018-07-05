function testIncompatibleTypeAssign () {
    boolean b;
    b = 12;
}

function testAssignCountMismatch1 () returns (int, string, int) {
    int a;
    string name;
    int b;

    (a, name) = testMultiReturnValid();
    return (a, name, b);
}

function testAssignCountMismatch2 () returns (int, string, int) {
    int a;
    string name;
    int b;
    int c;

    (a, name, b, c) = testMultiReturnValid();
    return (a, name, b);
}

function testAssignTypeMismatch1 () returns (int, string, int) {
    int a;
    string name;
    int b;

    (a, name, b) = testMultiReturnInvalid();
    return (a, name, b);
}

function testMultiReturnInvalid () returns (string, string, int) {
    return (5, "john", 6);
}

function testAssignTypeMismatch2 () returns (int, string, int) {
    int a;
    int name;
    int b;

    (a, name, b) = testMultiReturnValid();
    return (a, name, b);
}

function testVarRepeatedReturn1 () returns (int, string, int) {
    var (a, name, a) = testMultiReturnValid();
    return (a, name, b);
}

function testVarRepeatedReturn2 () returns (int, string, int) {
    var (a, name, name) = testMultiReturnValid();
    return (a, name, b);
}

function testMultiReturnValid () returns (int, string, int) {
    return (5, "john", 6);
}

@final int i = 10;
@final string aa = "sam";

function testConstAssignment () {
    i = 20;
    return;
}

function constantAssignment () {
    // Following line is invalid.
    aa = "mad";
}