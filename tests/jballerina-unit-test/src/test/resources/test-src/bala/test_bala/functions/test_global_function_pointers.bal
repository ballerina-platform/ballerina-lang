import testorg/foo;

function test0 (string x, int y) returns string {
    string result = x + y.toString();
    return result;
}

function test0Reverse (string x, int y) returns string {
    string result = y.toString() + x;
    return result;
}

function test1() returns string {
    function (string a, int b) returns string f = foo:getGlf1();
    return f("test",1);
}

function test2() returns string{
    function (string a, boolean b) returns string f = foo:getGlf2();
    return f("test2", true);
}

function test3() returns [string, string, string] {
    foo:setGlf3(test0);
    function (string, int) returns string f = foo:getGlf3();
    string x = f("test",3);
    string y = test0("test",3);
    foo:setGlf3(test0Reverse);
    f = foo:getGlf3();
    string z = f("test",3);
    return [x, y, z];
}

function bar (string x, boolean y) returns string {
    string result = y.toString() + x;
    return result;
}

function test5() returns string{
    function (string a, boolean b) returns string glf1 = bar;
    return glf1("test5", false);
}
function test6() returns string {
    function (string a, boolean b) returns string glf1 = bar;
    foo:setGlf2(glf1);
    function (string a, boolean b) returns string f = foo:getGlf2();
    return f("test6", true);
}

