type F function(int) returns int;
type G function(int) returns F;
type H function(int...) returns int;

function createVarArgClosure() returns H {
    int base = 5;
    H f = function(int... args) returns int {
        int sum = base;
        foreach int arg in args {
            sum += arg;
        }
        return sum;
    };
    return f;
}

function createSimpleClosure() returns F {
    int a = 5;
    F f = function(int x) returns int {
        return a * x;
    };
    return f;
}

function createNestedClosure() returns G {
    int a = 5;
    G f = function(int x) returns F {
        int b = 10;
        F g = function(int y) returns int {
            return a * x + b * y;
        };
        return g;
    };
    return f;
}

function test1() returns int {
    F f = createSimpleClosure();
    return f(10);
}

function test2() returns int {
    G f = createNestedClosure();
    F g = f(10);
    return g(20);
}

function test3() returns int {
    H f = createVarArgClosure();
    return f(10, 20, 30);
}
