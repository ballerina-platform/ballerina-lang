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
    string|error retryErrorRes = retryError();
    if(retryErrorRes is string) {
        assertEquality("start attempt 1:error, attempt 2:error, attempt 3:result returned end.", retryErrorRes);
    } else {
         panic error("Expected a  string");
    }
    string|error testNestedRetryWithLessOnFailsRes = testNestedRetryWithLessOnFails();
     if(testNestedRetryWithLessOnFailsRes is string) {
         assertEquality("start -> within retry block 1 -> within retry block 2 -> within retry block 2 " +
         "-> within retry block 2 " +
         "-> within retry block 1 -> within retry block 2 -> within retry block 2 -> within retry block 2 " +
         "-> within retry block 1 -> within retry block 2 -> within retry block 2 -> within retry block 2 " +
         "-> within retry block 1 -> within retry block 2 -> within retry block 2 -> within retry block 2 " +
         "-> error handled -> execution completed", testNestedRetryWithLessOnFailsRes);
    } else {
         panic error("Expected a  string");
    }

    string|error testRetryReturnValRes = testRetryReturnVal();
    if(testRetryReturnValRes is string) {
         assertEquality("start -> within retry block 1 -> within retry block 2 -> within retry block 2",
         testRetryReturnValRes);
    } else {
         panic error("Expected a  string");
    }

    string appendOnFailErrorResult = testAppendOnFailError();
    assertEquality("-> Before failure throw-> Before failure throw-> Before failure throw-> Before failure throw " +
    "-> Error caught: custom error -> Execution continues...", appendOnFailErrorResult);
}

function retryError() returns string|error {
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
    } on fail error e {
        str += "-> error handled";
    }
}

function testNestedRetryWithLessOnFails () returns string|error {
    string str = "start";
    int count1 = 0;
    error err = error("custom error", message = "error value");
    retry<MyRetryManager> (3) {
        count1 += 1;
        str = str + " -> within retry block 1";
        int count2 = 0;
        retry<MyRetryManager> (2) {
           count2 += 1;
           str = str + " -> within retry block 2";
           fail err;
        }
    }
    on fail error e {
        str += " -> error handled";
    }
    str = str + " -> execution completed";
    return str;
}

function testRetryReturnVal() returns string {
    string str = "start";
    int count1 = 0;
    error err = error("custom error", message = "error value");
    retry<MyRetryManager> (3) {
        count1 += 1;
        str = str + " -> within retry block 1";
        int count2 = 0;
        retry<MyRetryManager> (2) {
           count2 += 1;
           str = str + " -> within retry block 2";
           if(count2 == 1) {
               fail err;
           }
        }
        return str;
    } on fail error e {
        str += " -> error handled";
        return str;
    }
}

function testAppendOnFailError () returns string {
   string str = "";
   retry(3) {
     error err = error("custom error", message = "error value");
     str += "-> Before failure throw";
     fail err;
   }
   on fail error e {
      str += " -> Error caught: ";
      str = str.concat(e.message());
   }
   str += " -> Execution continues...";
   return str;
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
