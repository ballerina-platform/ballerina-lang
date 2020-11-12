isolated function() a;

function foo(isolated function() f) returns isolated function() {
    isolated function(a, b) c;
    isolated function()|MyType[] d;
    [isolated function(), int, restType...] e;

    boolean b = x is isolated function() returns int;
    f2 = <isolated function(int, int a) returns int>f1;
}

type bar record {
    isolated function() returns int func;
};
