function foo() {
    [int, string] a = [4, "hello"];
    [int] b;
    [mytype, mytype2...] c;
    [+5, int[]...] d;
    [restParam...] e;
    [stream<mytype, int>] f;
    [table<myType> key(id), int, float] g;
}
