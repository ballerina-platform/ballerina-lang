
function openFileSuccess(string path) returns (boolean | error) {
    return true;
}

function openFileFailure(string path) returns (boolean | error) {
    error  e = {message: "file not found error: " + path};
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


function testSafeAssignmentBasics3 () {
    boolean statusFailure = check openFileFailure("/home/sameera/bar.txt");
}

function testSafeAssignmentBasics4 () returns (boolean){
    boolean statusFailure = check openFileFailure("/home/sameera/bar.txt");
    return statusFailure;
}

function testSafeAssignOpInAssignmentStatement1 () returns (boolean) {
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

type FileOpenStatus {
    boolean status = false;
};

function testSafeAssignOpInAssignmentStatement4 () returns (boolean|error) {
    boolean[] ba = [];
    ba[0] = check openFileSuccess("/home/sameera/foo.txt");
    ba[1] = false;
    return ba[0];
}

function testSafeAssignOpInAssignmentStatement5 () {
    boolean statusFailure;
    int a = 10;
    statusFailure = check openFileFailure("/home/sameera/bar.txt");
}

function testSafeAssignOpInAssignmentStatement6 () returns boolean {
    int a = 10;
    var statusFailure = check openFileSuccess("/home/sameera/bar.txt");
    return statusFailure;
}

type person {
    string name;
};

public type myerror {
    string message;
    error? cause;
    int code;
};

public type customError {
    string message;
    error? cause;
    int code;
    string data;
};

function getPerson() returns person | myerror {
   //myerror e = {message:"ddd"};
    //return e;
    person p = {name:"Diayasena"};
    return  p;
}

function testSafeAssignOpInAssignmentStatement7 () returns string {
    var p = check getPerson();
    return p.name;
}


function readLineError() returns string | myerror {
    myerror e = { message: "io error" };
    return e;
}

function readLineCustomError() returns string | customError {
    customError e = { message: "custom io error", data: "foo.txt"};
    return e;
}

function readLineSuccess() returns string | myerror {
    return "Ballerina";
}

function testCheckExprInBinaryExpr1() returns error? {
    string str = "hello, " + check readLineError();
    return ();
}

function testCheckExprInBinaryExpr2() returns myerror? {
    string str = "hello, " + check readLineError();
    return ();
}

function testCheckExprInBinaryExpr3() returns string | customError {
    string str = "hello, " + check readLineSuccess();
    return str;
}

function testCheckExprInBinaryExpr4() {
    string str = "hello, " + check readLineError();
}

function testCheckExprInBinaryExpr5() {
    string str = "hello, " + check readLineError();
}

function testCheckExprInBinaryExpr6() returns string | customError {
    string str = "hello, " + check readLineCustomError();
    return str;
}

// This test case should throw an error since customError is not assignable to the myerror
function testCheckExprInBinaryExpr7() returns string | customError {
    string str = "hello, " + check readLineError();
    return str;
}

function readLineProper() returns string | myerror | customError {
    return "Hello, World!!!";
}

function testCheckExprInBinaryExpr8() returns string {
    string str = "hello, " + check readLineProper();
    return str;
}

function foo(string s) returns string | customError {
    return "(" + s + "|" + s + ")";
}

function bar(string s1, string s2) returns string | customError  {
    return s1 + " " + s2;
}

function testCheckedExprAsFuncParam1() returns string | error  {
    return check bar(check bar(check foo(check foo(check foo(check foo("S")))),
                check foo(check foo("A"))) ,
                    check bar(check foo(check foo(check foo("M"))), "done"));
}