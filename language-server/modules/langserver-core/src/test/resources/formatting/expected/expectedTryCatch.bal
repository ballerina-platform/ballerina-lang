public type testError record {
    string message;
    error? cause;
    string code;
};

public type testDataError record {
    string message;
    error? cause;
    string data;
};

public type testInputError record {
    string message;
    error? cause;
    string input;
};

function testTryCatch(int value) returns (string) {
    string path = "start ";
    try {
        path = path + "insideTry ";
        try {
            path = path + "insideInnerTry ";
            if (value > 10) {
                path = path + "onError ";
                testError tError = {
                    message: "error",
                    code: "test"
                };
                throw tError;
            } else if (value < 0) {
                path = path + "onInputError ";
                testInputError tError = {
                    message: "error",
                    input: "0"
                };
                throw tError;
            }

            path = path + "endInsideInnerTry ";
        } catch (testError ex) {
            path = path + "innerTestErrorCatch:" + ex.code + " ";
            throw ex;
        } catch (testDataError e) {
            path = path + "innerDataErrorCatch:" + e.message + " ";
            throw e;
        } finally {
            path = path + "innerFinally ";
        }
        path = path + "endInsideTry ";
    } catch (error e) {
        path = path + "ErrorCatch ";
    } catch (testError ex) {
        path = path + "TestErrorCatch ";
    } finally {
        path = path + "Finally ";
    }
    path = path + "End";
    return path;
}
