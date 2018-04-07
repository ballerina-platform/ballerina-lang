public type TrxError {
    string message;
    error[] cause;
    string data;
};

string a = "";

function testTransactionStmtWithNoHandlers () returns (string) {
    a = "";
    a = (a +  "start");
    try {
        transaction {
            a = a + " inTrx";
            int i = 0;
            a = a + " endTrx";
        } onretry {
            a = a + " inFailed";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = (a + " end");
    return a;
}

function testTransactionStmtCommitWithCommitHandler () returns (string) {
    a = "";
    a = a +  "start";
    try {
        transaction with oncommit=commitFunction{
            a = a + " inTrx";
            int i = 0;
            a = a + " endTrx";
        } onretry {
            a = a + " inFailed";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testTransactionAbortStmtWithAbortHandler () returns (string) {
    a = "";
    a = a +  "start";
    try {
        transaction with onabort=abortFunction{
            a = a + " inTrx";
            int i = 0;
            if (i == 0) {
                abort;
            }
            a = a + " endTrx";
        } onretry {
            a = a + " inFailed";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testTransactionAbortStmtWithNoAbortHandler () returns (string) {
    a = "";
    a = a +  "start";
    try {
        transaction {
            a = a + " inTrx";
            int i = 0;
            if (i == 0) {
                abort;
            }
            a = a + " endTrx";
        } onretry {
            a = a + " inFailed";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}


function testTransactionAbortStmtWithCommitHandler () returns (string) {
    a = "";
    a = a +  "start";
    try {
        transaction with oncommit=commitFunction{
            a = a + " inTrx";
            int i = 0;
            if (i == 0) {
                abort;
            }
            a = a + " endTrx";
        } onretry {
            a = a + " inFailed";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testTransactionAbortStmtWithAllHandlers () returns (string) {
    a = "";
    a = a +  "start";
    try {
        transaction with retries=4, oncommit=commitFunction, onabort=abortFunction {
            a = a + " inTrx";
            int i = 0;
            if (i == 0) {
                abort;
            }
            a = a + " endTrx";
        } onretry {
            a = a + " inFailed";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testTransactionCommitStmtWithAllHandlers () returns (string) {
    a = "";
    a = a +  "start";
    try {
        transaction with retries=4, oncommit=commitFunction, onabort=abortFunction {
            a = a + " inTrx";
            int i = 0;
            a = a + " endTrx";
        } onretry {
            a = a + " inFailed";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testTransactionThrowWithAllHandlers () returns (string) {
    a = "";
    a = a +  "start";
    try {
        transaction with retries=4, oncommit=commitFunction, onabort=abortFunction{
            a = a + " inTrx";
            int i = 0;
            if (i == 0) {
                TrxError err = {message:" trxErr", data:"test"};
                throw err;
            }
            a = a + " endTrx";
        } onretry {
            a = a + " inFailed";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testTransactionCommitAfterFailureWithAllHandlers () returns (string) {
    int i = 0;
    a = "";
    a = a +  "start";
    try {
        transaction with retries=4, oncommit=commitFunction, onabort=abortFunction {
            a = a + " inTrx";
            if (i < 2) {
                TrxError err = {message:" trxErr", data:"test"};
                throw err;
            }
            a = a + " endTrx";
        } onretry {
            i = i + 1;
            a = a + " inFailed";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testMultipleTransactionsWithAllHandlers () returns (string) {
    a = "";
    a = a +  "start";
    transaction with retries=4, oncommit=commitFunction, onabort=abortFunction{
        a = a + " inFirstTrx";

        a = a + " endFirstTrx";
    } onretry {
        a = a + " beforeRetry-First";
    }

    transaction with retries=4, oncommit=commitFunctionSecond, onabort=abortFunctionSecond {
        a = a + " inSecondTrx";
        int i = 0;
        a = a + " endSecondTrx";
    } onretry {
        a = a + " beforeRetry-second";
    }

    a = a + " end";
    return a;
}

function testMultipleTransactionsFailedWithAllHandlers () returns (string) {
    a = "";
    a = a +  "start";
    int i = 0;
    try {
        transaction with retries=0, oncommit=commitFunction, onabort=abortFunction {
            a = a + " inFirstTrx";
            if (i == 0) {
                TrxError err = {message:" trxErr", data:"test"};
                throw err;
            }
            a = a + " endFirstTrx";
        } onretry {
            a = a + " beforeRetry-First";
        }
    } catch (error err) {
        a = a + err.message;
    }

    transaction with retries=4, oncommit=commitFunctionSecond, onabort=abortFunctionSecond {
        a = a + " inSecondTrx";
        if (i == 0) {
            abort;
        }
        a = a + " endSecondTrx";
    } onretry {
        a = a + " beforeRetry-second";
    }

    a = a + " end";
    return a;
}

function commitFunction() {
    a = a + " incommitFunction";
}

function abortFunction() {
    a = a + " inAbortFunction";
}

function commitFunctionSecond() {
    a = a + " incommitFunctionSecond";
}


function abortFunctionSecond() {
    a = a + " inAbortFunctionSecond";
}
