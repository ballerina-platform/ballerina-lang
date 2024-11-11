import testorg/foo as foo;
import testorg/functions;

//------------ Testing a function with all types of parameters ---------

function testInvokeFunctionInOrder1() returns [int, float, string, int, string, int[]] {
    return foo:functionWithAllTypesParams(10, 20.0, "Alex", 30, "Bob");
}

function testInvokeFunctionInOrder2() returns [int, float, string, int, string, int[]] {
    int[] _ = [40, 50, 60];
    return foo:functionWithAllTypesParams(10, 20.0, "Alex", 30, "Bob");
}

function testInvokeFunctionWithoutRestArgs() returns [int, float, string, int, string, int[]] {
    return foo:functionWithAllTypesParams(10, 20.0, "Alex", 30, "Bob");
}

function testInvokeFunctionWithoutSomeNamedArgs() returns [int, float, string, int, string, int[]] {
    return foo:functionWithAllTypesParams(10, 20.0, c="Alex");
}

function testInvokeFunctionWithRequiredArgsOnly() returns [int, float, string, int, string, int[]] {
    return foo:functionWithAllTypesParams(10, 20.0);
}

function funcInvocAsRestArgs() returns [int, float, string, int, string, int[]] {
    return foo:functionWithAllTypesParams(10, 20.0, "Alex", 30, "Bob");
}

function getIntArray() returns int[] {
    return [1,2,3,4];
}

function total(int b) returns int {
    return b;
}

function t = total;

function testAnyFunction() returns [boolean, boolean] {
    function fn = foo:anyFunction(t);
    return [fn is function (int) returns int, fn is function () returns int];
}

//------------- Testing a function having required and rest parameters --------

function testInvokeFuncWithoutRestParams() returns [int, float, string, int, string] {
    return foo:functionWithoutRestParams(a=10, e="Bob", b=20.0, d=30);
}

//------------- Testing a function having only named parameters --------

function testInvokeFuncWithOnlyNamedParams1() returns [int, float, string, int, string] {
    return foo:functionWithOnlyNamedParams(b = 20.0, e="Bob", d=30, a=10 , c="Alex");
}

function testInvokeFuncWithOnlyNamedParams2() returns [int, float, string, int, string] {
    return foo:functionWithOnlyNamedParams(e="Bob", d=30, c="Alex");
}

function testInvokeFuncWithOnlyNamedParams3() returns [int, float, string, int, string] {
    return foo:functionWithOnlyNamedParams();
}

//------------- Testing a function having only rest parameters --------

function testInvokeFuncWithOnlyRestParam1() returns int[] {
    return foo:functionWithOnlyRestParam();
}

function testInvokeFuncWithOnlyRestParam2() returns int[] {
    return foo:functionWithOnlyRestParam(10, 20, 30);
}

function testInvokeFuncWithOnlyRestParam3() returns int[] {
    int[] a = [10, 20, 30];
    return foo:functionWithOnlyRestParam(...a);
}

//------------- Testing a function with rest parameter of any type --------

function testInvokeFuncWithAnyRestParam1() returns any[] {
    int[] a = [10, 20, 30];
    json j = {"name":"John"};
    return foo:functionAnyRestParam(a, j);
}

// ------------------- Test function signature with invocation as default value of parameter

function testFuncCallingFuncFromDifferentModuleAsParamDefault() {
    assertValueEquality(101, functions:funcCallingFuncFromDifferentModuleAsParamDefault());
}

// ------------------- Test function signature with union types for default parameter

function testFuncWithUnionTypedDefaultParam() returns json {
    return foo:funcWithUnionTypedDefaultParam();
}


// ------------------- Test function signature with null as default parameter value

function testFuncWithNilDefaultParamExpr() returns [any, any] {
   return [foo:funcWithNilDefaultParamExpr_1(), foo:funcWithNilDefaultParamExpr_2()];
}

// ------------------- Test function signature for attached functions ------------------

function testAttachedFunction() returns [int, int] {
    foo:Employee emp = new;
    return [emp.getSalary("Alex"), emp.getSalary("Alex", b = 10)];
}


function testDefaultableParamInnerFunc() returns [int, string] {
    foo:Person p = new;
    return p.test1(a = 50);
}

function testDefaultableParamOuterFunc() returns [int, string] {
    foo:Person p = new;
    return p.test2(a = 40);
}

