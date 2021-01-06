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
    string|error retrySuccessRes = retrySuccess();
    if(retrySuccessRes is string) {
        assertEquality("start attempt 1:error, attempt 2:error, attempt 3:result returned end.", retrySuccessRes);
    } else {
        panic error("Expected a string");
    }

    string|error retryFailureRes = retryFailure();
    if(retryFailureRes is error) {
        assertEquality("Custom Error", retryFailureRes.message());
    } else {
        panic error("Expected an error");
    }

    string|error ignoreErrorReturnRes = ignoreErrorReturn();
    if(ignoreErrorReturnRes is error) {
        assertEquality("Custom Error", ignoreErrorReturnRes.message());
    } else {
        panic error("Expected an error");
    }
}

function retrySuccess() returns string |error {
    string str = "start";
    int count = 0;
    retry<MyRetryManager> (3) {
        count = count+1;
        if (count < 3) {
            str += (" attempt " + count.toString() + ":error,");
            fail trxError();
        }
        str += (" attempt "+ count.toString() + ":result returned end.");
        return str;
    }
}

function retryFailure() returns string |error {
    string str = "start";
    int count = 0;
    retry<MyRetryManager> (3) {
        count = count+1;
        if (count < 5) {
            str += (" attempt " + count.toString() + ":error,");
            fail error("Custom Error");
        }
        str += (" attempt "+ count.toString() + ":result returned end.");
        return str;
    }
}

function ignoreErrorReturn() returns string|error {
    string str = "start";
    int count = 0;
    retry<MyRetryManager> (3) {
        count = count+1;
        if (count < 5) {
            str += (" attempt " + count.toString() + ":error,");
            return error("Custom Error");
        }
        str += (" attempt "+ count.toString() + ":result returned end.");
        return str;
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
