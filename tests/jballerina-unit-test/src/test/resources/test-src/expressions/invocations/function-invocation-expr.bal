import ballerina/math;

function testFuncInvocation (int a, int b, int c) returns (int) {
    int x;
    x = 10;
    int val = add(a, b);
    val = add(val, c);
    return add(val, x);
}

function add(int x, int y) returns (int) {
    int z;
    z = x  + y;
    return z;
}

function multiply(int x, int y) returns (int) {
    int z;
    z = x * y;
    return z;
}

function funcInvocationWithinFuncInvocation(int a, int b, int c) returns (int){
    int result;

    result = add( add( add(a, c), b), add(b, c) );
    return result + add(a, b) + add(a, b);
}

function testReturnFuncInvocationWithinFuncInvocation(int a, int b) returns (int){
    return add(a, multiply(a, b));
}

function testReturnNativeFuncInvocationWithinNativeFuncInvocation(float x) returns (float) {
    return math:sqrt(math:pow(x, 2.0));
}

function sum (int a) returns @untainted int {
    int x;
    int val = 0;
    if (a > 0) {
        x = sum(a - 1);
        val =  a + x;
    }
    return val;
}

function getPowerOfN (float a, float n) returns (float) {
    float v = math:pow(a, n);
    return v;
}

function testInvocationWithArgVarargMix() {
    testInvocationWithArgVarargMixWithoutDefaultableParam();
    testInvocationWithArgVarargMixWithDefaultableParam();
    testVarargEvaluationCount();
    testMethodInvocationWithArgVarargMixWithoutDefaultableParam();
    testMethodInvocationWithArgVarargMixWithDefaultableParam();
    testMethodVarargEvaluationCount();
}

function testInvocationWithArgVarargMixWithoutDefaultableParam() {
    var a = [true, false];
    var b = [2];
    var c = [3, false, true, false];

    [int, boolean[]] res = baz(1, ...[true]);
    assertValueEquality(1, res[0]);
    assertValueEquality(1, res[1].length());
    assertTrue(res[1][0]);

    res = baz(1, ...a);
    assertValueEquality(1, res[0]);
    assertValueEquality(2, res[1].length());
    assertTrue(res[1][0]);
    assertFalse(res[1][1]);

    res = baz(...b);
    assertValueEquality(2, res[0]);
    assertValueEquality(0, res[1].length());

    res = baz(...c);
    assertValueEquality(3, res[0]);
    assertValueEquality(3, res[1].length());
    assertFalse(res[1][0]);
    assertTrue(res[1][1]);
    assertFalse(res[1][2]);

    res = baz(4, false, ...[true, true]);
    assertValueEquality(4, res[0]);
    assertValueEquality(3, res[1].length());
    assertFalse(res[1][0]);
    assertTrue(res[1][1]);
    assertTrue(res[1][2]);

    [int, string...] x = [1, "hello", "world"];
    qux(...x);
}

function testInvocationWithArgVarargMixWithDefaultableParam() {
    var a = ["a", "b"];
    var b = [2, "j"];
    var c = [3, "x", "c", "d", "e"];

    [int, string, string[]] res = bar(1, "p");
    assertValueEquality(1, res[0]);
    assertValueEquality("p", res[1]);
    assertValueEquality(0, res[2].length());

    res = bar(1, "p", ...a);
    assertValueEquality(1, res[0]);
    assertValueEquality("p", res[1]);
    assertValueEquality(2, res[2].length());
    assertValueEquality("a", res[2][0]);
    assertValueEquality("b", res[2][1]);

    res = bar(1, "p", "l", ...["m"]);
    assertValueEquality(1, res[0]);
    assertValueEquality("p", res[1]);
    assertValueEquality(2, res[2].length());
    assertValueEquality("l", res[2][0]);
    assertValueEquality("m", res[2][1]);

    res = bar(1);
    assertValueEquality(1, res[0]);
    assertValueEquality("hello", res[1]);
    assertValueEquality(0, res[2].length());

    res = bar(...b);
    assertValueEquality(2, res[0]);
    assertValueEquality("j", res[1]);
    assertValueEquality(0, res[2].length());

    res = bar(...c);
    assertValueEquality(3, res[0]);
    assertValueEquality("x", res[1]);
    assertValueEquality(3, res[2].length());
    assertValueEquality("c", res[2][0]);
    assertValueEquality("d", res[2][1]);
    assertValueEquality("e", res[2][2]);
}

