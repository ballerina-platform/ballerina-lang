public type TrxErrorData record {
    string message = "";
    error? cause = ();
    string data = "";
    !...;
};

public type TrxError error<string, TrxErrorData>;

final int RETRYCOUNT = 4;
final int RETRYCOUNT_2 = -4;

string workerTest = "";

int attemptCount = 0;
function testTransactionStmt(int i) returns (string) {
    map<string> logMap = {"log":"start"};
    var result = trap testTransactionStmtHelper2(<string>logMap["log"], i);
    if (result is string) {
        logMap["log"] = result;
    } else if (result is error) {
        logMap["log"] = <string>logMap["log"] + <string>result.reason();
    }
    logMap["log"] = <string>logMap["log"] + " rc:" + attemptCount + " end";
    return <string>logMap["log"] ;
}

function testTransactionStmtHelper1(string status, int i) returns string {
    map<string> logMap = {"log":status};
    if (i == -1) {
        error err = error(" err" );
        panic err;
    } else if (i < -1) {
        TrxError err = error(" trxErr", { data: "test" });
        panic err;
    }
    return  <string>logMap["log"] ;
}

function testTransactionStmtHelper2(string status, int i) returns string {
    map<string> logMap = {"log":status};
    attemptCount = 0;
    transaction {
        attemptCount += 1;
        logMap["log"] = <string>logMap["log"] + " inTrx";
        if (i == 0) {
            logMap["log"] = <string>logMap["log"] + " abort";
            abort;
        } else {
            var result = testTransactionStmtHelper1(<string>logMap["log"], i);
            logMap["log"] = result;
        }
        logMap["log"] = <string>logMap["log"] + " endTrx";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " inFailed";
    }
    // test variables declaration after transaction block.
    string temp = <string>logMap["log"];
    return temp;
}

function testAbortStatement() returns (string) {
    map<string> logMap = {"log":"BeforeTR "};
    int i = 0;
    transaction {
        logMap["log"] = <string>logMap["log"] + "WithinTR ";
        if (i == 0) {
            logMap["log"] = <string>logMap["log"] + "BeforAbort ";
            abort;
        }
        logMap["log"] = <string>logMap["log"] + "AfterIf ";
    }
    logMap["log"] = <string>logMap["log"] + "AfterTR ";
    return <string>logMap["log"] ;
}


function testOptionalFailed(int i) returns (string) {
    map<string> logMap = {"log":"start"};
    var result = trap testOptionalFailedHelper2(i, <string>logMap["log"]);
    if (result is string) {
        logMap["log"] = result;
    } else if (result is TrxError) {
        logMap["log"] = <string>logMap["log"] + <string>result.reason();
    }
    logMap["log"] = <string>logMap["log"] + " end";
    return  <string>logMap["log"] ;
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
    map<string> logMap = { "log":status};
    transaction {
        logMap["log"] = <string>logMap["log"] + " inTrx";
        if (i == 0) {
            logMap["log"] = <string>logMap["log"] + " abort";
            abort;
        } else {
            var result = trap testOptionalFailedHelper1(i, <string>logMap["log"]);
            if (result is string) {
                logMap["log"] = result;
            } else if (result is TrxError) {
                logMap["log"] = <string>logMap["log"] + <string>result.reason();
            }
        }
         logMap["log"] = <string>logMap["log"] + " endTrx";
    }
    return  <string>logMap["log"] ;
}

function testTransactionStmtWithFailedAndNonDefaultRetries(int i) returns (string) {
    map<string> logMap = {"log":"start"};
    attemptCount = 0;
    var result = trap testTransactionStmtWithFailedAndNonDefaultRetriesHelper1(i, <string>logMap["log"]);
    if (result is string) {
       logMap["log"] = result;
    } else if (result is error) {
        logMap["log"] = <string>logMap["log"] + result.reason();
    }
    logMap["log"] = <string>logMap["log"] + " rc:" + attemptCount + " end";
    return <string>logMap["log"];
}

function testTransactionStmtWithFailedAndNonDefaultRetriesHelper1(int i, string status) returns string {
    map<string> logMap = {"log":status};
    transaction with retries = 4 {
        attemptCount += 1;
        logMap["log"] = <string>logMap["log"] + " inTrx";
        if (i == 0) {
            logMap["log"] = <string>logMap["log"] + " abort";
            abort;
        } else {
            var result = testTransactionStmtWithFailedAndNonDefaultRetriesHelper2(i,  <string>logMap["log"]);
            logMap["log"] = <string>logMap["log"] + result;
        }
        logMap["log"] = <string>logMap["log"] + " endTrx";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " inFailed";
    }
    return <string>logMap["log"];
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
    return a;
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
    map<string> logMap = {"log":status};
    transaction with retries = 0 {
        logMap["log"] = <string>logMap["log"] + " inTrx";
        var result = trap testTransactionStmtWithRetryOffHelper1(i);
        if (result is TrxError) {
             logMap["log"] =  <string>logMap["log"] + <string>result.reason();
        }
        logMap["log"] = <string>logMap["log"] + " endTrx";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " inFailed";
    }
    return <string>logMap["log"];
}

function testTransactionStmtWithConstRetryFailed() returns (string) {
    string a = "start";
    attemptCount = 0;
    var result = trap testTransactionStmtWithConstRetryFailedHelper(a);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a += result.reason();
    }
    a = a + " rc:" + attemptCount + " end";
    return a;
}

