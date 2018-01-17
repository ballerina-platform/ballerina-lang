struct Error {
    string msg;
}

struct A {
    string x;
    int y;
}

struct B {
    string x;
}

function testStructEquivalentErrorDirectAssignment() (Error) {
    B b = {x: "x-valueof-b"};
    A a;
    Error err;
    a, err = (A) b;
    return err;
}