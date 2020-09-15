import ballerina/io;
import ballerina/lang.'transaction as transactions;

function testCommitSuccessWithSuccessOutcome() returns error? {
    string str = "";
    boolean getErr = true;
    var onCommitFunc = function(transactions:Info? info) {
        str = str + "-> commit triggered ";
    };
    retry transaction {
        str += "trx started ";
        if (getErr) {
            getErr = false;
            str += "-> ";
            var c = check incrementCount(2);
        } else {
            transactions:onCommit(onCommitFunc);
            var e = commit;
        }
    }
    str += "-> exit trx block";
    assertEquality("trx started -> trx started -> commit triggered -> exit trx block", str);
}

function testCommitSuccessWithNoRetryFailOutcome() returns error? {
    string str = "";
    boolean getErr = true;
    error err = error("error");
    var onCommitFunc = function(transactions:Info? info) {
        str = str + "-> commit triggered ";
        io:println(str);
    };

    retry transaction {
        str += "trx started ";
        err = error("Error in block statement");
        transactions:onCommit(onCommitFunc);
        check commit;
    }
    return err;
}

function testCommitSuccessWithPanicOutcome() returns string|error {
    string str = "";
    var onCommitFunc = function(transactions:Info? info) {
        str = str + "-> commit triggered ";
        io:println(str);
    };
    retry transaction {
        str += "trx started ";
        transactions:onCommit(onCommitFunc);
        var e = incrementCount(2);
        check commit;
        if (e is error) {
            panic e;
        }
    }
    return str;
}

function testCommitFailWithUnusualSuccessOutcome() returns error? {
    string str = "";
    boolean getErr = true;

    retry transaction {
        str += "trx started";
        if (getErr) {
            getErr = false;
            str += " -> ";
            var c = check incrementCount(2);
        } else {
            setRollbackOnlyError();
            var e = commit;
        }

        if (transactional) {
            str += " -> commit failed";
        }
    }
    str += " -> exit transaction block.";
    assertEquality("trx started -> trx started -> commit failed -> exit transaction block.", str);
}

function testCommitFailWithFailOutcome() returns string|error {
    string str = "";
    boolean getErr = true;

    retry(2) transaction {
        str += "trx started ";
        if (getErr) {
            getErr = false;
            var c = check incrementCount(2);
        } else {
            setRollbackOnlyError();
            check commit;
        }
    }
    str += "-> exit transaction block.";
    return str;
}

function testCommitFailWithPanicOutcome() returns error? {
    string str = "";
    boolean getErr = true;

    retry transaction {
        str += "trx started ";
        if (getErr) {
            getErr = false;
            var c = check incrementCount(2);
        } else {
            setRollbackOnlyError();
            var e = commit;
        }

        if (transactional) {
            panic error("Panic due to failed commit");
        }
    }
}

function testRollbackWithSuccessOutcome() returns error? {
    string str = "";
    boolean getErr = true;
    int x = -10;
    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        str += "-> rollback ";
    };

    retry transaction {
        str += "trx started ";
        x += 1;
        if (getErr) {
            getErr = false;
            str += "-> ";
            var c = check incrementCount(2);
        } else if (x < 0) {
            transactions:onRollback(onRollbackFunc);
            rollback;
        } else {
            str += "-> commit ";
            var o = commit;
        }
        str += "-> end of trx block ";
    }
    str += "-> exit transaction block.";
    assertEquality("trx started -> trx started -> rollback -> end of trx block -> exit transaction block.", str);
}

function testRollbackWithFailOutcome() returns string|error {
    string str = "";
    boolean getErr = true;
    boolean rollbackperformed = false;
    int x = -10;
    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        if (cause is error) {
           rollbackperformed = true;
        }
    };

    retry transaction {
        str += "trx started ";
        x += 1;
        if (getErr) {
            getErr = false;
            var c = check incrementCount(2);
        } else if (x < 0) {
            transactions:onRollback(onRollbackFunc);
            rollback error("Invalid number");
        } else {
            str += "-> commit ";
            var o = commit;
        }

        if (rollbackperformed) {
            fail error("Invalid number");
        }
    }
    str += "-> exit transaction block.";
    return str;
}

