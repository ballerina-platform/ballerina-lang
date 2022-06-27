type BooleanArray boolean[];
type StringArray string[];

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
    return float:sqrt(float:pow(x, 2.0));
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
    float v = float:pow(a, n);
    return v;
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
    return [i, checkpanic b.cloneWithType(BooleanArray)];
}

function bar(int i, string s = "hello", string... t) returns [int, string, string[]] {
    return [i, s, checkpanic t.cloneWithType(StringArray)];
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

client class Foo {
    function baz(int i, boolean... b) returns [int, boolean[]] {
        return [i, checkpanic b.cloneWithType(BooleanArray)];
    }

    remote function bar(int i, string s = "hello", string... t) returns [int, string, string[]] {
        return [i, s, checkpanic t.cloneWithType(StringArray)];
    }
}

type ArgsAsRecord record {
    boolean b;
    int i;
    string[] s;
};

function testVarargForParamsOfDifferentKinds() {
    [int, string...] x = [1, "foo"];
    ArgsAsRecord rec = takeParamsOfDifferentKinds(true, ...x);
    assertTrue(rec.b);
    assertValueEquality(1, rec.i);
    assertValueEquality(1, rec.s.length());
    assertValueEquality("foo", rec.s[0]);

    [boolean, int, string...] y = [false, 2, "bar", "baz"];
    rec = takeParamsOfDifferentKinds(...y);
    assertFalse(rec.b);
    assertValueEquality(2, rec.i);
    assertValueEquality(2, rec.s.length());
    assertValueEquality("bar", rec.s[0]);
    assertValueEquality("baz", rec.s[1]);
}

function takeParamsOfDifferentKinds(boolean b, int i, string... s) returns ArgsAsRecord {
    return {b, i, s};
}

function testArrayVarArg() {
    int[2] a = [1, 2];
    assertValueEquality(3, allInts(...a));

    int[3] b = [1, 2, 3];
    assertValueEquality(6, allInts(...b));

    int[5] c = [1, 2, 3, 1, 2];
    assertValueEquality(9, allInts(...c));

    int[] d = [1, 1, 2, 2, 1];
    assertValueEquality(7, intRestParam(...d));

    assertValueEquality(3, intRestParam(...a));

    assertValueEquality(6, intsWithStringRestParam(...a));
}

function allInts(int i, int j, int... k) returns int {
    int tot = i + j;
    foreach int x in k {
        tot += x;
    }
    return tot;
}

function intsWithStringRestParam(int i, int j, string... k) returns int {
    return 2 * (i + j + k.length());
}

function intRestParam(int... i) returns int {
     int tot = 0;
     foreach int x in i {
         tot += x;
     }
     return tot;
}

type Tuple [int, string...];

function testTypeRefTypedRestArg() {
    Tuple f = [1, "hello", "Ballerina"];
    int i = testFn(...f);
    assertValueEquality(15, i);
}

function testFn(int a, string... b) returns int =>
    b.reduce(function (int tot, string str) returns int => tot + str.length(), a);

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'true', found '" + (actual is error ? actual.toString() : actual.toString()) + "'");
}

function assertFalse(any|error actual) {
    if actual is boolean && !actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'false', found '" + (actual is error ? actual.toString() : actual.toString()) + "'");
}

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
