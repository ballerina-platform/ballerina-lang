int[] data = [1, -3, 5, -30, 4, 11, -25, 10];

function testIntArrayWithArityOne() returns string {
    string dataStr = "";
    int negativeCount = 0;
    foreach var i in data {
        if(i > 0) {
            dataStr = dataStr + " (Positive:" + i.toString() + "),";
        } else {
             dataStr += " (Negative:" + i.toString() + ")";
             negativeCount += 1;
             if(negativeCount > 2) {
                 error err = error("Throttle reached");
                  fail err;
             }
             dataStr += " within grace,";
        }
    } on fail error e {
        dataStr += " " + e.message();
    }
    return dataStr;
}

function testForeachStmtWithCheck() returns string {
    string str = "";

     foreach var i in data {
         str = str.concat("Value: ", i.toString(), " ");
         if(i < 0) {
             int val = check getError();
             str = str.concat(" Value: ", val.toString());
         }
     } on fail error e {
         str += "-> error caught. Hence value returning";
         return str;
     }
     str += "-> reached end";
     return str;
}

function getError()  returns int|error {
    error err = error("Custom Error");
    return err;
}

function testNestedWhileStmtWithFail() returns string {
    int[] data = [1];
    string result = "";
    error err = error("Custom Error");
    foreach var i in data {
        foreach var j in data {
            foreach var k in data {
                result = result + "level3";
                fail err;
            } on fail error e3 {
                result = result + "-> error caught at level 3, ";
            }
            result = result + "level2";
            fail err;
        } on fail error e2 {
            result = result + "-> error caught at level 2, ";
        }
        result = result + "level1";
        fail err;
    } on fail error e1 {
         result = result + "-> error caught at level 1.";
    }
    return result;
}

function testNestedForeachLoopBreak() returns string {
    int[] data1 = [1, 2, 3];
    int[] data2 = [1];
    string result = "";
    error err = error("Custom Error");
    foreach var i in data1 {
        foreach var j in data2 {
            foreach var k in data2 {
                result = result + "level3";
                fail err;
            } on fail error e3 {
                result = result + "-> error caught at level 3, ";
            }
            result = result + "level2";
            fail err;
        } on fail error e2 {
            result = result + "-> error caught at level 2, ";
        }
        result = result + "level1";
        fail err;
    } on fail error e1 {
         result = result + "-> error caught at level 1.";
    }
    return result;
}

type SampleErrorData record {|
    int code;
    string reason;
|};

type SampleError error<SampleErrorData>;

function testSimpleOnFailWithErrorBP() {
    string testMessage;
    foreach var i in data {
        fail error("error!");
    } on fail error error(msg) {
        testMessage = msg;
    }
    assertEquality(testMessage, "error!");
}

