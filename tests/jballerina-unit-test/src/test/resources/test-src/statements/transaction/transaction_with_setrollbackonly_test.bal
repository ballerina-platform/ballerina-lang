import ballerina/lang.'transaction as trx;

function testSetRollbackOnly() returns error? {
    string str = "";
    var rollbackFunc = function (trx:Info info, error? cause, boolean willRetry) {
        if (cause is error) {
            str += " " + cause.message();
        }
    };
    transaction {
        trx:onRollback(rollbackFunc);
        str += "In Trx";
        setRollbackOnlyError();
        check commit;
        str += " commit";
    }
    str += " -> Trx exited";
    assertEquality("In Trx -> Trx exited", str);
}

transactional function setRollbackOnlyError() {
    error cause = error("setRollbackOnly");
    trx:setRollbackOnly(cause);
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

    panic AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
