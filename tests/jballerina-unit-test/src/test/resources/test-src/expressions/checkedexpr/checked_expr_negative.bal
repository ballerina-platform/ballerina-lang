public type MyError error<record { int code; string message?; error cause?; }>;

public type CustomError error<record { int code; string data; string message?; error cause?;}>;

function readLineProper() returns string | MyError | CustomError {
    MyError e = MyError("io error", code = 0);
    return e;
}

function testCheckedExprSemanticErrors5() {
    string line = check readLineProper();
}
