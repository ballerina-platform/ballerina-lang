

function readLineSuccess() returns string {
    return "Hello, World!!!";
}

function readLineError() returns error {
    error e = {message:"io error"};
    return e;
}

function testCheckedExprSemanticErrors1() {
    string line = check readLineSuccess();
}

function testCheckedExprSemanticErrors2() {
    string line = check readLineError();
}

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

function readLine() returns myerror | customError {
    myerror e = {message:"io error"};
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
    myerror e = {message:"io error"};
    return e;
}

function testCheckedExprSemanticErrors5() {
    string line = check readLineProper();
}

