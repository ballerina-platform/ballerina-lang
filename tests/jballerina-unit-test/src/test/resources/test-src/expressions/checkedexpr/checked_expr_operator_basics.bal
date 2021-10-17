import ballerina/lang.value;
function openFileSuccess(string path) returns (boolean | error) {
    return true;
}

function openFileFailure(string path) returns (boolean | error) {
    error  e = error("file not found error: " + path);
    return e;
}

function testSafeAssignmentBasics1 () returns (boolean | error) {
    boolean statusSuccess = check openFileSuccess("/home/sameera/foo.txt");
    return statusSuccess;
}

function testSafeAssignmentBasics2 () {
    var f = function () returns error|boolean {
        boolean statusFailure = check openFileFailure("/home/sameera/bar.txt");
        return statusFailure;
    };
    var e = f();
    validateError(e);
}

function validateError(any|error e) {
    if (e is error) {
        string expMessage = "file not found error: /home/sameera/bar.txt";
        if (e.message() == expMessage) {
            return;
        }
        panic error(string `Expected error message: ${expMessage}, found: ${e.message()}`);
    }
    panic error("Expected error, found boolean");
}

function testSafeAssignmentBasics3 () returns (error?) {
    var f = function () returns error? {
        boolean statusFailure = check openFileFailure("/home/sameera/bar.txt");
        return ();
    };
    var e = f();
    validateError(e);
}

function testSafeAssignmentBasics4 () {
    var f = function () returns (boolean|error) {
        boolean statusFailure = check openFileFailure("/home/sameera/bar.txt");
        return statusFailure;
    };
    var e = f();
    validateError(e);
}

function testSafeAssignOpInAssignmentStatement1 () returns (boolean | error) {
    boolean b = false;
    int a = 0;
    b = check openFileSuccess("/home/sameera/foo.txt");
    return b;
}

function testSafeAssignOpInAssignmentStatement2 () {
    var f = function () returns (boolean|error) {
        boolean b = false;
        int a = 0;
        b = check openFileFailure("/home/sameera/bar.txt");
        return b;
    };
    var e = f();
    validateError(e);
}

function testSafeAssignOpInAssignmentStatement3 () returns (boolean|error) {
    FileOpenStatus fos = {};
    fos.status = check openFileSuccess("/home/sameera/foo.txt");
    return fos.status;
}

type FileOpenStatus record {
    boolean status = false;
};

function testSafeAssignOpInAssignmentStatement4 () returns (boolean|error) {
    boolean[] ba = [];
    ba[0] = check openFileSuccess("/home/sameera/foo.txt");
    ba[1] = false;
    return ba[0];
}

function testSafeAssignOpInAssignmentStatement5 () returns error? {
    var f = function () returns error? {
        boolean statusFailure;
        int a = 10;
        statusFailure = check openFileFailure("/home/sameera/bar.txt");
        return ();
    };
    var e = f();
    validateError(e);
}

function testSafeAssignOpInAssignmentStatement6 () returns (boolean | error) {
    int a = 10;
    var statusFailure = check openFileSuccess("/home/sameera/bar.txt");
    return statusFailure;
}

type Person record {
    string name;
};

public type MyErrorData record {|
    string message?;
    error cause?;
|};

type MyError error<MyErrorData>;

public type CustomErrorData record {|
    string data;
    string message?;
    error cause?;
|};

type CustomError error<CustomErrorData>;

function getPerson() returns Person | MyError {
   //myerror e = error("ddd");
    //return e;
    Person p = {name:"Diayasena"};
    return  p;
}

function testSafeAssignOpInAssignmentStatement7 () returns (string | error) {
    var p = check getPerson();
    return p.name;
}


function readLineError() returns string | MyError {
    MyError e = error MyError("io error");
    return e;
}

function readLineCustomError() returns string | CustomError {
    CustomError e = error CustomError("custom io error", data = "foo.txt");
    return e;
}

function readLineSuccess() returns string | MyError {
    return "Ballerina";
}

function testCheckExprInBinaryExpr1() returns error? {
    string str = "hello, " + check readLineError();
    return ();
}

function testCheckExprInBinaryExpr2() returns MyError? {
    string str = "hello, " + check readLineError();
    return ();
}

function testCheckExprInBinaryExpr3() returns string | MyError {
    string str = "hello, " + check readLineSuccess();
    return str;
}

function testCheckExprInBinaryExpr4() returns error {
    string str = "hello, " + check readLineError();
    return error("error");
}