function testVarargEvaluationCount() {
    // The expression given as the vararg should only be evaluated once.
    int counter = 0;

    var fn = function () returns [int, string, string...] {
        counter += 1;
        return [100, "foo", "bar", "baz"];
    };

    [int, string, string[]] res = bar(...fn());
    assertValueEquality(100, res[0]);
    assertValueEquality("foo", res[1]);
    assertValueEquality(2, res[2].length());
    assertValueEquality("bar", res[2][0]);
    assertValueEquality("baz", res[2][1]);

    assertValueEquality(1, counter);
}

function baz(int i, boolean... b) returns [int, boolean[]] {
    return [i, checkpanic b.cloneWithType(boolean[])];
}

function bar(int i, string s = "hello", string... t) returns [int, string, string[]] {
    return [i, s, checkpanic t.cloneWithType(string[])];
}

function qux(int i, string... s) {
    assertValueEquality(1, i);
    assertValueEquality("hello", s[0]);
    assertValueEquality("world", s[1]);
}

function testMethodInvocationWithArgVarargMixWithoutDefaultableParam() {
    Foo f = new;

    var a = [true, false];
    var b = [2];
    var c = [3, false, true, false];

    [int, boolean[]] res = f.baz(1, ...[true]);
    assertValueEquality(1, res[0]);
    assertValueEquality(1, res[1].length());
    assertTrue(res[1][0]);

    res = f.baz(1, ...a);
    assertValueEquality(1, res[0]);
    assertValueEquality(2, res[1].length());
    assertTrue(res[1][0]);
    assertFalse(res[1][1]);

    res = f.baz(...b);
    assertValueEquality(2, res[0]);
    assertValueEquality(0, res[1].length());

    res = f.baz(...c);
    assertValueEquality(3, res[0]);
    assertValueEquality(3, res[1].length());
    assertFalse(res[1][0]);
    assertTrue(res[1][1]);
    assertFalse(res[1][2]);

    res = f.baz(4, false, ...[true, true]);
    assertValueEquality(4, res[0]);
    assertValueEquality(3, res[1].length());
    assertFalse(res[1][0]);
    assertTrue(res[1][1]);
    assertTrue(res[1][2]);
}

function testMethodInvocationWithArgVarargMixWithDefaultableParam() {
    Foo f = new;

    var a = ["a", "b"];
    var b = [2, "j"];
    var c = [3, "x", "c", "d", "e"];

    [int, string, string[]] res = f->bar(1, "p");
    assertValueEquality(1, res[0]);
    assertValueEquality("p", res[1]);
    assertValueEquality(0, res[2].length());

    res = f->bar(1, "p", ...a);
    assertValueEquality(1, res[0]);
    assertValueEquality("p", res[1]);
    assertValueEquality(2, res[2].length());
    assertValueEquality("a", res[2][0]);
    assertValueEquality("b", res[2][1]);

    res = f->bar(1, "p", "l", ...["m"]);
    assertValueEquality(1, res[0]);
    assertValueEquality("p", res[1]);
    assertValueEquality(2, res[2].length());
    assertValueEquality("l", res[2][0]);
    assertValueEquality("m", res[2][1]);

    res = f->bar(1);
    assertValueEquality(1, res[0]);
    assertValueEquality("hello", res[1]);
    assertValueEquality(0, res[2].length());

    res = f->bar(...b);
    assertValueEquality(2, res[0]);
    assertValueEquality("j", res[1]);
    assertValueEquality(0, res[2].length());

    res = f->bar(...c);
    assertValueEquality(3, res[0]);
    assertValueEquality("x", res[1]);
    assertValueEquality(3, res[2].length());
    assertValueEquality("c", res[2][0]);
    assertValueEquality("d", res[2][1]);
    assertValueEquality("e", res[2][2]);
}

function testMethodVarargEvaluationCount() {
    // The expression given as the vararg should only be evaluated once.
    Foo f = new;

    int counter = 0;

    var fn = function () returns [int, string, string...] {
        counter += 1;
        return [100, "foo", "bar", "baz"];
    };

    [int, string, string[]] res = f->bar(...fn());
    assertValueEquality(100, res[0]);
    assertValueEquality("foo", res[1]);
    assertValueEquality(2, res[2].length());
    assertValueEquality("bar", res[2][0]);
    assertValueEquality("baz", res[2][1]);

    assertValueEquality(1, counter);
}

type Foo client object {
    function baz(int i, boolean... b) returns [int, boolean[]] {
        return [i, checkpanic b.cloneWithType(boolean[])];
    }

    remote function bar(int i, string s = "hello", string... t) returns [int, string, string[]] {
        return [i, s, checkpanic t.cloneWithType(string[])];
    }
};

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'true', found '" + actual.toString () + "'");
}

function assertFalse(any|error actual) {
    if actual is boolean && !actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'false', found '" + actual.toString () + "'");
}

function assertValueEquality(anydata|error expected, anydata|error actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
