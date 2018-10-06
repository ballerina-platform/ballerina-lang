import testorg/foo version v1;

function test0 (string x, int y) returns (string) {
    string result = x + y;
    return result;
}

function test0Reverse (string x, int y) returns (string) {
    string result = y + x;
    return result;
}

function test1() returns (string) {
    return foo:glf1("test",1);
}

function test2() returns (string){
    return foo:glf2("test2", true);
}

function test3() returns (string, string, string) {
    foo:glf3 = test0;
    string x = foo:glf3("test",3);
    string y = test0("test",3);
    foo:glf3 = test0Reverse;
    string z = foo:glf3("test",3);
    return (x, y, z);
}

function bar (string x, boolean y) returns (string) {
    string result = y + x;
    return result;
}

function test5() returns (string){
    function (string a, boolean b) returns (string) glf1 = bar;
    return glf1.call("test5", false);
}
function test6() returns (string) {
    function (string a, boolean b) returns (string) glf1 = bar;
    foo:glf2 = glf1;
    return foo:glf2("test6", true);
}

