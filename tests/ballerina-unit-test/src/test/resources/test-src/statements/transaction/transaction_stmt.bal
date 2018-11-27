final int RETRYCOUNT = 4;
final int RETRYCOUNT_2 = -4;

string workerTest = "";

function testTransactionStmt() returns (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }

    a = a + " end";
    return a;
}

function testTransactionStmtAbort(int i) returns (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
        if (i == 0) {
            a = a + " abort";
            abort;
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionStmtFailed(int i) returns (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
        if (i == -1) {
            error err = error(" err" );
            panic err;
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionStmtTrap(int i) returns (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
        int|error ret = trap mockError(i);
        if (ret is int) {
            a = a + " int";
        } else {
            a = a + " trxErr";
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function mockError(int i) returns int {
    if (i == 0) {
        error err = error(" err" );
        panic err;
    }
    return 0;
}

function testOptionalOnretry() returns (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
        a = a + " endTrx";
    }
    a = a + " end";
    return a;
}

function testOptionalOnretryWithAbort(int i) returns (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
        if (i == 0) {
            a = a + " abort";
            abort;
        }
        a = a + " endTrx";
    }
    a = a + " end";
    return a;
}

function testOptionalOnretryWithFail(int i) returns (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
        a = a + " endTrx";
    }
    a = a + " end";
    return a;
}

function testOptionalOnretryWithTrap(int i) returns (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
        int|error ret = trap mockError(i);
        if (ret is int) {
            a = a + " int";
        } else {
            a = a + " trxErr";
        }
        a = a + " endTrx";
    }
    a = a + " end";
    return a;
}

function testNestedTransaction() returns (string) {
    string a = "start";
    transaction {
        a = a + " inOuterTrx";
        transaction {
            a = a + " inInnerTrx";
            a = a + " endInnerTrx";
        }
        a = a + " endOuterTrx";
    }
    a = a + " end";
    return a;
}

function testNestedTransactionInnerAbort(int i) returns (string) {
    string a = "start";
    transaction {
        a = a + " inOuterTrx";
        transaction {
            a = a + " inInnerTrx";
            if (i == 0) {
                a = a + " abort";
                abort;
            }
            a = a + " endInnerTrx";
        }
        a = a + " endOuterTrx";
    }
    a = a + " end";
    return a;
}

function testNestedTransactionInnerTrap(int i) returns (string) {
    string a = "start";
    transaction {
        a = a + " inOuterTrx";
        transaction {
            a = a + " inInnerTrx";
            int|error ret = trap mockError(i);
            if (ret is int) {
                a = a + " int";
            } else {
                a = a + " trxErr";
            }
            a = a + " endInnerTrx";
        }
        a = a + " endOuterTrx";
    }
    a = a + " end";
    return a;
}

function testNestedTransactionInnerPanic(int i) returns (string) {
    string a = "start";
    transaction {
        a = a + " inOuterTrx";
        transaction {
            a = a + " inInnerTrx";
            if (i == 0) {
                error err = error(" err" );
                panic err;
            }
            a = a + " endInnerTrx";
        }
        a = a + " endOuterTrx";
    }
    a = a + " end";
    return a;
}

function testNestedTransactionWithFailed(int i) returns (string) {
    string a = "start";
    transaction with retries = 3 {
        a = a + " inOuterTrx";
        transaction with retries = 2 {
            a = a + " inInnerTrx";
            int|error ret = trap mockError(i);
            if (ret is int) {
                a = a + " int";
            } else {
                a = a + " trxErr";
            }
            a = a + " endInnerTrx";
        } onretry {
            a = a + " innerFailed";
        }
        a = a + " endOuterTrx";
    } onretry {
        a = a + " outerFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionStmtAbortAndNonDefaultRetries(int i) returns (string) {
    string a = "start";
    transaction with retries = 4 {
        a = a + " inTrx";
        if (i == 0) {
            a = a + " abort";
            abort;
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithFailedAndNonDefaultRetries(int i) returns (string) {
    string a = "start";
    transaction with retries = 4 {
        a = a + " inTrx";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithTrapAndNonDefaultRetries(int i) returns (string) {
    string a = "start";
    transaction with retries = 4 {
        a = a + " inTrx";
        int|error ret = trap mockError(i);
        if (ret is int) {
            a = a + " int";
        } else {
            a = a + " trxErr";
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionStmtSuccessAndNonDefaultRetries(int i) returns (string) {
    string a = "start";
    transaction with retries = 4 {
        a = a + " inTrx";
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithRetryOff(int i) returns (string) {
    string a = "start";
    transaction with retries = 0 {
        a = a + " inTrx";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithConstRetryFailed() returns (string) {
    string a = "start";
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
    a = a + " end";
    return a;
}

function testTransactionStmtWithConstRetryFailed2() returns (string) {
    string a = "start ";
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
    a = a + " end";
    return a;
}

function testTransactionStmtWithConstRetrySuccess() returns (string) {
    string a = "start";
    transaction with retries = RETRYCOUNT {
        a = a + " inTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtSuccess() returns (string) {
    string a = "start";
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
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtFailed1() returns (string) {
    string a = "start";
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
    a = a + " inSecTrxEnd";
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtFailed2() returns (string) {
    string a = "start";
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
    a = a + " inSecTrxEnd";
    a = a + " end";
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
    transaction with retries = 4 {
        a = a + " inOuterTxstart ";
        transaction {
            a = a + " inInnerTxstart ";
            a = a + testReturn();
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
