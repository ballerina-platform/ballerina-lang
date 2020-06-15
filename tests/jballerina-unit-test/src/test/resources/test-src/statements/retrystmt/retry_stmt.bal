public type MyRetryManager object {
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
};

function testRetryStatement() {
    string|error x = retryError();
    if(x is string) {
        assertEquality("start attempt 1:panic, attempt 2:error, attempt 3:result returned end.", x);
    }
}

function retryError() returns string |error {
    string str = "start";
    int count = 0;
    retry<MyRetryManager> (3) {
        count = count+1;
        if (count == 1) {
            str += (" attempt " + count.toString() + ":panic,");
            error err = error("Retrying");
            panic err;
        } else if (count == 2) {
            str += (" attempt " + count.toString() + ":error,");
            error er = error("Custom Error");
            return er;
        }
        str += (" attempt "+ count.toString() + ":result returned end.");
        return str;
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
