struct TrxError {
    string msg;
    error cause;
    stackFrame[] stackTrace;
    string data;
}

const int RETRYCOUNT = 4;

function testTransactionStmt (int i) (string) {
    string a = "start";
    try {
        transaction {
            a = a + " inTrx";
            try {
                if (i == - 1) {
                    error err = { msg:" err"};
                    throw err;
                } else if (i == 0) {
                    a = a + " abort";
                    abort;
                } else if (i < - 1) {
                    TrxError err = {msg:" trxErr", data:"test"};
                    throw err;
                }

            } catch (TrxError err) {
                a = a + err.msg;
            }
            a = a + " endTrx";
        }aborted {
            a = a + " inAbt";
        }committed {
            a = a + " inCmt";
        }
    } catch (error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testOptionalAborted(int i)(string) {
    string a = "start";
    try {
        transaction {
            a = a + " inTrx";
            try {
                if (i == - 1) {
                    error err = { msg:" err"};
                    throw err;
                } else if (i == 0) {
                    a = a + " abort";
                    abort;
                } else if (i < - 1) {
                    TrxError err = {msg:" trxErr", data:"test"};
                    throw err;
                }

            } catch (TrxError err) {
                a = a + err.msg;
            }
            a = a + " endTrx";
        }committed {
            a = a + " inCmt";
        }
    } catch (error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testOptionalCommitted(int i)(string) {
    string a = "start";
    try {
        transaction {
            a = a + " inTrx";
            try {
                if (i == - 1) {
                    error err = { msg:" err"};
                    throw err;
                } else if (i == 0) {
                    a = a + " abort";
                    abort;
                } else if (i < - 1) {
                    TrxError err = {msg:" trxErr", data:"test"};
                    throw err;
                }

            } catch (TrxError err) {
                a = a + err.msg;
            }
            a = a + " endTrx";
        }aborted {
            a = a + " inAbt";
        }
    } catch (error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testNestedTransaction (int i)(string){
    string a = "start";
    try {
        a = a + " ";
        transaction {
            a = a + "inOuterTrx";
            transaction {
                a = a + " inInnerTrx";
                try {
                    if (i == - 1) {
                        error err = { msg:" err"};
                        throw err;
                    } else if (i == 0) {
                        a = a + " abort";
                        abort;
                    } else if (i < - 1) {
                        TrxError err = {msg:" trxErr", data:"test"};
                        throw err;
                    }
                } catch (TrxError err) {
                    a = a + err.msg;
                }
                a = a + " endInnerTrx";
            } aborted {
                a = a + " innerAborted";
            }committed {
                a = a + " inInnerCmt";
            }
            a = a + " endOuterTrx";
        } aborted {
            a = a + " outerAborted";
        }committed {
            a = a + " inOuterCmt";
        }
        a = a + " ";
    }catch (error err){
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testNestedTransactionWithFailed (int i)(string){
    string a = "start";
    try {
        a = a + " ";
        transaction {
            a = a + " inOuterTrx";
            transaction {
                a = a + " inInnerTrx";
                try {
                    if (i == - 1) {
                        error err = { msg:" err"};
                        throw err;
                    } else if (i == 0) {
                        a = a + " abort";
                        abort;
                    } else if (i < - 1) {
                        TrxError err = {msg:" trxErr", data:"test"};
                        throw err;
                    }
                } catch (TrxError err) {
                    a = a + err.msg;
                }
                a = a + " endInnerTrx";
            } failed {
                a = a + " innerFailed";
                retry 2;
            } aborted {
                a = a + " innerAborted";
            } committed {
                a = a + " inInnerCmt";
            }
            a = a + " endOuterTrx";
        } failed {
            a = a + " outerFailed";
            retry 3;
        } aborted {
            a = a + " outerAborted";
        } committed {
            a = a + " inOuterCmt";
        }
        a = a + " ";
    }catch (error err){
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithFailed (int i) (string) {
    string a = "start";
    try {
        transaction {
            a = a + " inTrx";
            try {
                if (i == - 1) {
                    error err = { msg:" err"};
                    throw err;
                } else if (i == 0) {
                    a = a + " abort";
                    abort;
                } else if (i < - 1) {
                    TrxError err = {msg:" trxErr", data:"test"};
                    throw err;
                } else {
                    a = a + " success";
                }
            } catch (TrxError err) {
                a = a + err.msg;
            }
            a = a + " endTrx";
        } failed {
           a = a + " inFailed";
           retry 4;
        } aborted {
            a = a + " inAbt";
        } committed {
            a = a + " inCmt";
        }
    } catch (error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithoutFailed (int i) (string) {
    string a = "start";
    try {
        transaction {
            a = a + " inTrx";
            try {
                if (i == - 1) {
                    error err = { msg:" err"};
                    throw err;
                } else if (i == 0) {
                    a = a + " abort";
                abort;
                } else if (i < - 1) {
                    TrxError err = {msg:" trxErr", data:"test"};
                    throw err;
                } else {
                    a = a + " success";
                }
            } catch (TrxError err) {
                a = a + err.msg;
            }
            a = a + " endTrx";
        }
    } catch (error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithRetryOff (int i) (string) {
    string a = "start";
    try {
        transaction {
            a = a + " inTrx";
            try {
                if (i == - 1) {
                    error err = { msg:" err"};
                    throw err;
                }
            } catch (TrxError err) {
                a = a + err.msg;
            }
            a = a + " endTrx";
        } failed {
            a = a + " inFailed";
            retry 0;
        } aborted {
            a = a + " inAbt";
        } committed {
            a = a + " inCmt";
        }
    } catch (error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtConstRetry() (string) {
    string a = "start";
    try {
        transaction {
            a = a + " inTrx";
            error err = {msg:" err"};
            throw err;
        } failed {
            a = a + " inFailed";
            retry RETRYCOUNT;
        } committed {
            a = a + " inTrx";
        }
    } catch (error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtSuccess() (string) {
    string a = "start";
    try {
        transaction {
            a = a + " inTrx";
        } failed {
            a = a + " inFailed";
            retry RETRYCOUNT;
        } committed {
            a = a + " inCmt";
        }
    } catch (error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtSuccess() (string) {
    string a = "start";
    try {
        transaction {
            a = a + " inFirstTrxBlockBegin";
            int i = 0;
            a = a + " inFirstTrxBlockEnd";
        } committed {
            a = a + " inFirstCmt";
        } aborted {
            a = a + " inFirstAgt";
        }
        a = a + " inFirstTrxEnd";
        transaction {
            a = a + " inSecTrxBlockBegin";
            int k = 0;
            a = a + " inSecTrxBlockEnd";
        } committed {
            a = a + " inSecCmt";
        } aborted {
            a = a + " inSecAbt";
        }
        a = a + " inFSecTrxEnd";
    } catch (error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtError() (string) {
    string a = "start";
    try {
        transaction {
            a = a + " inFirstTrxBlockBegin";
            int i = 0;
            a = a + " inFirstTrxBlockEnd";
            error err = {msg:" err"};
            throw err;
        } failed {
            a = a + " inFirstTFld";
            retry 2;
        } committed {
            a = a + " inFirstTCmt";
        } aborted {
            a = a + " inFirstTAbt";
        }
        a = a + " inFirstTrxEnd";
        transaction {
            a = a + " inSecTrxBlockBegin";
            int k = 0;
            a = a + " inSecTrxBlockEnd";
        } committed {
            a = a + " inSecCmt";
        } aborted {
            a = a + " inSecAbt";
        }
        a = a + " inFSecTrxEnd";
    } catch (error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function test ()(string) {
    string i = "st";
    transaction {
        transaction {
            i = i + " inTrx";
            abort;
        } aborted {
            i = i + " inAbt";
            abort;
        }
    } committed {
        i = i + " outCom";
    } aborted {
        i = i + " outAbt";
    }
    return i;
}

function testReturn1 ()(string) {
    string i = "st";
    transaction {
        i = i + " inTrx";
    } committed {
        i = i + " com";
        return i;
    }
    return "done";
}

function testReturn2 ()(string) {
    string i = "st";
    transaction {
        i = i + " inTrx";
        abort;
    } aborted {
        i = i + " abt";
        return i;
    }
    return "done";
}

function transactionWithBreak1 () (string) {
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

function transactionWithBreak2 () (string) {
    int i = 0;
    while (i < 5) {
        transaction {
            i = i + 1;
        } committed {
            if (i == 2) {
                break;
            }
        }
    }
    return "done";
}

function transactionWithBreak3 () (string) {
    int i = 0;
    while (i < 5) {
        transaction {
            i = i + 1;
            abort;
        } aborted {
            if (i == 2) {
            break;
            }
        }
    }
    return "done";
}


function transactionWithNext1 () (string) {
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

function transactionWithNext2 () (string) {
    int i = 0;
    while (i < 5) {
        transaction {
            i = i + 1;
        } committed {
            if (i == 2) {
                next;
            }
        }
    }
    return "done";
}

function transactionWithNext3 () (string) {
    int i = 0;
    while (i < 5) {
        transaction {
            i = i + 1;
            abort;
        } aborted {
            if (i == 2) {
                next;
            }
        }
    }
    return "done";
}
