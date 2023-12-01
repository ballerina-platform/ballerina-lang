function foo(int a = -1, string b = "foo") returns int {
    return a + b.length();
}

function t1() returns int {
    return foo();
}

function tmp() returns int {
    function() returns int f = function() returns int {
        return 5;
    };
    return f();
}

function t2() returns int {
    function(int x = 5, string b = x.toString()) returns int f = foo;
    return f();
}
