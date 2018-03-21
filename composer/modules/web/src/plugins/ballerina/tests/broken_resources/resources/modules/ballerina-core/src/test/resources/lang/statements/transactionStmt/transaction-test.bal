import ballerina/lang.errors;

struct TrxError {
    string msg;
    errors:Error cause;
    string data;
}

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