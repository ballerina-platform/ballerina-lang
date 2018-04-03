
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
    b =? openFileSuccess("/home/sameera/foo.txt");
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

struct FileOpenStatus {
    boolean status = false;
}

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

struct person {
    string name;
}

public struct myerror {
    string message;
    error[] cause;
    int code;
}

public struct customError {
    string message;
    error[] cause;
    int code;
    string data;
}

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

function readLineSuccess() returns string | myerror {
    return "Ballerina";
}

function testCheckExprInBinaryExpr1() returns error? {
    string str = "hello, " + check readLineError();
    return ();
}

function testCheckExprInBinaryExpr2() returns customError? {
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
