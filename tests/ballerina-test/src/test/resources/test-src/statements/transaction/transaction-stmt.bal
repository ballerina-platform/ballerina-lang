public type TrxError {
    string message;
    error[] cause;
    string data;
};

@final int RETRYCOUNT = 4;
@final int RETRYCOUNT_2 = -4;

function testTransactionStmt (int i) returns (string) {
    string a = "start";
    try {
        transaction {
            a = a + " inTrx";
            try {
                if (i == -1) {
                    error err = {message:" err"};
                    throw err;
                } else if (i == 0) {
                    a = a + " abort";
                    abort;
                } else if (i < -1) {
                    TrxError err = {message:" trxErr", data:"test"};
                    throw err;
                }

            } catch (TrxError err) {
                a = a + err.message;
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

function testAbortStatement () returns (string) {
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


function testOptionalFailed (int i) returns (string) {
    string a = "start";
    try {
        transaction {
            a = a + " inTrx";
            try {
                if (i == -1) {
                    error err = {message:" err"};
                    throw err;
                } else if (i == 0) {
                    a = a + " abort";
                    abort;
                } else if (i < -1) {
                    TrxError err = {message:" trxErr", data:"test"};
                    throw err;
                }
            } catch (TrxError err) {
                a = a + err.message;
            }
            a = a + " endTrx";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testNestedTransaction (int i) returns (string) {
    string a = "start";
    try {
        transaction {
            a = a + " inOuterTrx";
            transaction {
                a = a + " inInnerTrx";
                try {
                    if (i == -1) {
                        error err = {message:" err"};
                        throw err;
                    } else if (i == 0) {
                        a = a + " abort";
                        abort;
                    } else if (i < -1) {
                        TrxError err = {message:" trxErr", data:"test"};
                        throw err;
                    }
                } catch (TrxError err) {
                    a = a + err.message;
                }
                a = a + " endInnerTrx";
            }
            a = a + " endOuterTrx";
        }
        a = a + " ";
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testNestedTransactionWithFailed (int i) returns (string) {
    string a = "start";
    try {
        transaction with retries=3 {
            a = a + " inOuterTrx";
            transaction with retries=2 {
                a = a + " inInnerTrx";
                try {
                    if (i == -1) {
                        error err = {message:" err"};
                        throw err;
                    } else if (i == 0) {
                        a = a + " abort";
                        abort;
                    } else if (i < -1) {
                        TrxError err = {message:" trxErr", data:"test"};
                        throw err;
                    }
                } catch (TrxError err) {
                    a = a + err.message;
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
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithFailedAndNonDefaultRetries (int i) returns (string) {
    string a = "start";
    try {
        transaction with retries=4 {
            a = a + " inTrx";
            try {
                if (i == -1) {
                    error err = {message:" err"};
                    throw err;
                } else if (i == 0) {
                    a = a + " abort";
                    abort;
                } else if (i < -1) {
                    TrxError err = {message:" trxErr", data:"test"};
                    throw err;
                } else {
                    a = a + " success";
                }
            } catch (TrxError err) {
                a = a + err.message;
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

function testTransactionStmtWithRetryOff (int i) returns (string) {
    string a = "start";
    try {
        transaction with retries=0 {
            a = a + " inTrx";
            try {
                if (i == -1) {
                    error err = {message:" err"};
                    throw err;
                }
            } catch (TrxError err) {
                a = a + err.message;
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

function testTransactionStmtWithConstRetryFailed () returns (string) {
    string a = "start";
    int i = 0;
    try {
        transaction with retries = RETRYCOUNT {
            a = a + " inTrx";
            if (i == 0) {
                error err = {message:" err"};
                throw err;
            }
        } onretry {
            a = a + " inFailed";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithConstRetryFailed2 () returns (string) {
    string a = "start ";
    int i = 0;
    try {
        transaction with retries=RETRYCOUNT_2 {
            a = a + " inTrx";
            if (i == 0) {
                error err = {message:" err"};
                throw err;
            }
        } onretry {
            a = a + " inFailed";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithConstRetrySuccess () returns (string) {
    string a = "start";
    try {
        transaction with retries=RETRYCOUNT {
            a = a + " inTrx";
        } onretry {
            a = a + " inFailed";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtSuccess () returns (string) {
    string a = "start";
    try {
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
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtFailed1 () returns (string) {
    string a = "start";
    int i = 0;
    try {
        transaction with retries=2 {
            a = a + " inFirstTrxBlock";
            if (i == 0) {
                error err = {message:" err"};
                throw err;
            }
        } onretry {
            a = a + " inFirstTrxFld";
        }
        a = a + " inFirstTrxEnd";
        transaction {
            a = a + " inSecTrxBlock";
        }
        a = a + " inFSecTrxEnd";
    } catch (error err) {
        a = a + err.message;
    }
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtFailed2 () returns (string) {
    string a = "start";
    int i = 0;
    try {
        transaction with retries=2 {
            a = a + " inFirstTrxBlock";
            if (i == 0) {
                error err = {message:" err"};
                throw err;
            }
        } onretry {
            a = a + " inFirstTrxFld";
        }
        a = a + " inFirstTrxEnd";
    } catch (error err) {
        a = a + err.message;
    }

    transaction {
        a = a + " inSecTrxBlock";
    }
    a = a + " inFSecTrxEnd";
    a = a + " end";
    return a;
}

function testAbort () returns (string) {
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

function transactionWithBreak () returns (string) {
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

function transactionWithNext () returns (string) {
    int i = 0;
    transaction {
        while (i < 5) {
            i = i + 1;
            if (i == 2) {
                next;
            }
        }
    }
    return "done";
}

function testTransactionStmtWithFail () returns (string) {
    string a = "start ";
    int i = 0;

    transaction with retries=4 {
        a = a + " inTrx";
        if (i == 0) {
            fail;
        }
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}


function testSimpleNestedTransactionAbort () returns (string) {
    string a = "start ";
    int i = 0;
    transaction with retries=4 {
        a = a + " inOuterTxstart ";
        transaction {
            a = a + " inInnerTxstart ";
            if (i == 0) {
                a = a + " abortingInnerTxstart ";
                abort ;
            }
            a = a + " endInnerTxstart ";
        }
        a = a + " endOuterTxstart";
    }
    return a;
}


function testValidReturn () returns (string) {
    string a = "start ";
    int i = 0;
    transaction with retries=4 {
        a = a + " inOuterTxstart ";
        transaction {
            a = a + " inInnerTxstart ";
            if (i == 0) {
                a = a + foo();
            }
            a = a + " endInnerTx";
        }
        a = a + foo();
        a = a + " endOuterTx";
    }
    return a;
}

function foo() returns (string) {
    return " foo";
}
