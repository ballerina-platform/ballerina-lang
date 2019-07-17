public type TrxErrorData record {|
    string message = "";
    error cause?;
    string data = "";
|};

public type TrxError error<string, TrxErrorData>;

final int RETRYCOUNT = 4;
final int RETRYCOUNT_2 = -4;

string workerTest = "";

int attemptCount = 0;
function testTransactionStmt(int i) returns (string) {
    string a = "start";
    var result = trap testTransactionStmtHelper2(a, i);
    if (result is string) {
        a = result;
    } else {
        a = a + <string>result.reason();
    }
    a = a + " rc:" + attemptCount.toString() + " end";
    return a;
}

function testTransactionStmtHelper1(string status, int i) returns string {
    string a = status;
    if (i == -1) {
        error err = error(" err" );
        panic err;
    } else if (i < -1) {
        TrxError err = error(" trxErr", data = "test");
        panic err;
    }
    return a;
}

function testTransactionStmtHelper2(string status, int i) returns string {
    string a = status;
    attemptCount = 0;
    transaction {
        attemptCount += 1;
        a = a + " inTrx";
        if (i == 0) {
            a = a + " abort";
            abort;
        } else {
            var result = testTransactionStmtHelper1(a, i);
            a = result;
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
        TrxError err = error(" trxErr", data = "test");
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
            } else {
                a += <string>result.reason();
            }
        }
        a = a + " endTrx";
    }
    return a;
}

function testTransactionStmtWithFailedAndNonDefaultRetries(int i) returns (string) {
    string a = "start";
    attemptCount = 0;
    var result = trap testTransactionStmtWithFailedAndNonDefaultRetriesHelper1(i, a);
    if (result is string) {
        a = result;
    } else {
        a = a + result.reason();
    }
    a = a + " rc:" + attemptCount.toString() + " end";
    return a;
}

function testTransactionStmtWithFailedAndNonDefaultRetriesHelper1(int i, string status) returns string {
    string a = status;
    transaction with retries = 4 {
        attemptCount += 1;
        a = a + " inTrx";
        if (i == 0) {
            a = a + " abort";
            abort;
        } else {
            var result = testTransactionStmtWithFailedAndNonDefaultRetriesHelper2(i, a);
            a += result;
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
        TrxError err = error(" trxErr", data = "test");
        panic err;
    } else {
        a = a + " success";
    }
    return a;
}

function testTransactionStmtWithRetryOff(int i) returns (string) {
    string a = "start";
    var result = trap testTransactionStmtWithRetryOffHelper2(i, a);
    if (result is string) {
        a = result;
    } else {
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
        if (result is error) {
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
    attemptCount = 0;
    var result = trap testTransactionStmtWithConstRetryFailedHelper(a);
    if (result is string) {
        a = result;
    } else {
        a += result.reason();
    }
    a = a + " rc:" + attemptCount.toString() + " end";
    return a;
}

function testTransactionStmtWithConstRetryFailedHelper(string status) returns string {
    string a = status;
    int i = 0;
    transaction with retries = RETRYCOUNT {
        attemptCount += 1;
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
    } else {
        a = a + <string>result.detail()["message"];
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
    } else {
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
    } else {
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

string failingTrxLog = "";
function testMultipleTransactionStmtFailed1() returns (string) {
    string a = "start";
    var result = trap testMultipleTransactionStmtFailed1Helper(a);
    if (result is string) {
        a = failingTrxLog;
    } else {
        a = failingTrxLog + result.reason();
    }
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtFailed1Helper(string status) returns string {
    failingTrxLog = status;
    int i = 0;
    transaction with retries = 2 {
        failingTrxLog = failingTrxLog + " inFirstTrxBlock";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
    } onretry {
        failingTrxLog = failingTrxLog + " inFirstTrxFld";
    } aborted {
        failingTrxLog += " aborted";
    }
    failingTrxLog = failingTrxLog + " inFirstTrxEnd";
    transaction {
        failingTrxLog = failingTrxLog + " inSecTrxBlock";
    }
    failingTrxLog = failingTrxLog + " inFSecTrxEnd";
    return failingTrxLog;
}

string log2 = "";
function testMultipleTransactionStmtFailed2() returns (string) {
    log2 = "start";
    int i = 0;
    var result = trap testMultipleTransactionStmtFailed2Helper(log2);
    if (result is string) {
        log2 = result;
    } else {
        log2 += result.reason();
    }
    transaction {
        log2 = log2 + " inSecTrxBlock";
    }
    log2 = log2 + " inFSecTrxEnd";
    log2 = log2 + " end";
    return log2;
}

function testMultipleTransactionStmtFailed2Helper(string status) returns string {
    int i = 0;
    log2 = status;
    transaction with retries = 2 {
        log2 = log2 + " inFirstTrxBlock";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
    } onretry {
        log2 = log2 + " inFirstTrxFld";
    }
    log2 = log2 + " inFirstTrxEnd";
    return log2;
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

function testValidReturn() returns (string) {
    string a = "start ";
    int i = 0;
    transaction with retries = 4 {
        a = a + " inOuterTxstart ";
        a = a + testReturn();
        a = a + " endOuterTx";
    }
    return a;
}

function testReturn() returns (string) {
    return " foo";
}
