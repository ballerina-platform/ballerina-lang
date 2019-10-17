function testIncompatibleTypeAssign () {
    boolean b;
    b = 12;
}

function testAssignCountMismatch1 () returns [int, string, int] {
    int a;
    string name;
    int b = 0;

    [a, name] = testMultiReturnValid();
    return [a, name, b];
}

function testAssignCountMismatch2 () returns [int, string, int] {
    int a;
    string name;
    int b;
    int c;

    [a, name, b, c] = testMultiReturnValid();
    return [a, name, b];
}

function testAssignTypeMismatch1 () returns [int, string, int] {
    int a;
    string name;
    int b;

    [a, name, b] = testMultiReturnInvalid();
    return [a, name, b];
}

function testMultiReturnInvalid () returns [string, string, int] {
    return [5, "john", 6];
}

function testAssignTypeMismatch2 () returns [int, string, int] {
    int a;
    int name;
    int b;

    [a, name, b] = testMultiReturnValid();
    return [a, name, b];
}

function testVarRepeatedReturn1 () returns [int, string, int] {
    var [a, name, a] = testMultiReturnValid();
    return [a, name, b];
}

function testVarRepeatedReturn2 () returns [int, string, int] {
    var [a, name, name] = testMultiReturnValid();
    return [a, name, b];
}

function testMultiReturnValid () returns [int, string, int] {
    return [5, "john", 6];
}

final int i = 10;
final string aa = "sam";

function testConstAssignment () {
    i = 20;
    return;
}

function constantAssignment () {
    // Following line is invalid.
    aa = "mad";
}

public type Client client object {
    public remote function foo() returns [int, int] {
        return [0, 0];
    }

    public remote function foo1() returns record { string a; } {
        return { a: "a" };
    }

    public remote function foo2() returns error {
        return error("the error reason");
    }
};

public function restActionResultAssignment() {
    Client c = new();
    int a = c->foo();
    map<string> sm = c->foo1();
    var { a: d } = c->foo2();
}
