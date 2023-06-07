final int PI = 3.14989;

final float floatVariableEx = "not a float";

final int intVariableEx = "not an int";

// const map<string> X = {a: "a"};

// type Foo record {|
//    X x;
//    int i;
// |};

// const Foo F1 = {x: {b : "a"}, i: 1, c: 2};
// const record{|X x; int i;|} F2 = {x: {b : "a"}, i: 1, c: 2};

// const X F3 = {b : "b"};

const int CONST1 = 3 + 5;
type A CONST1;
const A CONST2 = 3;

function testAssignInvalidValue() {
    A a = 3;
    CONST1 c = 4;
}