// ------------------- Test invocations with arg/vararg mix ------------------
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

    [int, boolean[]] res = foo:bazTwo(1, ...[true]);
    assertValueEquality(1, res[0]);
    assertValueEquality(1, res[1].length());
    assertTrue(res[1][0]);

    res = foo:bazTwo(1, ...a);
    assertValueEquality(1, res[0]);
    assertValueEquality(2, res[1].length());
    assertTrue(res[1][0]);
    assertFalse(res[1][1]);

    res = foo:bazTwo(...b);
    assertValueEquality(2, res[0]);
    assertValueEquality(0, res[1].length());

    res = foo:bazTwo(...c);
    assertValueEquality(3, res[0]);
    assertValueEquality(3, res[1].length());
    assertFalse(res[1][0]);
    assertTrue(res[1][1]);
    assertFalse(res[1][2]);

    res = foo:bazTwo(4, false, ...[true, true]);
    assertValueEquality(4, res[0]);
    assertValueEquality(3, res[1].length());
    assertFalse(res[1][0]);
    assertTrue(res[1][1]);
    assertTrue(res[1][2]);
}

function testInvocationWithArgVarargMixWithDefaultableParam() {
    var a = ["a", "b"];
    var b = [2, "j"];
    var c = [3, "x", "c", "d", "e"];

    [int, string, string[]] res = foo:barTwo(1, "p");
    assertValueEquality(1, res[0]);
    assertValueEquality("p", res[1]);
    assertValueEquality(0, res[2].length());

    res = foo:barTwo(1, "p", ...a);
    assertValueEquality(1, res[0]);
    assertValueEquality("p", res[1]);
    assertValueEquality(2, res[2].length());
    assertValueEquality("a", res[2][0]);
    assertValueEquality("b", res[2][1]);

    res = foo:barTwo(1, "p", "l", ...["m"]);
    assertValueEquality(1, res[0]);
    assertValueEquality("p", res[1]);
    assertValueEquality(2, res[2].length());
    assertValueEquality("l", res[2][0]);
    assertValueEquality("m", res[2][1]);

    res = foo:barTwo(1);
    assertValueEquality(1, res[0]);
    assertValueEquality("hello", res[1]);
    assertValueEquality(0, res[2].length());

    res = foo:barTwo(...b);
    assertValueEquality(2, res[0]);
    assertValueEquality("j", res[1]);
    assertValueEquality(0, res[2].length());

    res = foo:barTwo(...c);
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

    [int, string, string[]] res = foo:barTwo(...fn());
    assertValueEquality(100, res[0]);
    assertValueEquality("foo", res[1]);
    assertValueEquality(2, res[2].length());
    assertValueEquality("bar", res[2][0]);
    assertValueEquality("baz", res[2][1]);

    assertValueEquality(1, counter);
}

function testMethodInvocationWithArgVarargMixWithoutDefaultableParam() {
    foo:FooTwo f = new;

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
    foo:FooTwo f = new;

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
    foo:FooTwo f = new;

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

type Func isolated function ();

public isolated function f1() {
    json|error res = foo:testFunc("f1");
}

final readonly & map<Func> func = {
    "f1": f1
};

public isolated function testFunc() {
    Func? f = func["f1"];
    if f is () {
        return ();
    }
    _ = f();
}

public function f2() returns int {
    json|error res = foo:testFunc("f2");
    if res is error {
        return -1;
    }
    return 1;
}

final readonly & map<int> func1 = {
    "f1": f2()
};

function testCyclicFuncCallWhenFuncDefinedInModuleWithSameName() {
    assertValueEquality((), testFunc());
    assertValueEquality(1, func1["f1"]);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }

    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON, message = "expected 'true', found '" + actualValAsString + "'");
}

function assertFalse(any|error actual) {
    if actual is boolean && !actual {
        return;
    }

    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON, message = "expected 'false', found '" + actualValAsString + "'");
}

function assertValueEquality(anydata|error expected, anydata|error actual) {
    if isEqual(expected, actual) {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString+ "', found '" + actualValAsString + "'");
}

isolated function isEqual(anydata|error val1, anydata|error val2) returns boolean {
    if (val1 is anydata && val2 is anydata) {
        return (val1 == val2);
    } else {
        return (val1 === val2);
    }
}
