function readLineSuccess() returns string {
    return "Hello, World!!!";
}

function readLineError() returns error {
    error e = error("io error");
    return e;
}

function testCheckedExprSemanticErrors1() returns error? {
    string line = check readLineSuccess();
    return ();
}

function testCheckedExprSemanticErrors2() returns error? {
    string line = check readLineError();
    return ();
}

public type MyError error<string, record { int code; string message?; error cause?; }>;

public type CustomError error<string, record { int code; string data; string message?; error cause?;}>;

function readLine() returns MyError | CustomError {
    MyError e = error("io error", code = 0);
    return e;
}

function testCheckedExprSemanticErrors3() returns error? {
    string line = check readLine();
    return ();
}

function readLineInternal() returns string | int {
    return "Hello, World!!!";
}

function testCheckedExprSemanticErrors4() returns error? {
    string line = check readLineInternal();
    return ();
}

function readLineProper() returns string | MyError | CustomError {
    MyError e = error("io error", code = 0);
    return e;
}

function testCheckedExprSemanticErrors5() {
    string line = check readLineProper();
}
