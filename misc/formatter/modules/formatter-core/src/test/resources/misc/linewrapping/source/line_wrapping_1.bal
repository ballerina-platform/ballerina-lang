function foo(int firstParameter, record { int x;
string y =
 "This is some parameter default value. This requires wrapping both the expression and the parameter"; }) {
}

function foo(int firstParameter, record { int x;
string y =
 "This is another similar default value. Wrapping the parameter only should be enough"; }) {
}

function foo(int firstParameter,
record { int x;
string y =
 "A default value that doesn't require wrapping"; }) {
}
