import ballerina/io;
import ballerina/lang.'transaction as transactions;

function testCommitSuccessWithSuccessOutcome() {
    string str = "";
    int x = 0;
    transaction {
        str += "trx started ";
        x = x + 1;
        var e = commit;
        if (!transactional) {
            str += "-> trx commit successfull ";
        }
    }
    str += "-> exit trx block";
    assertEquality(1, x);
    assertEquality("trx started -> trx commit successfull -> exit trx block", str);
}

function testCommitSuccessWithFailOutcome() returns error? {
    int x = 1;
    transaction {
        x = x + 1;
        check commit;
        if (!transactional) {
            io:println("Transaction successfully committed");
        }
        var y = check incrementCount(x);
    }
}

function testCommitSuccessWithPanicOutcome() returns error? {
    int x = 2;
    transaction {
        x = x + 1;
        check commit;
        if (!transactional) {
           io:println("Transaction successfully committed");
        }
        var y = check incrementCount(x);
    }
}

function testCommitFailWithUnusualSuccessOutcome() returns error? {
    string str = "";
    int x = 2;
    transaction {
        str += "trx started";
        x += 2;
        var y = check incrementCount(x);
        setRollbackOnlyError();
        var e = commit;
        if (transactional) {
            str += " -> fail commit";
        }
        assertEquality(6, y);
    }
    str += " -> exit transaction block.";
    assertEquality("trx started -> fail commit -> exit transaction block.", str);
}

function testCommitFailWithFailOutcome() returns error? {
    string str = "";
    int x = 1;
    var onCommitFunc = function(transactions:Info? info) {
        str = str + " -> commit triggered";
    };

    transaction {
        str += "trx started ";
        x += 1;
        transactions:onCommit(onCommitFunc);
        var y = check incrementCount(x);
        var e = commit;
    }
    str += "-> exit transaction block.";
    assertEquality("trx started -> exit transaction block.", str);
}

function testCommitFailWithPanicOutcome() returns error? {
    int x = 1;
    transaction {
        x += 2;
        setRollbackOnlyError();
        var e = commit;
        if (transactional) {
            io:println("Transaction not committed");
            var y = incrementCount(x);
        }
    }
}

function testRollbackWithSuccessOutcome() {
    string str = "";
    int x = -10;
    transaction {
        str += "trx started ";
        x += 1;
        if (x < 0) {
            str += "-> rollback ";
            rollback;
        } else {
            str += "-> commit ";
            var o = commit;
        }
        str += "-> end of trx block ";
    }
    str += "-> exit transaction block.";
    assertEquality("trx started -> rollback -> end of trx block -> exit transaction block.", str);
}

function testRollbackWithFailOutcome() returns error? {
    string str = "";
    int x = -10;
    transaction {
        str += "trx started ";
        x += 1;
        if (x < 0) {
            str += "-> rollback ";
            rollback;
        } else {
            str += "-> commit ";
            var o = commit;
        }
        x += 11;
        io:println(str);
        var y = check incrementCount(x);
        str += "-> end of trx block ";
    }
    str += "-> exit transaction block.";
}

function testRollbackWithPanicOutcome() returns error? {
    string str = "";
    int x = -10;
    transaction {
        str += "trx started ";
        x += 1;
        if (x < 0) {
            str += "-> rollback ";
            rollback;
        } else {
            str += "-> commit ";
            var o = commit;
        }
        x += 12;
        io:println(str);
        var y = check incrementCount(x);
        str += "-> end of trx block ";
    }
    str += "-> exit transaction block.";
}

function testPanicFromRollbackWithUnusualSuccessOutcome() returns error? {
    string str = "";
    int x = 1;

    var onCommitFunc = function(transactions:Info? info) {
        str = str + " -> commit triggered";
    };

    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        if (cause is error) {
            var err = trap panicWithError(cause);
            str += "-> panic from rollback ";
        }
    };

    transaction {
        str += "trx started ";
        x += 1;
        transactions:onCommit(onCommitFunc);
        transactions:onRollback(onRollbackFunc);
        if (x == 2) {
            error err = error("Invalid value");
            rollback err;
        } else {
            check commit;
        }
        str += "-> end of trx block ";
    }
    str += "-> exit transaction block.";
    assertEquality("trx started -> panic from rollback -> end of trx block -> exit transaction block.", str);
}

function testPanicFromCommitWithUnusualSuccessOutcome() returns error? {
    string str = "";
    int x = 1;

    var onCommitFunc = function(transactions:Info? info) {
        error err = error("Panic from commit");
        var p = trap panicWithError(err);
        str = str + " -> panic from commit ";
    };

    transaction {
        str += "trx started ";
        x += 2;
        transactions:onCommit(onCommitFunc);
        var o = commit;
        str += "-> end of trx block ";
    }
    str += "-> exit transaction block.";
    assertEquality("trx started  -> panic from commit -> end of trx block -> exit transaction block.", str);
}

function testPanicFromRollbackWithUnusualFailOutcome() returns error? {
    string str = "";
    int x = 0;

    var onCommitFunc = function(transactions:Info? info) {
        str = str + " -> commit triggered";
    };

    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        if (cause is error) {
            var err = trap panicWithError(cause);
            io:println("Panic from rollback");
        }
    };

    transaction {
        str += "trx started ";
        x += 1;
        transactions:onCommit(onCommitFunc);
        transactions:onRollback(onRollbackFunc);
        if (x == 1) {
            error err = error("Invalid value");
            rollback err;
        } else {
            check commit;
        }
        x += 1;
        var y = check incrementCount(x);
    }
    str += "-> exit transaction block.";
}

function testPanicFromCommitWithUnusualFailOutcome() returns error? {
    string str = "";
    int x = 1;

    var onCommitFunc = function(transactions:Info? info) {
        error err = error("Panic from commit");
        var p = trap panicWithError(err);
        io:println("Panic from commit");
    };

    transaction {
        str += "trx started ";
        x += 1;
        transactions:onCommit(onCommitFunc);
        var o = commit;
        var y = check incrementCount(x);
        str += "-> end of trx block ";
    }
    str += "-> exit transaction block.";
}

function incrementCount(int i) returns int|error {
    if (i == 2) {
        error err = error("Error in increment count");
        return err;
    } else if (i == 3) {
        error err = error("Panic in increment count");
        panic err;
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

function getError(boolean err) returns error? {
    if(err) {
       error er = error("Custom Error");
       return er;
    }
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

    panic AssertionError(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
