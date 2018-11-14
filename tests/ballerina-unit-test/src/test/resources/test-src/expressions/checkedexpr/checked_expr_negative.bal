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

public type MyError error<string, record { int code; }>;

public type CustomError error<string, record { int code; string data; }>;

function readLine() returns MyError | CustomError {
    MyError e = error("io error");
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

function readLineProper() returns string | MyError | CustomError {
    MyError e = error("io error");
    return e;
}

// This will be a negative case only after implementing the compiler check to validate if an error is returned  from any
// function or resource that uses check expression.
function testCheckedExprSemanticErrors5() {
    string line = check readLineProper();
}
