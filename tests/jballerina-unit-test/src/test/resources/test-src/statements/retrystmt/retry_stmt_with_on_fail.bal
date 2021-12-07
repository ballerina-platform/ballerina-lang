import ballerina/lang.'error as errors;

public class MyRetryManager {
    private int count;
    public function init(int count = 3) {
        self.count = count;
    }
    public function shouldRetry(error e) returns boolean {
        if e is error && self.count >  0 {
            self.count -= 1;
            return true;
        } else {
            return false;
        }
    }
}

function testRetryStatement() {
    string|error retryErrorWithFailRes = retryErrorWithFail();
    if(retryErrorWithFailRes is string) {
        assertEquality("start attempt 1:error, attempt 2:error, attempt 3:result returned end.", retryErrorWithFailRes);
    } else {
         panic error("Expected a  string");
    }

    string|error retryErrorWithCheckRes = retryErrorWithCheck();
    if(retryErrorWithCheckRes is string) {
        assertEquality("start attempt 1:error, attempt 2:error, attempt 3:error, attempt 4:error,-> error handled",
        retryErrorWithCheckRes);
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

    string|error testNestedRetryLessOnFailSuccessRes = testNestedRetryLessOnFailSuccess();
     if(testNestedRetryLessOnFailSuccessRes is string) {
         assertEquality("start -> within retry block 1 -> within retry block 2 -> within retry block 3 " +
         "-> within retry block 3 -> execution completed", testNestedRetryLessOnFailSuccessRes);
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

    string nestedRetryOnFailJumpRes = testNestedRetryOnFailJump();
    assertEquality("start -> within retry block 1 -> within retry block 2 -> within retry block 2 " +
    "-> within retry block 2 -> within retry block 1 -> within retry block 2 -> within retry block 2 " +
    "-> within retry block 2 -> within retry block 1 -> within retry block 2 -> within retry block 2 " +
    "-> within retry block 2 -> within retry block 1 -> within retry block 2 -> within retry block 2 " +
    "-> within retry block 2 -> error handled", nestedRetryOnFailJumpRes);

    string nestedRetryOnFailJump2Res = testNestedRetryOnFailJump2();
    assertEquality("start -> within retry block 1 -> within retry block 2 -> within retry block 3 " +
    "-> within retry block 3 -> within retry block 2 -> within retry block 3 -> within retry block 3 " +
    "-> within retry block 1 -> within retry block 2 -> within retry block 3 -> within retry block 3 " +
    "-> within retry block 2 -> within retry block 3 -> within retry block 3 -> within retry block 1 " +
    "-> within retry block 2 -> within retry block 3 -> within retry block 3 -> within retry block 2 " +
    "-> within retry block 3 -> within retry block 3 -> error handled", nestedRetryOnFailJump2Res);
}

function retryErrorWithFail() returns string|error {
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

function retryErrorWithCheck() returns string|error {
    string str = "start";
    int count = 0;
    retry<MyRetryManager> (3) {
        count = count+1;
        if (count < 5) {
            str += (" attempt " + count.toString() + ":error,");
            string ignoreString = check trxErrorOrString();
        }
        str += (" attempt "+ count.toString() + ":result returned end.");
        return str;
    } on fail error e {
        str += "-> error handled";
        return str;
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

function testNestedRetryLessOnFailSuccess () returns string|error {
    string str = "start";
    int count1 = 0;
    error err = error("custom error", message = "error value");
    retry<MyRetryManager> (3) {
        count1 += 1;
        str = str + " -> within retry block 1";
        int count2 = 0;
        retry<MyRetryManager> (1) {
           count2 += 1;
           str = str + " -> within retry block 2";
           int count3 = 0;
           retry<MyRetryManager> (1) {
              count3 += 1;
              str = str + " -> within retry block 3";
              if(count3 < 2) {
                  fail err;
              }
           }
        }
    } on fail error e {
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
     error err = error errors:Retriable("custom error", message = "error value");
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

function trxErrorOrString()  returns string|error {
    if(0 < 1) {
        return error("TransactionError");
    }
    return "Custom String";
}

function testNestedRetryOnFailJump() returns string {
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
        str = str + " -> should not reach here";
    } on fail error e {
        str += " -> error handled";
    }
    return str;
}

function testNestedRetryOnFailJump2() returns string {
    string str = "start";
    error err = error("custom error", message = "error value");
    retry<MyRetryManager> (2) {
        str = str + " -> within retry block 1";
        retry<MyRetryManager> (1) {
            str = str + " -> within retry block 2";
            retry<MyRetryManager> (1) {
                str = str + " -> within retry block 3";
                fail err;
            }
            str = str + " -> should not reach here L2";
        }
        str = str + " -> should not reach here L1";
    } on fail error e {
        str += " -> error handled";
    }
    return str;
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
