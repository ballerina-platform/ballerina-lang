
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

function testSafeAssignmentBasics2 () returns (boolean | error) {
      boolean statusFailure = check openFileFailure("/home/sameera/bar.txt");
      return statusFailure;
}


function testSafeAssignmentBasics3 () returns (error?) {
    boolean statusFailure = check openFileFailure("/home/sameera/bar.txt");
    return ();
}

function testSafeAssignmentBasics4 () returns (boolean | error) {
    boolean statusFailure = check openFileFailure("/home/sameera/bar.txt");
    return statusFailure;
}

function testSafeAssignOpInAssignmentStatement1 () returns (boolean | error) {
    boolean b = false;
    int a = 0;
    b = check openFileSuccess("/home/sameera/foo.txt");
    return b;
}

function testSafeAssignOpInAssignmentStatement2 () returns (boolean|error) {
    boolean b = false;
    int a = 0;
    b = check openFileFailure("/home/sameera/foo.txt");
    return b;
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
    boolean statusFailure;
    int a = 10;
    statusFailure = check openFileFailure("/home/sameera/bar.txt");
    return ();
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

type MyError error<string, MyErrorData>;

public type CustomErrorData record {|
    string data;
    string message?;
    error cause?;
|};

type CustomError error<string, CustomErrorData>;

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
    MyError e = error("io error");
    return e;
}

function readLineCustomError() returns string | CustomError {
    CustomError e = error("custom io error", data = "foo.txt");
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
