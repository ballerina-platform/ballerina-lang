string exFlow = " ";
function basicTupleTest () returns (string) {
    // Test 1;
    string z = ("test1 " + "expr");
    addValue(z);
    endTest();

    // Test 2
    var y = ("test2");
    addValue(y);
    endTest();

    // Test 3
    FooStruct foo = {x:"foo test3"};
    var (a, b) = ("test3",foo);
    addValue(a);
    addValue(b.x);
    endTest();

    // Test 4
    string c;
    int d;
    (c, d) = ("test4", 4);
    addValue(c);
    addValue(<string> d);
    endTest();

    // Test 5
    (string,int) f = ("test5",5);
    var (g, h) = f;
    addValue(g);
    addValue(<string> h);
    endTest();

    // Test 6
    FooStruct foo6 = {x:"foo test6"};
    var i = ("test6",foo6);
    var (j, k) = i;
    addValue(j);
    addValue(k.x);
    endTest();
    return exFlow;
}

type FooStruct {
    string x;
};

function addValue (string value) {
    exFlow += value;
    exFlow += " ";
}

function endTest(){
    addValue("\n");
}

function testFunctionInvocation() returns (string) {
    var i = ("y",5.0, "z");
    string x = testTuples("x", i);
    return x;
}

function testTuples (string x, (string, float, string) y) returns (string) {
    var (i, j, k) = y;
    return x + i + j + k;
}

function testFunctionReturnValue() returns (string) {
    (string, float, string) x = testReturnTuples("x");
    var (i, j, k) = x;
    return i + j + k;
}

function testReturnTuples (string a) returns ((string, float, string)) {
    var x = (a,5.0, "z");
    return x;
}

function testFunctionReturnValue2() returns (string, float) {
    var (i, j, k) = testReturnTuples("x");
    return (i + k, j);
}

function testIgnoredValue1 () returns string {
    (string, int) x = ("foo", 1);
    var (a, _) = x;
    return a;
}

function testIgnoredValue2 () returns string {
    (string, int, int) x = ("foo", 1, 2);
    var (a, _, c) = x;
    return a;
}

function testIgnoredValue3 () returns string {
    (string, int, int) x = ("foo", 1, 2);
    string a;
    (a, _, _) = x;
    return a;
}