function testTransactionStmtWithConstRetryFailedHelper(string status) returns string {
    map<string> logMap = {"log":status};
    int i = 0;
    transaction with retries = RETRYCOUNT {
        attemptCount += 1;
        logMap["log"] = <string>logMap["log"] + " inTrx";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
    } onretry {
        logMap["log"] = <string>logMap["log"] + " inFailed";
    }
    return <string>logMap["log"];
}

function testTransactionStmtWithConstRetryFailed2() returns (string) {
    string a = "start ";
    var result = trap testTransactionStmtWithConstRetryFailed2Helper(a);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a = a + <string>result.detail().message;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithConstRetryFailed2Helper(string status) returns string {
    map<string> logMap = {"log":status};
    int i = 0;
    transaction with retries = RETRYCOUNT_2 {
        logMap["log"] = <string>logMap["log"] + " inTrx";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
    } onretry {
        logMap["log"] = <string>logMap["log"] + " inFailed";
    }
    return <string>logMap["log"];
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
    map<string> logMap = {"log":status};
    transaction with retries = RETRYCOUNT {
        logMap["log"] = <string>logMap["log"] + " inTrx";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " inFailed";
    }
    return <string>logMap["log"];
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
    map<string> logMap = {"log":status};
    transaction {
        logMap["log"] = <string>logMap["log"] + " inFirstTrxBlock";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " inFirstTrxFailed";
    }
    logMap["log"] = <string>logMap["log"] + " inFirstTrxEnd";
    transaction {
        logMap["log"] = <string>logMap["log"] + " inSecTrxBlock";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " inSecTrxFailed";
    }
    logMap["log"] = <string>logMap["log"] + " inFSecTrxEnd";
    return <string>logMap["log"];
}

map<string> failingTrxLog = {"log":""};
function testMultipleTransactionStmtFailed1() returns (string) {
    string a = "start";
    var result = trap testMultipleTransactionStmtFailed1Helper(a);
    if (result is string) {
        a =  <string>failingTrxLog["log"] ;
    } else if (result is error) {
        a =  <string>failingTrxLog["log"]  + result.reason();
    }
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtFailed1Helper(string status) returns string {
    failingTrxLog["log"]  = status;
    int i = 0;
    transaction with retries = 2 {
        failingTrxLog["log"] = <string>failingTrxLog["log"] +  " inFirstTrxBlock";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
    } onretry {
        failingTrxLog["log"] = <string>failingTrxLog["log"] + " inFirstTrxFld";
    } aborted {
        failingTrxLog["log"] = <string>failingTrxLog["log"] +  " aborted";
    }
    failingTrxLog["log"] = <string>failingTrxLog["log"] +  " inFirstTrxEnd";
    transaction {
        failingTrxLog["log"] = <string>failingTrxLog["log"] +  " inSecTrxBlock";
    }
    failingTrxLog["log"] = <string>failingTrxLog["log"] +  " inFSecTrxEnd";
    return <string>failingTrxLog["log"];
}

 map<string> logMap2 = {"log":""};
function testMultipleTransactionStmtFailed2() returns (string) {
    logMap2 = {"log":"start"};
    int i = 0;
    var result = trap testMultipleTransactionStmtFailed2Helper(<string>logMap2["log"]);
    if (result is string) {
        logMap2["log"] = result;
    } else if (result is error) {
        logMap2["log"] = <string>logMap2["log"] +  result.reason();
    }
    transaction {
        logMap2["log"] = <string>logMap2["log"]  + " inSecTrxBlock";
    }
    logMap2["log"] = <string>logMap2["log"] +  " inFSecTrxEnd";
    logMap2["log"] = <string>logMap2["log"] +  " end";
    return <string>logMap2["log"];
}

function testMultipleTransactionStmtFailed2Helper(string status) returns string {
    int i = 0;
    logMap2["log"] = status;
    transaction with retries = 2 {
        logMap2["log"] = <string>logMap2["log"] + " inFirstTrxBlock";
        if (i == 0) {
            error err = error(" err" );
            panic err;
        }
    } onretry {
        logMap2["log"] = <string>logMap2["log"] + " inFirstTrxFld";
    }
    logMap2["log"] = <string>logMap2["log"] + " inFirstTrxEnd";
    return <string>logMap2["log"];
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
    map<string> logMap = {"log":"start "}; 
    int i = 0;

    transaction with retries = 4 {
        logMap["log"] = <string>logMap["log"] + " inTrx";
        if (i == 0) {
            retry;
        }
    } onretry {
        logMap["log"] = <string>logMap["log"] + " inFailed";
    }
    logMap["log"] = <string>logMap["log"] + " end";
    return <string>logMap["log"];
}

function testValidReturn() returns (string) {
    map<string> logMap = {"log":"start "}; 
    int i = 0;
    transaction with retries = 4 {
        logMap["log"] = <string>logMap["log"] + " inOuterTxstart ";
        logMap["log"] = <string>logMap["log"] + testReturn();
        logMap["log"] = <string>logMap["log"] + " endOuterTx";
    }
    return <string>logMap["log"];
}

function testReturn() returns (string) {
    return " foo";
}
