import ballerina/transactions;

public type TrxError error<string, record {string data;}>;

string a = "";
string id1WithinTx = "";
string id2WithinTx = "";
string idWithinHandler = "";

function testTransactionStmtWithNoHandlers() returns (string) {
    a = "";
    a = (a + "start");
    transaction {
        a = a + " inTrx";
        int i = 0;
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = (a + " end");
    return a;
}

function testTransactionStmtCommitWithCommitHandler() returns (string) {
    a = "";
    a = a + "start";
    transaction with oncommit = commitFunction {
        string id = transactions:getCurrentTransactionId();
        a = a + " inTrx";
        int i = 0;
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionAbortStmtWithAbortHandler() returns (string) {
    a = "";
    a = a + "start";
    transaction with onabort = abortFunction {
        a = a + " inTrx";
        int i = 0;
        if (i == 0) {
            abort;
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionAbortStmtWithNoAbortHandler() returns (string) {
    a = "";
    a = a + "start";
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
    a = a + " end";
    return a;
}

function testTransactionAbortStmtWithCommitHandler() returns (string) {
    a = "";
    a = a + "start";
    transaction with oncommit = commitFunction {
        a = a + " inTrx";
        int i = 0;
        if (i == 0) {
            abort;
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionAbortStmtWithAllHandlers() returns (string) {
    a = "";
    a = a + "start";
    transaction with retries = 4, oncommit = commitFunction, onabort = abortFunction {
        a = a + " inTrx";
        int i = 0;
        if (i == 0) {
            abort;
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionCommitStmtWithAllHandlers() returns (string) {
    a = "";
    a = a + "start";
    transaction with retries = 4, oncommit = commitFunction, onabort = abortFunction {
        a = a + " inTrx";
        int i = 0;
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function testTransactionThrowWithAllHandlers() returns (string) {
    a = "";
    a = a + "start";
    var result = trap testTransactionThrowWithAllHandlersHelper();
    if (result is string) {
        a = result;
    } else if (result is error) {
        a += result.reason();
    }
    a = a + " end";
    return a;
}

function testTransactionThrowWithAllHandlersHelper() returns string {
    transaction with retries = 4, oncommit = commitFunction, onabort = abortFunction {
        a = a + " inTrx";
        int i = 0;
        if (i == 0) {
            TrxError err = error(" trxErr", { data: "test" });
            panic err;
        }
        a = a + " endTrx";
    } onretry {
        a = a + " inFailed";
    }
    return a;
}

function testTransactionCommitAfterFailureWithAllHandlers() returns (string) {
    a = "";
    a = a + "start";
    var result = trap testTransactionCommitAfterFailureWithAllHandlersHelper();
    if (result is string) {
        a = result;
    } else if (result is error) {
        a += result.reason();
    }
    a = a + " end";
    return a;
}

function testTransactionCommitAfterFailureWithAllHandlersHelper() returns string {
    int i = 0;
    transaction with retries = 4, oncommit = commitFunction, onabort = abortFunction {
        a = a + " inTrx";
        if (i < 2) {
            TrxError err = error(" trxErr", { data: "test" });
            panic err;
        }
        a = a + " endTrx";
    } onretry {
        i = i + 1;
        a = a + " inFailed";
    }
    return a;
}

function testMultipleTransactionsWithAllHandlers() returns (string) {
    a = "";
    a = a + "start";
    transaction with retries = 4, oncommit = commitFunction, onabort = abortFunction {
        a = a + " inFirstTrx";

        a = a + " endFirstTrx";
    } onretry {
        a = a + " beforeRetry-First";
    }

    transaction with retries = 4, oncommit = commitFunctionSecond, onabort = abortFunctionSecond {
        a = a + " inSecondTrx";
        int i = 0;
        a = a + " endSecondTrx";
    } onretry {
        a = a + " beforeRetry-second";
    }

    a = a + " end";
    return a;
}

function testMultipleTransactionsFailedWithAllHandlers() returns (string) {
    a = "";
    a = a + "start";
    int i = 0;
    var result = trap testMultipleTransactionsFailedWithAllHandlersHelper(i);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a += result.reason();
    }
    transaction with retries = 4, oncommit = commitFunctionSecond, onabort = abortFunctionSecond {
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

function testMultipleTransactionsFailedWithAllHandlersHelper(int i) returns string {
    transaction with retries = 0, oncommit = commitFunction, onabort = abortFunction {
        a = a + " inFirstTrx";
        if (i == 0) {
            TrxError err = error(" trxErr", { data: "test" });
            panic err;
        }
        a = a + " endFirstTrx";
    } onretry {
        a = a + " beforeRetry-First";
    }
    return a;
}

function testMultipleTransactionsWithAllHandlersWithID() returns (string, string, string, string, string) {
    a = "";
    a = a + "start";
    transaction with retries = 4, oncommit = commitFunction, onabort = abortFunction {
        id1WithinTx = transactions:getCurrentTransactionId();
        a = a + " inFirstTrx";

        a = a + " endFirstTrx";
    } onretry {
        a = a + " beforeRetry-First";
    }
    string idAfterHandler = idWithinHandler;

    transaction with retries = 4, oncommit = commitFunctionSecond, onabort = abortFunctionSecond {
        id2WithinTx = transactions:getCurrentTransactionId();
        a = a + " inSecondTrx";
        int i = 0;
        a = a + " endSecondTrx";
    } onretry {
        a = a + " beforeRetry-second";
    }

    a = a + " end";
    return (a, id1WithinTx, idAfterHandler, id2WithinTx, idWithinHandler);
}

function testMultipleTransactionsFailedWithAllHandlersWithID() returns (string, string, string, string, string) {
    a = "";
    a = a + "start";
    int i = 0;
    var result = trap testMultipleTransactionsFailedWithAllHandlersWithIDHelper(i);
    if (result is string) {
        a = result;
    } else if (result is error) {
        a += result.reason();
    }
    string idAfterHandler = idWithinHandler;

    transaction with retries = 4, oncommit = commitFunctionSecond, onabort = abortFunctionSecond {
        id2WithinTx = transactions:getCurrentTransactionId();
        a = a + " inSecondTrx";
        if (i == 0) {
            abort;
        }
        a = a + " endSecondTrx";
    } onretry {
        a = a + " beforeRetry-second";
    }

    a = a + " end";
    return (a, id1WithinTx, idAfterHandler, id2WithinTx, idWithinHandler);
}

function testMultipleTransactionsFailedWithAllHandlersWithIDHelper(int i) returns string {
    transaction with retries = 0, oncommit = commitFunction, onabort = abortFunction {
        id1WithinTx = transactions:getCurrentTransactionId();
        a = a + " inFirstTrx";
        if (i == 0) {
            TrxError err = error(" trxErr", { data: "test" });
            panic err;
        }
        a = a + " endFirstTrx";
    } onretry {
        a = a + " beforeRetry-First";
    }
    return a;
}

function commitFunction(string transactionid) {
    idWithinHandler = transactionid;
    a = a + " incommitFunction";
}

function abortFunction(string transactionid) {
    idWithinHandler = transactionid;
    a = a + " inAbortFunction";
}

function commitFunctionSecond(string transactionid) {
    idWithinHandler = transactionid;
    a = a + " incommitFunctionSecond";
}

function abortFunctionSecond(string transactionid) {
    idWithinHandler = transactionid;
    a = a + " inAbortFunctionSecond";
}
