public class MyRetryManager {
    private int count;
    public function init(int count = 3) {
        self.count = count;
    }
    public function shouldRetry(error? e) returns boolean {
        if e is error && self.count >  0 {
            self.count -= 1;
            return true;
        } else {
            return false;
        }
    }
}

function testRetryStatement() {
    string|error x = retryError();
    if(x is string) {
        assertEquality("start -> within retry 1 -> attempt 1:error -> within retry 1 -> attempt 2:error "
        + "-> within retry 1 -> attempt 3:execution completed. -> within retry 2 -> attempt 1:error -> within retry 2 "
        + "-> attempt 2:error -> within retry 2 -> attempt 3:execution completed.", x);
    }

    string nestedInnerReturnRes = testNestedInnerReturn();
    assertEquality("start -> within retry 1 -> attempt 1:execution completed. " +
    "-> within retry 2 -> attempt 1:execution completed. " +
    "-> within retry 3 -> attempt 1:execution completed.", nestedInnerReturnRes);

     string nestedMiddleReturnRes = testNestedMiddleReturn();
        assertEquality("start -> within retry 1 -> attempt 1:execution completed. " +
        "-> within retry 2 -> attempt 1:execution completed.", nestedMiddleReturnRes);
}

function retryError() returns string |error {
    string str = "start";
    int count1 = 0;
    retry<MyRetryManager> (3) {
        str += " -> within retry 1";
        count1 = count1 + 1;
        if (count1 < 3) {
            str += (" -> attempt " + count1.toString() + ":error");
            return trxError();
        }
        str += (" -> attempt "+ count1.toString() + ":execution completed.");
        int count2 = 0;
        retry<MyRetryManager> (3) {
            str += " -> within retry 2";
            count2 = count2 + 1;
            if (count2 < 3) {
                str += (" -> attempt " + count2.toString() + ":error");
                return trxError();
            }
            str += (" -> attempt "+ count2.toString() + ":execution completed.");
            return str;
        }
    }
}

function testNestedInnerReturn() returns string {
    string str = "start";
    int count1 = 0;
    retry<MyRetryManager> (3) {
        count1 += 1;
        str += " -> within retry 1";
        str += (" -> attempt "+ count1.toString() + ":execution completed.");
        int count2 = 0;
        retry<MyRetryManager> (3) {
            str += " -> within retry 2";
            count2 += 1;
            str += (" -> attempt "+ count2.toString() + ":execution completed.");
            int count3 = 0;
            retry<MyRetryManager> (3) {
                str += " -> within retry 3";
                count3 += 1;
                str += (" -> attempt "+ count3.toString() + ":execution completed.");
                return str;
            }
        }
    }
}

function testNestedMiddleReturn() returns string {
    string str = "start";
    int count1 = 0;
    retry<MyRetryManager> (3) {
        count1 += 1;
        str += " -> within retry 1";
        str += (" -> attempt "+ count1.toString() + ":execution completed.");
        int count2 = 0;
        retry<MyRetryManager> (3) {
            str += " -> within retry 2";
            count2 += 1;
            str += (" -> attempt "+ count2.toString() + ":execution completed.");
            if(count2 == 1) {
                return str;
            }
            int count3 = 0;
            retry<MyRetryManager> (3) {
                str += " -> within retry 3";
                count3 += 1;
                str += (" -> should not reach here.");
                return str;
            }
        }
    }
}

function trxError()  returns error {
    return error("TransactionError");
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
