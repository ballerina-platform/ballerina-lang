function readLineSuccess() returns string {
    return "Hello, World!!!";
}

function testCheckedExprSemanticErrors1() returns error? {
    string line = check readLineSuccess();
    return ();
}

public type MyError error<record { int code; string message?; error cause?; }>;

public type CustomError error<record { int code; string data; string message?; error cause?;}>;

function readLineInternal() returns string | int {
    return "Hello, World!!!";
}

function testCheckedExprSemanticErrors4() returns error? {
    string line = check readLineInternal();
    return ();
}

function readLineProper() returns string | MyError | CustomError {
    MyError e = error MyError("io error", code = 0);
    return e;
}

function testCheckedExprSemanticErrors5() {
    string line = check readLineProper();
}

function testCheckedExprSemanticErrors6() returns error? {
    string|error line = readLineSuccess();
    check line;
}

function testCheckedExprWithNoErrorType1() {
    int i = 10;
    int j = check i;
}

function testCheckedExprWithNoErrorType2() {
    int i = 10;
    int j = getInt(check 10) + check i;
}

function getInt(int x) returns int {
    return x + 1;
}
