type A {
    string x,
    int y,
};

type B {
    string x,
};

function testCastingWithTooManyReturns() {
    B b = {x: "x-valueof-b"};
    A a = {};
    int i;
    error err = {};
    a, err, i = check <A> b;
}