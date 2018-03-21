import ballerina/lang.system;

function foo(int a)(int) {
bar(a);
bar(500);
bar(400);
return 1000;
}

function bar(int b) {
foobar(b);
}

function foobar(int c) {
system:println(c);
}