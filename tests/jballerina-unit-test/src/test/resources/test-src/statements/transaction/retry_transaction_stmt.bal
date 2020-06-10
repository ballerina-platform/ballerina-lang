function testPanic() {
    string|error x =  trap actualCode(2, false);
    if(x is string) {
        assertEquality("start fc-2 inTrx blowUp inTrx blowUp inTrx Commit endTrx end", x);
    }
}

function actualCode(int failureCutOff, boolean requestRollback) returns (string) {
    string a = "";
    a = a + "start";
    a = a + " fc-" + failureCutOff.toString();
    int count = 0;

    retry transaction {
        a = a + " inTrx";
        count = count + 1;
        if (count <= failureCutOff) {
            a = a + " blowUp"; // transaction block panic scenario, Set failure cutoff to 0, for not blowing up.
            int bV = blowUp();
        }
        if (requestRollback) { // Set requestRollback to true if you want to try rollback scenario, otherwise commit
            rollback;
            a = a + " Rollback";
        } else {
            var i = commit;
            a = a + " Commit";
        }
        a = a + " endTrx";
        a = (a + " end");
    }
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

