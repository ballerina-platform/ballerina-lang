import ballerina.lang.errors;

struct A {
    string x;
    int y;
}

struct B {
    string x;
}

function testCastingWithTooManyReturns() {
    B b = {x: "x-valueof-b"};
    A a;
    int i;
    errors:CastError err;
    a, err, i = (A) b;
}