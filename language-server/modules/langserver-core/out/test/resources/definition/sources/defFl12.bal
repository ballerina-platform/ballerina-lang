function basicClosure() returns (function (int) returns int) {
    int a = 3;
    var fooFunc = function (int b1) returns int {
        int c = 34;
        if (b1 == 3) {
            c = c + b1 + a;
        }
        return c + a;
    };
    return fooFunc;
}