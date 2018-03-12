import ballerina.runtime;

public struct testError {
    string message;
    error cause;
    string code;
}

public struct testDataError {
    string message;
    error cause;
    string data;
}

public struct testInputError {
    string message;
    error cause;
    string input;
}

function testTryCatch (int value) (string) {
    string path = "start ";
    try {
        path = path + "insideTry ";
        try {
            path = path + "insideInnerTry ";
            if (value > 10) {
                path = path + "onError ";
                testError tError = {message:"error", code:"test"};
                throw tError;
            } else if (value < 0) {
                path = path + "onInputError ";
                testInputError tError = {message:"error", input:"0"};
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

function testFunctionThrow (int arg) (boolean, string) {
    string a = "0";
    try {
        a = a + "1";
        int b = testThrow(arg);
        a = a + "2";
    } catch (error b) {
        a = a + "3";
        return true, a;
    }
    a = a + "4";
    return false, a;
}

function testThrow (int a) (int) {
    int c = a + 10;
    return testNestedThrow(c);
}

function testNestedThrow (int a) (int) {
    error e = {message:"test message"};
    int i = 10;
    if (i == 10) {
        throw e;
    }
    return i;
}

function testUncaughtException () {
    _ = testNestedThrow(1);
}

function testStackTrace () (runtime:CallStackElement[]) {
    runtime:CallStackElement[] trace;
    try {
        testUncaughtException();
    } catch (error e) {
        trace = runtime:getErrorCallStack(e);
    }
    return trace;
}

function mockFunction () (string) {
    return "done";
}

function testMethodCallInFinally () (string) {
    string s = "start";
    try {
        error e = {message:"test"};
        throw e;
    } finally {
        s = s + mockFunction();
    }
    return s;
}

function scopeIssueTest () (int) {
    int i = 0;
    while (i < 10) {
        try {
        } catch (error e) {
        }
        i = i + 1;
    }
    int j6 = 5;
    while (i < 20) {
        int val = j6;
        i = i + 1;
    }
    j6 = i + j6;
    return j6;
}

function testTryWithinWhile () (int) {
    int i = 0;
    while (i < 3) {
        try {
            int o = 0;
        } catch (error e) {

        }
        i = i + 1;
    }
    return i;
}