function testRollbackWithPanicOutcome() returns string|error {
    string str = "";
    boolean getErr = true;
    int x = -10;
    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        str += "-> rollback triggered";
    };

    retry transaction {
        str += "trx started ";
        x += 1;
        if (getErr) {
            getErr = false;
            var c = check incrementCount(2);
        } else if (x < 0) {
            transactions:onRollback(onRollbackFunc);
            rollback;
        } else {
            str += "-> commit ";
            var o = commit;
        }

        if (transactional) {
            panic error("Invalid number");
        }
    }
    str += "-> exit transaction block.";
    return str;
}

function testPanicFromRollbackWithUnusualSuccessOutcome() returns error? {
    string str = "";
    int x = -10;
    boolean getErr = true;
    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        if (cause is error) {
            var err = trap panicWithError(cause);
            str += "-> panic from rollback ";
        }
    };

    retry transaction {
        str += "trx started ";
        x += 1;
        if (getErr) {
            getErr = false;
            str += "-> ";
            var c = check incrementCount(2);
        } else if (x < 0) {
            transactions:onRollback(onRollbackFunc);
            rollback error("Invalid number");
        } else {
            str += "-> commit ";
            var o = commit;
        }
    }
    str += "-> exit transaction block.";
    assertEquality("trx started -> trx started -> panic from rollback -> exit transaction block.", str);
}

function testPanicFromCommitWithUnusualSuccessOutcome() returns error? {
    string str = "";
    int x = 1;
    boolean getErr = true;

    retry transaction {
        str += "trx started ";
        if (getErr) {
            getErr = false;
            str += "-> ";
            var c = check incrementCount(2);
        } else {
            setRollbackOnlyError();
            var e = trap checkpanic commit;
            str = str + "-> panic from commit ";
        }
        str += "-> end of trx block ";
    }
    str += "-> exit transaction block.";
    assertEquality("trx started -> trx started -> panic from commit -> end of trx block -> exit transaction block.",
    str);
}

function testPanicFromRollbackWithUnusualFailOutcome() returns string|error {
    string str = "";
    boolean getErr = true;
    boolean rollbackWithErr = false;
    error e = error("Invalid number");
    int x = -10;
    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        if (cause is error) {
            var err = trap panicWithError(cause);
            rollbackWithErr = true;
            io:println("Panic from rollback");
        }
    };

    retry(2) transaction {
        str += "trx started ";
        x += 1;
        if (getErr) {
            getErr = false;
            var c = check incrementCount(2);
        } else if (x < 0) {
            transactions:onRollback(onRollbackFunc);
            rollback e;
        } else {
            str += "-> commit ";
            var o = commit;
        }

        if (rollbackWithErr) {
            fail e;
        }
    }
    str += "-> exit transaction block.";
    return str;
}

function testPanicFromCommitWithUnusualFailOutcome() returns string|error {
    string str = "";
    boolean getErr = true;

    retry transaction {
        str += "trx started ";
        if (getErr) {
            getErr = false;
            var c = check incrementCount(2);
        } else {
            setRollbackOnlyError();
            check commit;
        }
        str += "-> end of trx block ";
    }
    str += "-> exit transaction block.";
    return str;
}

function testPanicFromRollbackWithPanicOutcome() returns error? {
    string str = "";
    boolean getErr = true;
    int x = -10;
    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        if (cause is error) {
            panic cause;
        }
    };

    retry(2) transaction {
        str += "trx started ";
        x += 1;
        if (getErr) {
            getErr = false;
            var c = check incrementCount(2);
        } else if (x < 0) {
            transactions:onRollback(onRollbackFunc);
            rollback error("Invalid number");
        } else {
            str += "-> commit ";
            var o = commit;
        }
    }
    str += "-> exit transaction block.";
}

