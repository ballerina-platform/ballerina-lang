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

function testMistmatchErrorInMultiReturnCasting() {
    B b = {x: "x-valueof-b"};
    A a;
    Error err;
    a, err = (A) b;
}