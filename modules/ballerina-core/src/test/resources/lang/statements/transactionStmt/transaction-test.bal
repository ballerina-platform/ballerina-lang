import ballerina.lang.errors;

struct TrxError {
    string msg;
    errors:Error cause;
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
                    errors:Error err = { msg:" err"};
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
    } catch (errors:Error err) {
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
                    errors:Error err = { msg:" err"};
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
    } catch (errors:Error err) {
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
                    errors:Error err = { msg:" err"};
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
    } catch (errors:Error err) {
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
                        errors:Error err = { msg:" err"};
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
    }catch (errors:Error err){
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testNestedTransactionWithFailed (int i)(string){
    string a = "start";
    try {
        a = a + " ";
        transaction with retries(3) {
            a = a + " inOuterTrx";
            transaction with retries(2) {
                a = a + " inInnerTrx";
                try {
                    if (i == - 1) {
                        errors:Error err = { msg:" err"};
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
            } aborted {
                a = a + " innerAborted";
            } committed {
                a = a + " inInnerCmt";
            }
            a = a + " endOuterTrx";
        } failed {
            a = a + " outerFailed";
        } aborted {
            a = a + " outerAborted";
        } committed {
            a = a + " inOuterCmt";
        }
        a = a + " ";
    }catch (errors:Error err){
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithFailed (int i) (string) {
    string a = "start";
    try {
        transaction with retries(4) {
            a = a + " inTrx";
            try {
                if (i == - 1) {
                    errors:Error err = { msg:" err"};
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
        } aborted {
            a = a + " inAbt";
        } committed {
            a = a + " inCmt";
        }
    } catch (errors:Error err) {
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
                    errors:Error err = { msg:" err"};
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
    } catch (errors:Error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtWithRetryOff (int i) (string) {
    string a = "start";
    try {
        transaction with retries(0) {
            a = a + " inTrx";
            try {
                if (i == - 1) {
                    errors:Error err = { msg:" err"};
                    throw err;
                }
            } catch (TrxError err) {
                a = a + err.msg;
            }
            a = a + " endTrx";
        } failed {
            a = a + " inFailed";
        } aborted {
            a = a + " inAbt";
        } committed {
            a = a + " inCmt";
        }
    } catch (errors:Error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtConstRetry() (string) {
    string a = "start";
    try {
        transaction with retries(RETRYCOUNT) {
            a = a + " inTrx";
            errors:Error err = {msg:" err"};
            throw err;
        } failed {
            a = a + " inFailed";
        } committed {
            a = a + " inTrx";
        }
    } catch (errors:Error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testTransactionStmtSuccess() (string) {
    string a = "start";
    try {
        transaction with retries(RETRYCOUNT) {
            a = a + " inTrx";
        } failed {
            a = a + " inFailed";
        } committed {
            a = a + " inCmt";
        }
    } catch (errors:Error err) {
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
    } catch (errors:Error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}

function testMultipleTransactionStmtError() (string) {
    string a = "start";
    try {
        transaction with retries(2) {
            a = a + " inFirstTrxBlockBegin";
            int i = 0;
            a = a + " inFirstTrxBlockEnd";
            errors:Error err = {msg:" err"};
            throw err;
        } failed {
            a = a + " inFirstTFld";
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
    } catch (errors:Error err) {
        a = a + err.msg;
    }
    a = a + " end";
    return a;
}