function testCheckExprInBinaryExpr5() returns error? {
    string str = "hello, " + check readLineError();
    return ();
}

function testCheckExprInBinaryExpr6() returns string | CustomError {
    string str = "hello, " + check readLineCustomError();
    return str;
}

function readLineProper() returns string | MyError | CustomError {
    return "Hello, World!!!";
}

function testCheckExprInBinaryExpr8() returns (string | error) {
    string str = "hello, " + check readLineProper();
    return str;
}

function foo(string s) returns string | CustomError {
    return "(" + s + "|" + s + ")";
}

function bar(string s1, string s2) returns string | CustomError  {
    return s1 + " " + s2;
}

function testCheckedExprAsFuncParam1() returns string | error  {
    return check bar(check bar(check foo(check foo(check foo(check foo("S")))),
                check foo(check foo("A"))) ,
                    check bar(check foo(check foo(check foo("M"))), "done"));
}

//function testCheckInBinaryAndExpression() returns boolean|error {
//    string s = "Ballerina";
//    if (check s.matches("B.*") && check s.matches(".*a")) {
//        return true;
//    }
//    return false;
//}

function testCheckInBinaryAddExpression() returns int|error {
    int|error a = 10;
    int|error b = 20;
    return check a + check b;
}

function testCheckInBinaryDivExpression() returns int|error {
    int|error a = 10;
    int|error b = 20;
    return check b / check a;
}

function testCheckInBinaryLTExpression() returns boolean|error {
    int|error a = 10;
    int|error b = 20;
    return check b < check a;
}

function baz() returns boolean|error {
    value:Cloneable x = error("error one!");
    any y = check x;
    return true;
}

type CyclicUnion readonly|boolean[]|CyclicUnion[];

function corge(boolean bool) returns error|int {
    CyclicUnion x = bool ? error("error two!") : 1234;
    any y = check x;
    return y is int ? y : 0;
}

function testCheckedErrorsWithReadOnlyInUnion() {
    boolean|error x = baz();
    assertTrue(x is error);
    error errVal = <error> x;
    assertEquality("error one!", errVal.message());
    assertTrue(errVal.cause() is ());
    assertEquality(0, errVal.detail().length());

    int|error y = corge(true);
    assertTrue(y is error);
    errVal = <error> y;
    assertEquality("error two!", errVal.message());
    assertTrue(errVal.cause() is ());
    assertEquality(0, errVal.detail().length());

    y = corge(false);
    assertTrue(y is int);
    assertEquality(1234, checkpanic y);
}

type Err distinct error;

function ternaryCheck(boolean b) returns string|Err {
    return check (b ? barOrErr() : bazOrErr());
}

function barOrErr() returns string|Err {
    return "bar";
}

function bazOrErr() returns string|Err {
    return error("Err");
}

function testCheckWithTernaryOperator() {
    var a = ternaryCheck(false);
    if !(a is Err) {
        panic error("Expected value of type: Err, found: " + (typeof a).toString());
    }
    a = ternaryCheck(true);
    if !(a is string) {
        panic error("Expected value of type: string, found: " + (typeof a).toString());
    }
}

function emitError(int i) returns int|Err|error {
    match i {
        1 => { return 1; }
        2 => { return error Err("Err"); }
        _ => { return error("Any error"); }
    }
}

function performErrornousAction(int i) returns error? {
    int m = check emitError(i);
}

function testCheckWithMixOfDefaultErrorAndDistinctErrors() {
    var result = performErrornousAction(1);
    assertTrue(result is ());

    result = performErrornousAction(2);
    assertTrue(result is Err && result.message() == "Err");
}

float location1 =
    let var statusSuccess = check openFileSuccess("/home/sameera/foo.txt")
    in 3.33;

function testCheckInLetExpression() returns error? {
    float location =
        let var statusSuccess = check openFileSuccess("/home/sameera/foo.txt")
        in 5.33;
    assertEquality(5.33, location);
    assertEquality(3.33, location1);
}

function testCheckedExprWithNever() {
    error? e = checkingFunc();
    assertTrue(e is error && e.message() == "io error");
}

function checkingFunc() returns error? {
    check getErr();
}

function getErr() returns error {
    error e = error("io error");
    return e;
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(anydata actual) {
    assertEquality(true, actual);
}

function assertFalse(anydata actual) {
    assertEquality(false, actual);
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
