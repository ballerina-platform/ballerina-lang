import ballerina/module1;

function createFunctionInWorker() {
    var fn1 = function() returns int {
        return foo();
    };

    var fn2 = function() returns string {
        foo(32);
        return "bar";
    };

    var fn3 = function() {
        return foo();
    };

    var fn4 = function() returns module1:TestRecord2 {
        return foo();
    };

    var fn5 = function() returns function () returns int {
        return function () returns int {
            return foo();
        };
    };
}
