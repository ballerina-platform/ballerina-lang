import ballerina/io;

function testRollback() {
    string|error x =  trap actualCode(0, false);
    if(x is string) {
        assertEquality("start fc-0 inTrx Commit endTrx end", x);
    }
}

function testCommit() {
    string|error x =  trap actualCode(0, false);
    if(x is string) {
        assertEquality("start fc-0 inTrx Commit endTrx end", x);
    }
}

function testPanic() {
    string|error x =  trap actualCode(1, false);
    if(x is string) {
        assertEquality("start fc-1 inTrx blowUp", x);
    }
}

function actualCode(int failureCutOff, boolean requestRollback) returns (string) {
    string a = "";
    a = a + "start";
    a = a + " fc-" + failureCutOff.toString();
    int count = 0;

    transaction {
        a = a + " inTrx";
        count = count + 1;
        if transactional {
            io:println("Transactional mode");
        }
        if (count <= failureCutOff) {
            a = a + " blowUp"; // transaction block panic scenario, Set failure cutoff to 0, for not blowing up.
            int bV = blowUp();
        }
        if (requestRollback) { // Set requestRollback to true if you want to try rollback scenario, otherwise commit
            a = a + " Rollback";
            rollback;
        } else {
            a = a + " Commit";
            var i = commit;
        }
        a = a + " endTrx";
        a = (a + " end");
    }

    io:println("## Transaction execution completed ##");
    return a;
}

function blowUp()  returns int {
    if (5 == 5) {
        error err = error("TransactionError");
        panic err;
    }
    return 5;
}

type AssertionError error<ASSERTION_ERROR_REASON>;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError(message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
