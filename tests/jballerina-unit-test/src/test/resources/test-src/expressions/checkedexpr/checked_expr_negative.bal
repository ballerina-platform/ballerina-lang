public type MyError error<record { int code; string message?; error cause?; }>;

public type CustomError error<record { int code; string data; string message?; error cause?;}>;

function readLineProper() returns string | MyError | CustomError {
    MyError e = error MyError("io error", code = 0);
    return e;
}

function testCheckedExprSemanticErrors5() {
    string line = check readLineProper();
}

function testCheckedExprErrors() returns error? {
    string line = check readLineError();
    return;
}

function readLineError() returns error {
    error e = error("io error");
    return e;
}

function testCheckedExprErrors2() returns error? {
    string line = check readLine();
    return;
}

function readLine() returns MyError | CustomError {
    MyError e = error MyError("io error", code = 0);
    return e;
}
