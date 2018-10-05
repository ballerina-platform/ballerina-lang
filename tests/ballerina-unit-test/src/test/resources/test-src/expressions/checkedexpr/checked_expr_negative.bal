function readLineSuccess() returns string {
    return "Hello, World!!!";
}

function readLineError() returns error {
    error e = error("io error");
    return e;
}

function testCheckedExprSemanticErrors1() {
    string line = check readLineSuccess();
}

function testCheckedExprSemanticErrors2() {
    string line = check readLineError();
}

public type myerror error<string, record { int code; }>;

public type customError error<string, record { int code; string data; }>;

function readLine() returns myerror | customError {
    myerror e = error("io error");
    return e;
}

function testCheckedExprSemanticErrors3() {
    string line = check readLine();
}

function readLineInternal() returns string | int {
    return "Hello, World!!!";
}

function testCheckedExprSemanticErrors4() {
    string line = check readLineInternal();
}

function readLineProper() returns string | myerror | customError {
    myerror e = error("io error");
    return e;
}

function testCheckedExprSemanticErrors5() {
    string line = check readLineProper();
}
