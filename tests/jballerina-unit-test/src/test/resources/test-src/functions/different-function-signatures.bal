const ASSERTION_ERROR_REASON = "AssertionError";

//------------ Testing a function with all types of parameters ---------

function functionWithAllTypesParams(int a, float b, string c = "John", int d = 5, string e = "Doe", int... z)
        returns [int, float, string, int, string, int[]] {
    return [a, b, c, d, e, z];
}

function testInvokeFunctionInOrder1() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(10, 20.0, c="Alex", d=30, e="Bob");
}

function testInvokeFunctionInOrder2() returns [int, float, string, int, string, int[]] {
    int[] array = [40, 50, 60];
    return functionWithAllTypesParams(10, 20.0, c="Alex", d=30, e="Bob");
}

function testInvokeFunctionInMixOrder1() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(e="Bob", b=20.0, c="Alex", a=10, d=30);
}

function testInvokeFunctionInMixOrder2() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(10, c="Alex", e="Bob", d=30, b=20.0);
}

function testInvokeFunctionWithoutSomeNamedArgs() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(10, 20.0, c="Alex");
}

function testInvokeFunctionWithRequiredArgsOnly() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(10, 20.0);
}

function testInvokeFunctionWithAllParamsAndRestArgs() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(10, 20.0, "John1", 6, "Doe1", 40, 50, 60);
}

function funcInvocAsRestArgs() returns [int, float, string, int, string, int[]] {
    return functionWithAllTypesParams(10, 20.0, "Alex", 30, "Bob", ...getIntArray());
}

function getIntArray() returns (int[]) {
    return [1,2,3,4];
}

//------------- Testing a function having required and rest parameters --------

function functionWithoutRestParams(int a, float b, string c = "John", int d = 5, string e = "Doe") returns
            [int, float, string, int, string] {
    return [a, b, c, d, e];
}

function testInvokeFuncWithoutRestParamsAndMissingDefaultableParam() returns [int, float, string, int, string] {
    return functionWithoutRestParams(10, b=20.0, d=30, e="Bob");
}

//------------- Testing a function having only named parameters --------


function functionWithOnlyNamedParams(int a=5, float b=6.0, string c = "John", int d = 7, string e = "Doe")
                                                                                                    returns [int, float, string, int, string] {
    return [a, b, c, d, e];
}

function testInvokeFuncWithOnlyNamedParams1() returns [int, float, string, int, string] {
    return functionWithOnlyNamedParams(b = 20.0, e="Bob", d=30, a=10, c="Alex");
}

function testInvokeFuncWithOnlyNamedParams2() returns [int, float, string, int, string] {
    return functionWithOnlyNamedParams(e="Bob", d=30, c="Alex");
}

function testInvokeFuncWithOnlyNamedParams3() returns [int, float, string, int, string] {
    return functionWithOnlyNamedParams();
}

//------------- Testing a function having only rest parameters --------

function functionWithOnlyRestParam(int... z) returns (int[]) {
    return z;
}

function testInvokeFuncWithOnlyRestParam1() returns (int[]) {
    return functionWithOnlyRestParam();
}

function testInvokeFuncWithOnlyRestParam2() returns (int[]) {
    return functionWithOnlyRestParam(10, 20, 30);
}

function testInvokeFuncWithOnlyRestParam3() returns (int[]) {
    int[] a = [10, 20, 30];
    return functionWithOnlyRestParam(...a);
}

//------------- Testing a function with rest parameter of any type --------

function functionAnyRestParam(any... z) returns (any[]) {
    return z;
}

function testInvokeFuncWithAnyRestParam1() returns (any[]) {
    int[] a = [10, 20, 30];
    json j = {"name":"John"};
    return functionAnyRestParam(a, j);
}

// ------------------- Test function signature with union types for default parameter

function funcWithUnionTypedDefaultParam(string|int? s = "John") returns string|int? {
    return s;
}

function testFuncWithUnionTypedDefaultParam() returns json {
    return funcWithUnionTypedDefaultParam();
}


// ------------------- Test function signature with null as default parameter value

function funcWithNilDefaultParamExpr_1(string? s = ()) returns string? {
    return s;
}

type Student record {|
    int a = 0;
    anydata...;
|};

function funcWithNilDefaultParamExpr_2(Student? s = ()) returns Student? {
    return s;
}

function testFuncWithNilDefaultParamExpr() returns [any, any] {
    return [funcWithNilDefaultParamExpr_1(), funcWithNilDefaultParamExpr_2()];
}

// ------------------- Test function signature for attached functions ------------------

public class Employee {

    public string name = "";
    public int salary = 0;

    function init(string name = "supun", int salary = 100) {
        self.name = name;
        self.salary = salary;
    }

    public function getSalary (string n, int b = 0) returns int {
        return self.salary + b;
    }
}

function testAttachedFunction() returns [int, int] {
    Employee emp = new;
    return [emp.getSalary("Alex"), emp.getSalary("Alex", b = 10)];
}


function testDefaultableParamInnerFunc () returns [int, string] {
    Person p = new;
    return p.test1(a = 50);
}

class Person {
    public int age = 0;

    function test1(int a = 77, string n = "inner default") returns [int, string] {
        string val = n + " world";
        int intVal = a + 10;
        return [intVal, val];
    }

    function test2(int a = 89, string n = "hello") returns [int, string] {
        string val = n + " world";
        int intVal = a + 10;
        return [intVal, val];
    }
}

// ------------------- Test function signature which has a function typed param with only rest param ------------------

function functionOfFunctionTypedParamWithRest(int[] x, function (int...) returns int bar) returns [int, int] {
    return [x[0], bar(x[0])] ;
}

function sampleFunctionTypedParam(int... a) returns int {
    if (a.length() > 0) {
        return a[0];
    } else {
        return 0;
    }
}

function testFunctionOfFunctionTypedParamWithRest1() {
    int[] x = [4];
    int[] y = functionOfFunctionTypedParamWithRest(x, sampleFunctionTypedParam);
    assertEquality(x[0], y[0]);
    assertEquality(x[0], y[1]);
}

function testFunctionOfFunctionTypedParamWithRest2() {
    int[] x = [10, 20, 30];
    int[] y = functionOfFunctionTypedParamWithRest(x, sampleFunctionTypedParam);
    assertEquality(x[0], y[0]);
    assertEquality(x[0], y[1]);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
