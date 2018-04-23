import ballerina/lang.system;
import ballerina/doc;

function test (int x, string s) returns (float f) {
    var y, _ = <int>s;
    f = x * 1.0 * y;
    return;
}

@doc:Description {value:"function pointer as a parameter."}
function foo (int x, function (int, string) returns (float) bar) returns (float) {
    return x * bar(10, "2");
}

@doc:Description {value:"function pointer as a return type"}
function getIt () (function (int, string) returns (float) f) {
    f = test;
    return;
}

function main (string... args) {
    // value 'test' will serve as a pointer to 'test' function.
    system:println("Answer: " + foo(10, test));
    system:println("Answer: " + foo(10, getIt()));
    // function pointer as a variable;
    function (int, string) returns (float) f = getIt();
    system:println("Answer: " + foo(10, f));
}
