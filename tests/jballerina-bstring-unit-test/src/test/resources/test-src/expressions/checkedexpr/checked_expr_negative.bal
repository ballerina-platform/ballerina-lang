public type MyError error<string, record { int code; string message?; error cause?; }>;

public type CustomError error<string, record { int code; string data; string message?; error cause?;}>;

function readLineProper() returns string | MyError | CustomError {
    MyError e = error("io error", code = 0);
    return e;
}

function testCheckedExprSemanticErrors5() {
    string line = check readLineProper();
}