function testPanicFromCommitWithPanicOutcome() returns error? {
    string str = "";
    boolean getErr = true;

    retry transaction {
        str += "trx started ";
        if (getErr) {
            getErr = false;
            var c = check incrementCount(2);
        } else {
            setRollbackOnlyError();
            var e = checkpanic commit;
        }
        str += "-> end of trx block ";
    }
    str += "-> exit transaction block.";
}

function testNoCommitOrRollbackPerformedWithRollbackAndFailOutcome() returns string|error {
    string str = "";
    int x = 0;
    boolean getErr = true;
    var onCommitFunc = function(transactions:Info? info) {
        str = str + "-> commit triggered ";
    };

    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        if (cause is error) {
            io:println("rollback due to fail");
        }
    };

    retry transaction {
        str += "trx started ";
        transactions:onCommit(onCommitFunc);
        transactions:onRollback(onRollbackFunc);
        if (getErr) {
            var e = check incrementCount(2);
        } else {
            check commit;
        }
        str += "-> end of trx block ";
    }
    str += "-> exit transaction block.";
    return str;
}

function testNoCommitOrRollbackPerformedWithRollbackAndPanicOutcome() returns error? {
    string str = "";
    int x = 0;
    boolean getErr = true;
    var onCommitFunc = function(transactions:Info? info) {
        str = str + "-> commit triggered ";
    };

    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        if (cause is error) {
           str = str + "-> rollback triggered ";
           panic cause;
        }
    };

    retry transaction {
        str += "trx started ";
        transactions:onCommit(onCommitFunc);
        transactions:onRollback(onRollbackFunc);
        if (getErr) {
            getErr = false;
            var c = check incrementCount(2);
        } else {
            check commit;
        }
        str += "-> end of trx block ";
    }
    str += "-> exit transaction block.";
}

function testCommitSuccessWithSuccessOutcomeInNestedRetry() returns error? {
    var result = nestedRetryFunc(false, false);
    assertEquality("trx started -> trx started -> trx started -> commit triggered -> nested retry ->" +
        " nested retry -> nested retry -> commit triggered -> exit trx block", result);
}

function testCommitSuccessWithPanicOutcomeInNestedRetry() {
    var result = nestedRetryFunc(true, false);
}

function testCommitFailWithUnusualSuccessOutcomeInNestedRetry() returns error? {
    var result = nestedRetryFunc(false, true);
    assertEquality("trx started -> trx started -> trx started -> nested retry -> nested retry" +
    " -> nested retry -> exit trx block", result);
}

function testCommitFailWithPanicOutcomeInNestedRetry() {
    var result = nestedRetryFunc(true, true);
}

function nestedRetryFunc(boolean needPanic, boolean failCommit) returns string|error {
    string str = "";
    int count = 0;
    var onCommitFunc = function(transactions:Info? info) {
        str = str + "-> commit triggered ";
        io:println(str);
    };

    retry transaction {
        str += "trx started ";
        count = count + 1;
        if (count <= 2) {
            str += "-> ";
            var c = check incrementCount(2);
        } else {
            if (failCommit) {
               setRollbackOnlyError();
            }
            transactions:onCommit(onCommitFunc);
            var e = commit;
        }
        int count2 = 0;
        retry transaction {
            str += "-> nested retry ";
            count2 = count2 + 1;
            if (count2 <= 2) {
                var c = check incrementCount(2);
            } else {
                if (failCommit) {
                    setRollbackOnlyError();
                }
                transactions:onCommit(onCommitFunc);
                var e = commit;
            }
        }
        if (needPanic) {
            panic error("Panic in nested retry");
        }
    }
    str += "-> exit trx block";
    return str;
}

function testRollbackWithFailOutcomeInFirstNestedRetryStmt() returns error? {
    var result = nestedRetryFuncWithRollback(true, false, false, false, false);
    if (result is error) {
        assertEquality("Rollback due to error in trx 1", result.message());
    }
}