function testSimpleOnFailWithErrorBPWithVar() {
    string testMessage;
    foreach var i in data {
        fail error("error!");
    } on fail var error(msg) {
        testMessage = msg;
    }
    assertEquality(testMessage, "error!");
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithError() {
    int testErrorCode;
    string testMessage;
    string testErrorReason;
    foreach var i in data {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail error error(msg, code = errCode, reason = errReason) {
        testErrorCode = errCode;
        testErrorReason = errReason;
        testMessage = msg;
    }
    assertEquality(testMessage, "error!");
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorReason, "deadlock condition");
    assertEquality(testErrorCode is int, true);
    assertEquality(testErrorReason is string, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithVar() {
    string testMessage;
    int testErrorCode;
    string testErrorReason;
    foreach var i in data {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail var error(msg, code = errCode, reason = errReason) {
        testErrorCode = errCode;
        testErrorReason = errReason;
        testMessage = msg;
    }
    assertEquality(testMessage, "error!");
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorReason, "deadlock condition");
    assertEquality(testErrorCode is int, true);
    assertEquality(testErrorReason is string, true);
}

function testOnFailWithErrorBPHavingUserDefinedType() {
    string testMessage;
    int testErrorCode;
    string testErrorReason;
    foreach var i in data {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail SampleError error(msg, code = errCode, reason = errReason) {
        testErrorCode = errCode;
        testErrorReason = errReason;
        testMessage = msg;
    }
    assertEquality(testMessage, "error!");
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorReason, "deadlock condition");
    assertEquality(testErrorCode is int, true);
    assertEquality(testErrorReason is string, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail1() {
    string testMessage;
    int testErrorCode;
    string testErrorReason;
    foreach var i in data {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail error<SampleErrorData> error(msg, code = errCode, reason = errReason) {
        testErrorCode = errCode;
        testErrorReason = errReason;
        testMessage = msg;
    }
    assertEquality(testMessage, "error!");
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorReason, "deadlock condition");
    assertEquality(testErrorCode is int, true);
    assertEquality(testErrorReason is string, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail2() {
    int testErrorCode;
    foreach var i in data {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail error<record {int code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail3() {
    int testErrorCode;
    string testErrorReason;
    foreach var i in data {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail error<record {int code;}> error(code = errCode, reason = errReason) {
        testErrorCode = errCode;
        testErrorReason = errReason;
    }
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorReason, "deadlock condition");
    assertEquality(testErrorCode is int, true);
    assertEquality(testErrorReason is string, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail4() {
    int testErrorCode;
    foreach var i in data {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail error<record {int|string code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingAnonDetailRecord() {
    int testErrorCode;
    foreach var i in data {
        error<record {int code;}> errVar = error("error", code = 34);
        fail errVar;
    } on fail error<record {int code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 34);
    assertEquality(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingAnonDetailRecordWithVar() {
    int testErrorCode;
    string testErrorReason;
    foreach var i in data {
        error<record {int code;}> errVar = error("error", code = 34);
        fail errVar;
    } on fail var error(msg, code = errCode) {
        testErrorCode = errCode;
        testErrorReason = msg;
    }
    assertEquality(testErrorCode, 34);
    assertEquality(testErrorCode is int, true);
    assertEquality(testErrorReason is string, true);
    assertEquality(testErrorReason, "error");
}

function testOnFailWithErrorBPHavingAnonDetailRecordWithUnionType() {
    int testErrorCode;
    foreach var i in data {
        error<record {int code;}> errVar = error("error", code = 34);
        fail errVar;
    } on fail error<record {int|string code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 34);
    assertEquality(testErrorCode is int, true);
}

type SampleComplexErrorData record {|
    error cause;
    int code;
    int[2] pos;
    record {string moreInfo;} infoDetails;
|};

type SampleComplexError error<SampleComplexErrorData>;

function testOnFailWithErrorBPWithErrorArgsHavingBP1() {
    string causeMsg;
    int testErrorCode;
    int testErrorPosRow;
    int testErrorPosCol;
    string testErrorMoreInfo;
    foreach var i in data {
        fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
    } on fail SampleComplexError error(_, cause = errCause, code = errCode, pos = errorPos, infoDetails = errInfo) {
        testErrorCode = errCode;
        testErrorPosRow = errorPos[0];
        testErrorPosCol = errorPos[1];
        testErrorMoreInfo = errInfo.moreInfo;
        causeMsg = errCause.message();
    }
    assertEquality(testErrorPosRow, 30);
    assertEquality(testErrorPosRow is int, true);
    assertEquality(testErrorPosCol, 45);
    assertEquality(testErrorPosCol is int, true);
    assertEquality(testErrorMoreInfo, "deadlock condition");
    assertEquality(testErrorMoreInfo is string,  true);
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
    assertEquality(causeMsg, "Database Error");
    assertEquality(causeMsg is string, true);
}

function testOnFailWithErrorBPWithErrorArgsHavingBP2() {
    string testMessage;
    int testErrorCode;
    int testErrorPosRow;
    int testErrorPosCol;
    string testErrorMoreInfo;
    foreach var i in data {
        fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
    } on fail SampleComplexError error(message, cause = error(msg), code = errCode, pos = errorPos, infoDetails = errInfo) {
        testErrorCode = errCode;
        testErrorPosRow = errorPos[0];
        testErrorPosCol = errorPos[1];
        testErrorMoreInfo = errInfo.moreInfo;
        testMessage = msg;
    }
    assertEquality(testErrorPosRow, 30);
    assertEquality(testErrorPosRow is int, true);
    assertEquality(testErrorPosCol, 45);
    assertEquality(testErrorPosCol is int, true);
    assertEquality(testErrorMoreInfo, "deadlock condition");
    assertEquality(testErrorMoreInfo is string,  true);
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
    assertEquality(testMessage, "Database Error");
    assertEquality(testMessage is string, true);
}

function testOnFailWithErrorBPWithErrorArgsHavingBP3() {
    string testMessage;
    int testErrorCode;
    int testErrorPosRow;
    int testErrorPosCol;
    string testErrorMoreInfo;
    foreach var i in data {
        fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
    } on fail SampleComplexError error(message, cause = error(msg), code = errCode, pos = [row, col], infoDetails = errInfo) {
        testErrorCode = errCode;
        testErrorPosRow = row;
        testErrorPosCol = col;
        testErrorMoreInfo = errInfo.moreInfo;
        testMessage = msg;
    }
    assertEquality(testErrorPosRow, 30);
    assertEquality(testErrorPosRow is int, true);
    assertEquality(testErrorPosCol, 45);
    assertEquality(testErrorPosCol is int, true);
    assertEquality(testErrorMoreInfo, "deadlock condition");
    assertEquality(testErrorMoreInfo is string,  true);
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
    assertEquality(testMessage, "Database Error");
    assertEquality(testMessage is string, true);
}

function testOnFailWithErrorBPWithErrorArgsHavingBP4() {
    string testMessage;
    int testErrorCode;
    int testErrorPosRow;
    int testErrorPosCol;
    string testErrorMoreInfo;
    foreach var i in data {
        fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
    } on fail SampleComplexError error(_, cause = error(msg), code = errCode, pos = [row, col], infoDetails = {moreInfo: errInfo}) {
        testErrorCode = errCode;
        testErrorPosRow = row;
        testErrorPosCol = col;
        testErrorMoreInfo = errInfo;
        testMessage = msg;
    }
    assertEquality(testErrorPosRow, 30);
    assertEquality(testErrorPosRow is int, true);
    assertEquality(testErrorPosCol, 45);
    assertEquality(testErrorPosCol is int, true);
    assertEquality(testErrorMoreInfo, "deadlock condition");
    assertEquality(testErrorMoreInfo is string,  true);
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
    assertEquality(testMessage, "Database Error");
    assertEquality(testMessage is string, true);
}

function testOnFailWithErrorBPWithErrorArgsHavingBP5() {
    string testMessage;
    int testErrorCode;
    int testErrorPosRow;
    int testErrorPosCol;
    string testErrorMoreInfo;
    foreach var i in data {
        fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
    } on fail var error(_, cause = error(msg), code = errCode, pos = [row, col], infoDetails = {moreInfo: errInfo}) {
        testErrorCode = errCode;
        testErrorPosRow = row;
        testErrorPosCol = col;
        testErrorMoreInfo = errInfo;
        testMessage = msg;
    }
    assertEquality(testErrorPosRow, 30);
    assertEquality(testErrorPosRow is int, true);
    assertEquality(testErrorPosCol, 45);
    assertEquality(testErrorPosCol is int, true);
    assertEquality(testErrorMoreInfo, "deadlock condition");
    assertEquality(testErrorMoreInfo is string,  true);
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
    assertEquality(testMessage, "Database Error");
    assertEquality(testMessage is string, true);
}

function testNestedOnFailWithErrorBP() {
    string testMessage = "";
    int testErrorCode = 0;
    string testErrorReason = "";
    int testErrorCodeNested = 0;
    string testMessageNested = "";
    string testErrorReasonNested = "";
    foreach var i in data {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail var error(msg1, code = errCode1, reason = errReason1) {
        if errCode1 == 20 {
            foreach var j in data {
                fail error SampleError("nested error!", code = 30, reason = "database error");
            } on fail var error(msg2, code = errCode2, reason = errReason2) {
                testErrorCode = errCode1;
                testErrorCodeNested = errCode2;
                testErrorReason = errReason1;
                testErrorReasonNested = errReason2;
                testMessage = msg1;
                testMessageNested = msg2;
            }
        }
    }

    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCodeNested, 30);
    assertEquality(testErrorReason, "deadlock condition");
    assertEquality(testErrorReasonNested, "database error");
    assertEquality(testMessage, "error!");
    assertEquality(testMessageNested, "nested error!");
}

function testNestedOnFailWithErrorBPWithErrorArgsHavingBP() {
    string testMessage = "";
    int testErrorCode = 0;
    int testErrorCodeNested = 0;
    string testMessageNested = "";
    int testErrorPosRow = 0;
    int testErrorPosCol = 0;
    string testErrorMoreInfo = "";
    foreach var i in data {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail var error(msg1, code = errCode1, reason = errReason1) {
        if errCode1 == 20 {
            foreach var j in data {
                fail error SampleComplexError("nested error!", cause = error("Database Error"), code = 30, pos = [35, 45], infoDetails = {moreInfo: "deadlock condition"});
            } on fail SampleComplexError error(_, cause = error(msg2), code = errCode2, pos = [row, col], infoDetails = {moreInfo: errInfo}) {
                testErrorCode = errCode1;
                testErrorCodeNested = errCode2;
                testMessage = msg1;
                testMessageNested = msg2;
                testErrorPosRow = row;
                testErrorPosCol = col;
                testErrorMoreInfo = errInfo;
            }
        }
    }

    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCodeNested, 30);
    assertEquality(testErrorMoreInfo, "deadlock condition");
    assertEquality(testMessage, "error!");
    assertEquality(testMessageNested, "Database Error");
    assertEquality(testErrorPosRow, 35);
    assertEquality(testErrorPosCol, 45);
}

function testMultiLevelOnFailWithErrorBP() {
    string? testMessage = ();
    int? testErrorCode = ();
    string? testErrorReason = ();
    string str = "";
    int[] level1dData = [2];
    int[] nestedData = [1];

    foreach var i in level1dData {
        foreach var j in nestedData {
            str += " -> Iteration " + i.toString() + ", ";
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        } on fail var error(msg1, code = errCode1, reason = errReason1) {
                str += " -> On Fail #" + i.toString();
                testMessage = msg1;
                testErrorCode = errCode1;
                testErrorReason = errReason1;
        }
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail SampleError error(msg2, code = errCode2, reason = errReason2) {
            str += " -> On Fail Final";
    }

    assertEquality(" -> Iteration 2,  -> On Fail #2 -> On Fail Final", str);
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorReason, "deadlock condition");
    assertEquality(testMessage, "error!");
}

function testMultiLevelOnFailWithoutErrorInOneLevel() {
    string str = "";
    int[] nestedData = [1];
    int[] level1dData = [2];
    string? testMessage = ();
    int? testErrorCode = ();
    string? testErrorReason = ();

    foreach var i in level1dData {
        foreach var j in nestedData {
            str += " -> Iteration " + i.toString() + ", ";
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        } on fail var error(msg1, code = errCode1, reason = errReason1) {
                str += " -> On Fail #" + i.toString();
                testMessage = msg1;
                testErrorCode = errCode1;
                testErrorReason = errReason1;
        }
    } on fail SampleError error(msg2, code = errCode2, reason = errReason2) {
            str += " -> On Fail Final";
    }

    assertEquality(" -> Iteration 2,  -> On Fail #2", str);
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorReason, "deadlock condition");
    assertEquality(testMessage, "error!");
}

type AssertionError error;

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error AssertionError("AssertionError",
                            message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
