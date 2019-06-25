function test1() returns [string, float]{
    string a;
    float b;
    [a, _, b] = testMultiReturn1();
    return [a,b];
}

function testMultiReturn1() returns [string, int, float]{
    return ["a", 1, 2.0];
}

function test2() returns [string, string]{
    string[] x;
    [x, _] = testMultiReturn2();
    return [x[0], x[1]];
}

function testMultiReturn2() returns [string[], float]{
    string[] a = [ "a", "b", "c"];
    return [a, 1.0];
}

function test3() returns (float){
    float x;
    [_, x] = testMultiReturn2();
    return x;
}

function test4() returns [string, float]{
    string a = "a";
    float b = 0.0;
    [_, _, _] = testMultiReturn4();
    return [a,b];
}

function testMultiReturn4() returns [string, int, float]{
    return ["a", 1, 2.0];
}

function test5(){
    _ = testMultiReturn5();
}

function testMultiReturn5() returns (string){
    return "a";
}

