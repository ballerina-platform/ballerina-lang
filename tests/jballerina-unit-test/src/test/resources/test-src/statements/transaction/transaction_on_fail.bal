type AssertionError error;

const ASSERTION_ERROR_REASON = "AssertionError";

function testOnFailStatement() {
    string onFailResult = testTrxReturnVal();
    assertEquality("start -> within transaction1 -> within transaction2 -> error handled", onFailResult);
}

function testTrxReturnVal() returns string {
    string str = "start";
    transaction {
        str = str + " -> within transaction1";
        var ii = commit;
        error err = error("custom error", message = "error value");
        transaction {
            str = str + " -> within transaction2";
            var commitRes = commit;
        }
          fail err;
    } on fail error e {
        str += " -> error handled";
    }
    return str;
}

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
