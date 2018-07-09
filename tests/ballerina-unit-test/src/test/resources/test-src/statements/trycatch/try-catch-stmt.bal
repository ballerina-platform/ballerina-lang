import ballerina/runtime;

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

function testTryCatch (int value) returns (string) {
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

function testFunctionThrow (int arg) returns (boolean, string) {
    string a = "0";
    try {
        a = a + "1";
        int b = testThrow(arg);
        a = a + "2";
    } catch (error b) {
        a = a + "3";
        return (true, a);
    }
    a = a + "4";
    return (false, a);
}

function testThrow (int a) returns (int) {
    int c = a + 10;
    return testNestedThrow(c);
}

function testNestedThrow (int a) returns (int) {
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

function testErrorCallStackFrame () returns (runtime:CallStackElement, runtime:CallStackElement) {
    runtime:CallStackElement trace1 = {}; 
    runtime:CallStackElement trace2 = {};
    try {
        testUncaughtException();
    } catch (error e) {
        trace1 = runtime:getErrorCallStackFrame(e); 
        trace2 = runtime:getErrorCallStackFrame(e.cause);
    }
    return (trace1, trace2);
}

function mockFunction () returns (string) {
    return "done";
}

function testMethodCallInFinally () returns (string) {
    string s = "start";
    try {
        error e = {message:"test"};
        throw e;
    } finally {
        return s + mockFunction();
    }
}

function scopeIssueTest () returns (int) {
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

function testTryWithinWhile () returns (int) {
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

function testThrowInFinallyWithReturnInTry() returns int {
    int catchExecs = 0;
    int finallyExecs = 0;
    try {
        return 4;
    } catch(error e) {
        catchExecs += 1;
        error catchErr = { message: "number of catch executions: " + catchExecs  };
        throw catchErr;
    } finally {
        finallyExecs += 1;
        error finErr = { message: "number of finally executions: " + finallyExecs  };
        throw finErr;
    }
}

function testThrowInFinallyWithReturnInCatch() returns int {
    int finallyExecs = 0;
    try {
        error tryErr = { message: "error thrown in try" };
        throw tryErr;
    } catch(error e) {
        return 8;
    } finally {
        finallyExecs += 1;
        error finErr = { message: "number of finally executions: " + finallyExecs  };
        throw finErr;
    }
}

function testThrowInFinallyWithReturnInTryBranches(int i) returns int {
    int catchAndfinallyExecs = 0;
    try {
        if (i > 0) {
            return i;
        }
        int j = 5/0;
        return j;
    } catch(error e) {
        catchAndfinallyExecs += 1;
        return 4;
    } finally {
        catchAndfinallyExecs += 1;
        error finErr = { message: "number of catch and finally executions: " + catchAndfinallyExecs };
        throw finErr;
    }
}

function testReturnInFinallyWithThrowInTryAndFinally() returns int {
    int catchAndfinallyExecs = 0;
    try {
        error abc = {};
        if (true) {
            throw abc;
        }
        return 4;
    } catch (error e) {
        catchAndfinallyExecs += 1;
        return 22;
    } finally {
        catchAndfinallyExecs += 1;
        if (true) {
            error finErr = { message: "number of catch and finally executions: " + catchAndfinallyExecs };
            throw finErr;
        }
        return 40;
    }
}

function nestedTryCatchFinallyWithReturns(int i) returns int {
    int total = 0;
    try {
        if (i < 5) {
            total += 10;
            ErrorOne err = { message:"Try Block Error One" };
            throw err;
        } else if (i < 10) {
            total += 15;
            ErrorTwo err = { message:"Try Block Error Two" };
            throw err;
        }
        total += 20;
        return 4;
    } catch (ErrorOne e) {
        if (i < 5) {
            total += 25;
            ErrorOne err = { message:"Error One - Catch Block Error One" };
            throw err;
        } else if (i < 10) {
            total += 30;
            ErrorTwo err = { message:"Error One - Catch Block Error Two" };
            throw err;
        }
        total += 35;
        return 22;
    } catch (ErrorTwo e) {
        if (i < 5) {
            total += 40;
            ErrorOne err = { message:"Error Two - Catch Block Error One" };
            throw err;
        } else if (i < 10) {
            total += 45;
            ErrorTwo err = { message:"Error Two - Catch Block Error Two" };
            throw err;
        }
        total += 50;
        return 24;
    } catch (error e) {
        total += 55;
        error abc = { message: "Error - Catch Block Error" };
        if (true) {
            throw abc;
        }
        total += 60;
        return 26;
    } finally {
        try {
            try {
                try {
                    if (i < 5) {
                        total += 65;
                        ErrorOne err = { message:"Extreme Inner Try Block Error One" };
                        throw err;
                    } else if (i < 10) {
                        total += 70;
                        ErrorTwo err = { message:"Extreme Inner Try Block Error Two" };
                        throw err;
                    }
                    total += 75;
                    return 4;
                } catch (ErrorOne e) {
                    if (i < 5) {
                        total += 80;
                        ErrorOne err = { message:"Error One - Extreme Inner Catch Block Error One" };
                        throw err;
                    } else if (i < 10) {
                        total += 85;
                        ErrorTwo err = { message:"Error One - Extreme Inner Catch Block Error Two" };
                        throw err;
                    }
                    total += 90;
                    return 22;
                } catch (ErrorTwo e) {
                    if (i < 5) {
                        total += 95;
                        ErrorOne err = { message:"Error Two - Extreme Inner Catch Block Error One" };
                        throw err;
                    } else if (i < 10) {
                        total += 100;
                        ErrorTwo err = { message:"Error Two - Extreme Inner Catch Block Error Two" };
                        throw err;
                    }
                    total += 105;
                    return 24;
                } catch (error e) {
                    total += 110;
                    error abc = { message: "Error - Extreme Inner Catch Block Error" };
                    if (true) {
                        throw abc;
                    }
                    total += 115;
                    return 26;
                } finally {
                    total += 120;
                    error abc = { message: "Extreme Inner Finally Block Error" };
                    if (i > 5) {
                        throw abc;
                    }
                    total += 125;
                    return 40;
                }
            } catch (error e) {
                total += 130;
                error abc = { message: "Inner Catch Block Error" };
                if (true) {
                    throw abc;
                }
                total += 135;
                return 30;
            } finally {
                total += 140;
                error abc = { message: "Inner Finally Block Error" };
                if (i > 5) {
                    throw abc;
                }
                total += 145;
                return 40;
            }
        } catch (error e) {
            total += 150;
            error abc = { message: "Outer Catch Block Error" };
            if (true) {
                throw abc;
            }
            total += 155;
            return 60;
        } finally {
            total += 160;
            error abc = { message: "Outer Catch Block Error " + <string>total };
            if (true) {
                throw abc;
            }
            total += 165;
            return 80;
        }
    }
}

public type ErrorOne record {
    string message;
    error? cause;
};

public type ErrorTwo record {
    string message;
    error? cause;
};
