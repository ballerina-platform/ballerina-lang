import org.bar;

@final public int globalFinalInt = 10;


public function testFinalGlobalVariable() returns (int, int, int) {
    int v1 = globalFinalInt;
    globalFinalInt = 20;
    int v2 = globalFinalInt;
    bar:globalBarInt = 40;
    string v3 = bar:globalBarString;
    bar:globalBarString = "fail";
    return (v1, v2, globalFinalInt);
}

public function testFieldAsFinalParameter() returns (int) {
    int i = 50;
    int x = bar(i);
    return x;
}

function bar(@final int a) returns (int) {
    int i = a;
    a = 500;
    return a;
}
