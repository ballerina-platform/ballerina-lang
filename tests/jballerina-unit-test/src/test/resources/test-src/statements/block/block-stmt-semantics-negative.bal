public type testError record {|
    string message;
    error cause;
    string code?;
|};

function testRedeclareFunctionArgument (int value) returns (string) {
    int value = 11;
    if (value > 10) {
        testError tError = {message: "error", cause: error("errorMsg", code = "test")};
        return "unreachable throw";
        panic tError.cause;
    }
    return "done";
}

function testRedeclareFunctionParameterInBlockStmt(int value) returns string {
    {
        int value = 11;
        testError tError = {message: "error", cause: error("errorMsg", code = "test")};
        return "unreachable throw";
        panic tError.cause;
    }
    return "done";
}

function testRedeclareVariableInBlockStmt() returns (string) {
    {
        int value = 5;
        {
            int value = 11;
            testError tError = {message: "error", cause: error("errorMsg", code = "test")};
            return "unreachable throw";
            panic tError.cause;
        }
    }
    return "done";
}
