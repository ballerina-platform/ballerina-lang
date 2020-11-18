transactional function() a;

function foo(transactional function() f) returns isolated transactional function() {
    transactional function(a, b) c;
    isolated transactional function()|MyType[] d;
    [transactional isolated function(), int, restType...] e;

    boolean b = x is transactional function() returns int;
    f2 = <isolated transactional function(int, int a) returns int>f1;
}

type bar record {
    transactional function() returns int func;
};
