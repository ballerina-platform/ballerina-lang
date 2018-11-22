public type TrxErrorData record {
    string message = "";
    error? cause = ();
    string data = "";
    !...
};

public type TrxError error<string, TrxErrorData>;

@final int RETRYCOUNT = 4;
@final int RETRYCOUNT_2 = -4;

string workerTest = "";

function testTransactionStmt(int i) returns (string) {
    string a = "start";
    var result = trap testTransactionStmtHelper2(a, i);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a = a + <string>result.reason();
    }
    a = a + " end";
    return a;
}

function testTransactionStmtHelper1(string status, int i) returns string {
    string a = status;
    if (i == -1) {
        error err = error(" err" );
        panic err;
    } else if (i < -1) {
        TrxError err = error(" trxErr", { data: "test" });
        panic err;
    }
    return a;
}

function testTransactionStmtHelper2(string status, int i) returns string {
    string a = status;
    transaction {
        a = a + " inTrx";
        if (i == 0) {
            a = a + " abort";
            abort;
        } else {
            var result = trap testTransactionStmtHelper1(a, i);
            if (result is string) {
                a = result;
            } else if (result is error) {
                a = a + < string > result.reason();
            }
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    return a;
}

function testAbortStatement() returns (string) {
    string str = "BeforeTR ";
    int i = 0;
    transaction {
        str = str + "WithinTR ";
        if (i == 0) {
            str = str + "BeforAbort ";
            abort;
        }
        str = str + "AfterIf ";
    }
    str = str + "AfterTR ";
    return str;
}


function testOptionalFailed(int i) returns (string) {
    string a = "start";
    var result = trap testOptionalFailedHelper2(i, a);
    if (result is string) {
        a = result;
    } else if (result is TrxError) {
        a += <string>result.reason();
    }
    a = a + " end";
    return a;
}

function testOptionalFailedHelper1(int i, string status) returns string {
    string a = status;
    if (i == -1) {
        error err = error(" err" );
        panic err;
    } else if (i < -1) {
        TrxError err = error(" trxErr", { data: "test" });
        panic err;
    }
    return a;
}

function testOptionalFailedHelper2(int i, string status) returns string {
    string a = status;
    transaction {
        a = a + " inTrx";
        if (i == 0) {
            a = a + " abort";
            abort;
        } else {
            var result = trap testOptionalFailedHelper1(i, a);
            if (result is string) {
                a = result;
            } else if (result is TrxError) {
                a += <string>result.reason();
            }
        }
        a = a + " endTrx";
    }
    return a;
}

function testNestedTransaction(int i) returns (string) {
    string a = "start";
    var result = trap testNestedTransactionHelper2(i, a);
    if (result is string) {
        a = result;
    } else if (result is TrxError) {
        a += <string>result.reason();
    }
    a = a + " end";
    return a;
}

function testNestedTransactionHelper1(int i, string status) returns string {
    string a = status;
    if (i == -1) {
        error err = error(" err" );
        panic err;
    } else if (i < -1) {
        TrxError err = error(" trxErr", { data: "test" });
        panic err;
    }
    return a;
}

function testNestedTransactionHelper2(int i, string status) returns string {
    string a = status;
    transaction {
        a = a + " inOuterTrx";
        transaction {
            a = a + " inInnerTrx";
            if (i == 0) {
                a = a + " abort";
                abort;
            } else {
                var result = trap testNestedTransactionHelper1(i, a);
                if (result is string) {
                    a = result;
                } else if (result is TrxError) {
                    a += <string>result.reason();
                }
            }
            a = a + " endInnerTrx";
        }
        a = a + " endOuterTrx";
    }
    a = a + " ";
    return a;
}

function testNestedTransactionWithFailed(int i) returns (string) {
    string a = "start";
    var result = trap testNestedTransactionWithFailedHelper2(i, a);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a += <string>result.reason();
    }
    a = a + " end";
    return a;
}

function testNestedTransactionWithFailedHelper1(int i, string status) returns string {
    string a = status;
    if (i == -1) {
        error err = error(" err" );
        panic err;
    } else if (i < -1) {
        TrxError err = error(" trxErr", { data: "test" });
        panic err;
    }
    return a;
}

function testNestedTransactionWithFailedHelper2(int i, string status) returns string {
    string a = status;
    transaction with retries = 3 {
        a = a + " inOuterTrx";
        transaction with retries = 2 {
            a = a + " inInnerTrx";
            if (i == 0) {
                a = a + " abort";
                abort;
            } else {
                var result = trap testNestedTransactionWithFailedHelper1(i, a);
                if (result is string) {
                    a = result;
                } else if (result is TrxError) {
                    a += <string>result.reason();
                }
            }
            a = a + " endInnerTrx";
        } onretry {
            a = a + " innerFailed";
        }
            a = a + " endOuterTrx";
        } onretry {
            a = a + " outerFailed";
        }
    a = a + " ";
    return a;
}

function testTransactionStmtWithFailedAndNonDefaultRetries(int i) returns (string) {
    string a = "start";
    var result = trap testTransactionStmtWithFailedAndNonDefaultRetriesHelper1(i, a);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a = a + result.reason();
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithFailedAndNonDefaultRetriesHelper1(int i, string status) returns string {
    string a = status;
    transaction with retries = 4 {
        a = a + " inTrx";
        if (i == 0) {
            a = a + " abort";
            abort;
        } else {
            var result = trap testTransactionStmtWithFailedAndNonDefaultRetriesHelper2(i, a);
            if (result is string) {
                a = result;
            } else {
                a = a + result.reason();
            }
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    return a;
}

function testTransactionStmtWithFailedAndNonDefaultRetriesHelper2(int i, string status) returns string {
    string a = status;
    if (i == -1) {
        error err = error(" err" );
        panic err;
    } else if (i < -1) {
        TrxError err = error(" trxErr", { data: "test" });
        panic err;
    } else {
        a = a + " success";
    }
    return status;
}

function testTransactionStmtWithRetryOff(int i) returns (string) {
    string a = "start";
    var result = trap testTransactionStmtWithRetryOffHelper2(i, a);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a += <string>result.reason();
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithRetryOffHelper1(int i) {
    if (i == -1) {
        error err = error(" err" );
        panic err;
    }
}

function testTransactionStmtWithRetryOffHelper2(int i, string status) returns string {
    string a = status;
    transaction with retries = 0 {
        a = a + " inTrx";
        var result = trap testTransactionStmtWithRetryOffHelper1(i);
        if (result is TrxError) {
            a += <string>result.reason();
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    return a;
}

function testTransactionStmtWithConstRetryFailed() returns (string) {
    string a = "start";
    var result = trap testTransactionStmtWithConstRetryFailedHelper(a);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a += result.reason();
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithConstRetryFailedHelper(string status) returns string {
    string a = status;
    int i = 0;
    transaction with retries = RETRYCOUNT {
        a = a + " inTrx";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
    } onretry {
        a = a + " inFailed";
    }
    return a;
}

function testTransactionStmtWithConstRetryFailed2() returns (string) {
    string a = "start ";
    var result = trap testTransactionStmtWithConstRetryFailed2Helper(a);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a = a + result.reason();
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithConstRetryFailed2Helper(string status) returns string {
    string a = status;
    int i = 0;
    transaction with retries = RETRYCOUNT_2 {
        a = a + " inTrx";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
    } onretry {
        a = a + " inFailed";
    }
    return a;
}

function testTransactionStmtWithConstRetrySuccess() returns (string) {
    string a = "start";
    var result = trap testTransactionStmtWithConstRetrySuccessHelper(a);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a += result.reason();
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithConstRetrySuccessHelper(string status) returns string {
    string a = status;
    transaction with retries = RETRYCOUNT {
        a = a + " inTrx";
    } onretry {
        a = a + " inFailed";
    }
    return a;
}

function testMultipleTransactionStmtSuccess() returns (string) {
    string a = "start";
    var result = trap testMultipleTransactionStmtSuccessHelper(a);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a += result.reason();
    }
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtSuccessHelper(string status) returns string {
    string a = status;
    transaction {
        a = a + " inFirstTrxBlock";
    } onretry {
        a = a + " inFirstTrxFailed";
    }
    a = a + " inFirstTrxEnd";
    transaction {
        a = a + " inSecTrxBlock";
    } onretry {
        a = a + " inSecTrxFailed";
    }
    a = a + " inFSecTrxEnd";
    return a;
}

function testMultipleTransactionStmtFailed1() returns (string) {
    string a = "start";
    var result = trap testMultipleTransactionStmtFailed1Helper(a);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a += result.reason();
    }
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtFailed1Helper(string status) returns string {
    string a = status;
    int i = 0;
    transaction with retries = 2 {
        a = a + " inFirstTrxBlock";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
    } onretry {
        a = a + " inFirstTrxFld";
    }
    a = a + " inFirstTrxEnd";
    transaction {
        a = a + " inSecTrxBlock";
    }
    a = a + " inFSecTrxEnd";
    return a;
}

function testMultipleTransactionStmtFailed2() returns (string) {
    string a = "start";
    int i = 0;
    var result = trap testMultipleTransactionStmtFailed2Helper(a);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a += result.reason();
    }
    transaction {
        a = a + " inSecTrxBlock";
    }
    a = a + " inFSecTrxEnd";
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtFailed2Helper(string status) returns string {
    int i = 0;
    string a = status;
    transaction with retries = 2 {
        a = a + " inFirstTrxBlock";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
    } onretry {
        a = a + " inFirstTrxFld";
    }
    a = a + " inFirstTrxEnd";
    return a;
}

function testAbort() returns (string) {
    string i = "st";
    transaction {
        i = i + " inOuterTrx";
        transaction {
            i = i + " inInnerTrx";
            abort;
        }
        i = i + " inOuterTrxEnd";
    }
    i = i + " afterOuterTrx";
    return i;
}

function transactionWithBreak() returns (string) {
    int i = 0;
    transaction {
        while (i < 5) {
            i = i + 1;
            if (i == 2) {
                break;
            }
        }
    }
    return "done";
}

function transactionWithContinue() returns (string) {
    int i = 0;
    transaction {
        while (i < 5) {
            i = i + 1;
            if (i == 2) {
                continue;
            }
        }
    }
    return "done";
}

function testTransactionStmtWithFail() returns (string) {
    string a = "start ";
    int i = 0;

    transaction with retries = 4 {
        a = a + " inTrx";
        if (i == 0) {
            retry;
        }
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}


function testSimpleNestedTransactionAbort() returns (string) {
    string a = "start ";
    int i = 0;
    transaction with retries = 4 {
        a = a + " inOuterTxstart ";
        transaction {
            a = a + " inInnerTxstart ";
            if (i == 0) {
                a = a + " abortingInnerTxstart ";
                abort;
            }
            a = a + " endInnerTxstart ";
        }
        a = a + " endOuterTxstart";
    }
    return a;
}

function testValidReturn() returns (string) {
    string a = "start ";
    int i = 0;
    transaction with retries = 4 {
        a = a + " inOuterTxstart ";
        transaction {
            a = a + " inInnerTxstart ";
            if (i == 0) {
                a = a + testReturn();
            }
            a = a + " endInnerTx";
        }
        a = a + testReturn();
        a = a + " endOuterTx";
    }
    return a;
}

function testReturn() returns (string) {
    return " foo";
}

function testValidDoneWithinTransaction() returns (string) {
    workerTest = "start ";
    int i = 0;
    transaction {
        workerTest = workerTest + " withinTx";
        testDone();
        workerTest = workerTest + " endTx";
    }
    workerTest = workerTest + " afterTx";
    return workerTest;
}

function testDone() {
    worker w1 {
        workerTest = workerTest + " withinworker";
        int i = 0;
        if (i == 0) {
            workerTest = workerTest + " beforeDone";
            done;
        }
    }
}
