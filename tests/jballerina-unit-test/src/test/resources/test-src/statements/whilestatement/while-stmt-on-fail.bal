function testWhileStmtWithFail(int x) returns string {
    int a = 0;
    string str = "";

    while(x >= a) {
        a = a + 1;
        str = str.concat(" Value: ", a.toString());
        if(a == 3) {
         error err = error("Custom Error");
         fail err;
        }
    } on fail error e {
        str += "-> error caught. Hence value returning";
        return str;
    }
    str += "-> reached end";
    return str;
}

function testWhileStmtWithCheck(int x) returns string {
    int a = 0;
    string str = "";

    while(x >= a) {
        a = a + 1;
        str = str.concat(" Value: ", a.toString());
        if(a == 3) {
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
    string result = "";
    error err = error("Custom Error");
    int i = 0;
    while isEqual(i, 0) {
        i += 1;
        while isEqual(i, 1) {
            i += 1;
            while (i == 2) {
                i += 1;
                while (i == 0) {
                }
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

function isEqual(int i, int j) returns boolean => i == j;

function testWhileStmtLoopEndingWithFail(int x) returns string {
    int a = 0;
    string str = "";

    while(x >= a) {
        a = a + 1;
        str = str.concat(" Value: ", a.toString());
        error err = error("Custom Error");
        fail err;
    } on fail error e {
        str += "-> error caught. Hence value returning";
    }
    str += "-> reached end";
    return str;
}

function testNestedWhileStmtLoopTerminationWithFail() returns string {
    string result = "";
    error err = error("Custom Error");
    int i = 0;
    while (i >= 0) {
        i += 1;
        while (i >= 1) {
            i += 1;
            while (i >= 2) {
                i += 1;
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

function testWhileStmtWithOnFailWithoutVariable() {
    int a = 0;
    string str = "";

    while 3 >= a {
        a = a + 1;
        str = str.concat(" Value: ", a.toString());
        if a == 3 {
            error err = error("Custom Error");
            fail err;
        }
    } on fail {
        str += "-> error caught.";
    }
    str += " -> reached end";
    assertEquality(" Value: 1 Value: 2 Value: 3-> error caught. -> reached end", str);
}

type SampleErrorData record {|
    int code;
    string reason;
|};

type SampleError error<SampleErrorData>;

string testMessage = "";
int testErrorCode = 0;
string testErrorReason = "";
int whileIndex = 0;

function testSimpleOnFailWithErrorBP() {
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error("error!");
        }
    } on fail error error(msg) {
        testMessage = msg;
    }
     assertEquality(testMessage, "error!");
}

function testSimpleOnFailWithErrorBPWithVar() {
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error("error!");
        }
    } on fail var error(msg) {
        testMessage = msg;
    }
     assertEquality(testMessage, "error!");
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithError() {
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
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
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
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
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
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
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
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
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
    } on fail error<record {int code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail3() {
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
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
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
    } on fail error<record {int|string code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingAnonDetailRecord() {
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            error<record {int code;}> errVar = error("error", code = 34);
            fail errVar;
        }
    } on fail error<record {int code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 34);
    assertEquality(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingAnonDetailRecordWithVar() {
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            error<record {int code;}> errVar = error("error", code = 34);
            fail errVar;
        }
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
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            error<record {int code;}> errVar = error("error", code = 34);
            fail errVar;
        }
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

int testErrorPosRow = 0;
int testErrorPosCol = 0;
string testErrorMoreInfo = "";


function testOnFailWithErrorBPWithErrorArgsHavingBP1() {
    string causeMsg = "";
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
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
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
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
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
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
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
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
    whileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
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

int testErrorCodeNested = 0;
string testMessageNested = "";

function testNestedOnFailWithErrorBP() {
    string testErrorReasonNested = "";
    whileIndex = 0;
    int innerWhileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
    } on fail var error(msg1, code = errCode1, reason = errReason1) {
        if errCode1 == 20 {
            while innerWhileIndex >= 0 {
                innerWhileIndex = innerWhileIndex + 1;
                if innerWhileIndex == 1 {
                    fail error SampleError("nested error!", code = 30, reason = "database error");
                }
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
    whileIndex = 0;
    int innerWhileIndex = 0;
    while whileIndex >= 0 {
        whileIndex = whileIndex + 1;
        if whileIndex == 1 {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
    } on fail var error(msg1, code = errCode1, reason = errReason1) {
        if errCode1 == 20 {
            while innerWhileIndex >= 0 {
                innerWhileIndex = innerWhileIndex + 1;
                if innerWhileIndex == 1 {
                    fail error SampleComplexError("nested error!", cause = error("Database Error"), code = 30, pos = [35, 45], infoDetails = {moreInfo: "deadlock condition"});
                }
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
    int i = 0;
    string str = "";
    whileIndex = 0;

    while i == 0 {
        while whileIndex == 0 {
            whileIndex = whileIndex + 1;
            if (whileIndex == 1) {
                str += " -> Iteration " + i.toString() + ", ";
                fail error SampleError("error!", code = 20, reason = "deadlock condition");
            }
        } on fail var error(msg1, code = errCode1, reason = errReason1) {
                str += " -> On Fail #" + i.toString();
                testMessage = msg1;
                testErrorCode = errCode1;
                testErrorReason = errReason1;
        }
        i = i + 1;
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail SampleError error(msg2, code = errCode2, reason = errReason2) {
            str += " -> On Fail Final";
    }

    assertEquality(" -> Iteration 0,  -> On Fail #0 -> On Fail Final", str);
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorReason, "deadlock condition");
    assertEquality(testMessage, "error!");
}

function testMultiLevelOnFailWithoutErrorInOneLevel() {
    int i = 0;
    string str = "";
    whileIndex = 0;

    while i == 0 {
        while whileIndex == 0 {
            whileIndex = whileIndex + 1;
            if (whileIndex == 1) {
                str += " -> Iteration " + i.toString() + ", ";
                fail error SampleError("error!", code = 20, reason = "deadlock condition");
            }
        } on fail var error(msg1, code = errCode1, reason = errReason1) {
                str += " -> On Fail #" + i.toString();
                testMessage = msg1;
                testErrorCode = errCode1;
                testErrorReason = errReason1;
        }
        i = i + 1;
    } on fail SampleError error(msg2, code = errCode2, reason = errReason2) {
            str += " -> On Fail Final";
    }

    assertEquality(" -> Iteration 0,  -> On Fail #0", str);
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorReason, "deadlock condition");
    assertEquality(testMessage, "error!");
}

function assertEquality(anydata expected, anydata actual) {
    if actual == expected {
        return;
    }
    panic error("AssertionError", message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
