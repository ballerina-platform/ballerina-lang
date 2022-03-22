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

function test7() {
    assertEquality(true, foo:anyFunction1() is function (string a, boolean b) returns string);
    assertEquality(false, foo:anyFunction1() is function (int a, boolean b) returns string);
    assertEquality(false, foo:anyFunction2(test0) is function (int a, boolean b) returns string);
    assertEquality(true, foo:anyFunction2(test0) is function (string, int) returns string);
}

public function fn() returns int => foo:func();
public function fn1() returns int => foo:func1();

function test8() {
    assertEquality(500, fn());
    assertEquality(800, fn1());
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any expected, any actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
