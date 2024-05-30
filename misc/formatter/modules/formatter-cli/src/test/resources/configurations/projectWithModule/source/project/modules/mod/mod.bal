class Calculator {
    int a;
    int b;

    function intAdd(any x, any y) returns int {
        return <int> x + <int> y;
    }
}

function bar(int|string x, int k, string longName, string ultraLongName, int r,
string|int|boolean j) returns string|int {
    return x;
}