function testRollbackWithFailOutcomeInSecondNestedRetryStmt() returns error? {
    var result = nestedRetryFuncWithRollback(true, false, false, false, true);
    if (result is error) {
        assertEquality("Rollback due to error in trx 2", result.message());
    }
}

function testRollbackWithPanicOutcomeInFirstNestedRetryStmt() {
    var result = nestedRetryFuncWithRollback(true, false, true, false, false);
}

function testPanicFromRollbackWithPanicOutcomeInSecondNestedRetryStmt() {
    var result = nestedRetryFuncWithRollback(true, true, false, false, true);
}

function nestedRetryFuncWithRollback(boolean doRollback, boolean doPanicInRollback, boolean doPanicAfterRollback1,
boolean doPanicAfterRollback2, boolean errInSecond) returns string|error {
    string str = "";
    int count = 0;
    boolean errInRollback1 = false;
    boolean errInRollback2 = false;
    boolean errInFailedCheck = false;

    retry(2) transaction {
        retry(2) transaction {
            var onCommitFunc = function(transactions:Info? info) {
                str = str + "-> commit triggered in trx 1 ";
            };
            var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
                str = str + "-> rollback triggered in trx 1 ";
                if (cause is error) {
                    if (doPanicInRollback && !errInSecond && !errInFailedCheck) {
                       panic cause;
                    } else {
                        errInRollback1 = true;
                    }
                }
            };
            transactions:onCommit(onCommitFunc);
            transactions:onRollback(onRollbackFunc);
            count = count + 1;
            str += "trx strated ";
            if (count <= 3) {
                errInFailedCheck = true;
                var c = check incrementCount(2);
            }
            if (doRollback) {
                str += "do rollback in 1";
                errInFailedCheck = false;
                rollback error("Rollback due to error in trx 1");
            } else {
                setRollbackOnlyError();
                var e = commit;
            }
            if (errInRollback1 && !doPanicAfterRollback1 && !errInSecond) {
                fail error("Rollback due to error in trx 1");
            }
            if (doPanicAfterRollback1) {
                panic error("Panic in nested retry 1");
            }
        }
        check commit;
    }
    int count2 = 0;
    errInFailedCheck = false;
    retry(2) transaction {
        retry(2) transaction {
            var onCommitFunc = function(transactions:Info? info) {
                str = str + "-> commit triggered in trx 2 ";
            };
            var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
                str = str + "-> rollback triggered in trx 2 ";
                if (cause is error) {
                    if (doPanicInRollback && errInSecond && !errInFailedCheck) {
                       panic cause;
                    } else {
                        errInRollback2 = true;
                    }
                }
            };
            transactions:onCommit(onCommitFunc);
            transactions:onRollback(onRollbackFunc);
            count2 = count2 + 1;
            if (count2 <= 3) {
                errInFailedCheck = true;
                var c = check incrementCount(2);
            }
            if (doRollback) {
                str += "do rollback in 2";
                errInFailedCheck = false;
                rollback error("Rollback due to error in trx 2");
            } else {
                setRollbackOnlyError();
                var e = commit;
            }
            if (errInRollback2 && !doPanicAfterRollback2 && errInSecond) {
                fail error("Rollback due to error in trx 2");
            }
            if (doPanicAfterRollback2) {
                panic error("Panic in nested retry 2");
            }
        }
        check commit;
    }
    str += "-> exit trx block";
    return str;
}

function incrementCount(int i) returns int|error {
    if (i == 2) {
        error err = error("Error in increment count");
        return err;
    } else {
        int x = i + 2;
        return x;
    }
}

transactional function setRollbackOnlyError() {
    error cause = error("rollback only is set, hence commit failed !");
    transactions:setRollbackOnly(cause);
}

function panicWithError(error err) returns error? {
    panic err;
}

type AssertionError error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "'," +
                "found '" + actual.toString () + "'");
}
