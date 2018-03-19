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
    addValue(b);
    endTest();

    // Test 4
    string c;
    int d;
    (c, d) = ("test4", 4);
    addValue(c);
    addValue(d);
    endTest();

    // Test 5
    (string,int) f = ("test5",5);
    var (g, h) = f;
    addValue(g);
    addValue(h);
    endTest();

    // Test 6
    FooStruct foo6 = {x:"foo test6"};
    var i = ("test6",foo6);
    var (j, k) = i;
    addValue(j);
    addValue(k);
    endTest();
    return exFlow;
}

struct FooStruct {
    string x;
}

function addValue (any value) {
    var x = <string> value;
    exFlow += x;
    exFlow += " ";
}

function endTest(){
    addValue("\n");
}
