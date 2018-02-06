function test1()(string, float){
    string a;
    float b;
    a, _, b = testMultiReturn1();
    return a,b;
}

function testMultiReturn1()(string, int, float){
    return "a", 1, 2.0;
}

function test2()(string, string){
    string[] x;
    x, _ = testMultiReturn2();
    return x[0], x[1];
}

function testMultiReturn2()(string[], float){
    string[] a = [ "a", "b", "c"];
    return a, 1.0;
}

function test3()(float){
    float x;
    _, x = testMultiReturn2();
    return x;
}

