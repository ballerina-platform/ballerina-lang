import org.bar;

@final public int globalFinalInt = 10;
@final public string globalFinalString = "hello";


public function testFinalAccess() returns (int, int, int, int) {
    int v1 = globalFinalInt;
    int v2 = bar:globalBarInt;
    return (v1, v2, globalFinalInt, bar:globalBarInt);
}

public function testFinalStringAccess() returns (string, string, string, string) {
    string v1 = globalFinalString;
    string v2 = bar:globalBarString;
    return (v1, v2, globalFinalString, bar:globalBarString);
}

public function testFinalFieldAsParameter() returns (int) {
    int x = foo(globalFinalInt);
    return x;
}

public function testFieldAsFinalParameter() returns (int) {
    int i = 50;
    int x = bar(i);
    return x;
}


function foo(int a) returns (int) {
    int i = a;
    return i;
}

function bar(@final int a) returns (int) {
    int i = a;
    return a;
}
