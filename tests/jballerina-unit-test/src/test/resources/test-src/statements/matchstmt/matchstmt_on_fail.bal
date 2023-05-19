
function testStaticMatchPatternsWithFailStmt() {
    string|int|boolean a1 = 12;
    string|int|boolean a2 = "Hello";
    string|int|boolean a3 = true;

    string|int|boolean a4 = 15;
    string|int|boolean a5 = "fail";
    string|int|boolean a6 = false;

    string|int|boolean a7 = "NothingToMatch";

    assertEquals("12", fooWithFail(a1));
    assertEquals("Hello", fooWithFail(a2));
    assertEquals("true", fooWithFail(a3));
    assertEquals("15", fooWithFail(a4));
    assertEquals("error(\"custom error\",message=\"error value\")", fooWithFail(a5));
    assertEquals("false", fooWithFail(a6));
    assertEquals("Default", fooWithFail(a7));
}

function testStaticMatchPatternsWithOnFailStmtWithoutVariable() {
    string|int|boolean a1 = 12;
    string|int|boolean a2 = "fail";
    string|int|boolean a3 = "NothingToMatch";

    assertEquals("12", onFailWithoutVariable(a1));
    assertEquals("Failed!", onFailWithoutVariable(a2));
    assertEquals("Default", onFailWithoutVariable(a3));
}

function testStaticMatchPatternsWithCheckExpr() {
    string|int|boolean a1 = 12;
    string|int|boolean a2 = "Hello";
    string|int|boolean a3 = true;

    string|int|boolean a4 = 15;
    string|int|boolean a5 = "check";
    string|int|boolean a6 = false;

    string|int|boolean a7 = "NothingToMatch";

    assertEquals("12", fooWithCheck(a1));
    assertEquals("Hello", fooWithCheck(a2));
    assertEquals("true", fooWithCheck(a3));
    assertEquals("15", fooWithCheck(a4));
    assertEquals("error(\"Check Error\")", fooWithCheck(a5));
    assertEquals("false", fooWithCheck(a6));
    assertEquals("Default", fooWithCheck(a7));
}

function onFailWithoutVariable(string|int|boolean a) returns string {
    match a {
        12 => {
            return "12";
        }
        "fail" => {
            error err = error("custom error", message = "error value");
            fail err;
        }
    } on fail {
        return "Failed!";
    }
    return "Default";
}

function fooWithFail(string|int|boolean a) returns string {
    match a {
        12 => {
            return "12";
        }
        "Hello" => {
            return "Hello";
        }
        15 => {
            return "15";
        }
        true => {
            return "true";
        }
        false => {
            return "false";
        }
        "fail" => {
            error err = error("custom error", message = "error value");
            fail err;
        }
    } on fail error e {
        return e.toString();
    }

    return "Default";
}

function fooWithCheck(string|int|boolean a) returns string {
    match a {
        12 => {
            return "12";
        }
        "Hello" => {
            return "Hello";
        }
        15 => {
            return "15";
        }
        true => {
            return "true";
        }
        false => {
            return "false";
        }
        "check" => {
            string str = check getCheckError();
            return str;
        }
    } on fail error e {
        return e.toString();
    }

    return "Default";
}

function getError() returns string|error {
    error err = error("Custom Error");
    return err;
}

function getCheckError() returns string|error {
    error err = error("Check Error");
    return err;
}

function testNestedMatchPatternsWithFail() {
    string|int|boolean a1 = 12;
    string|int|boolean a2 = "Hello";

    string|int|boolean a3 = 15;
    string|int|boolean a4 = "HelloWorld";

    string|int|boolean a5 = "fail";
    string|int|boolean a6 = "fail";

    string|int|boolean a7 = "NothingToMatch";
    string|int|boolean a8 = false;

    string|int|boolean a9 = 15;
    string|int|boolean a10 = 34;

    string|int|boolean a11 = true;
    string|int|boolean a12 = false;

    assertEquals("12", barWithFail(a1, a2));
    assertEquals("15 & HelloWorld", barWithFail(a3, a4));
    assertEquals("error(\"custom error\",message=\"error value\")", barWithFail(a5, a6));
    assertEquals("Default", barWithFail(a7, a8));
    assertEquals("15 & 34", barWithFail(a9, a10));
    assertEquals("true", barWithFail(a11, a12));
}

function barWithFail(string|int|boolean a, string|int|boolean b) returns string {
    match a {
        12 => {
            return "12";
        }
        "Hello" => {
            return "Hello";
        }
        15 => {
            match b {
                34 => {
                    return "15 & 34";
                }
                "HelloWorld" => {
                    return "15 & HelloWorld";
                }
            }
        }
        "fail" => {
            match b {
                "fail" => {
                    error err = error("custom error", message = "error value");
                    fail err;
                }
                "HelloWorld" => {
                    return "HelloAgain & HelloWorld";
                }
            }
        }
        true => {
            return "true";
        }
    } on fail error e {
        return e.toString();
    }

    return "Default";
}

string msg = "Value is ";

function testNestedMatchPatternsWithCheck() {
    string|int|boolean a1 = 12;
    string|int|boolean a2 = "Hello";

    string|int|boolean a3 = 15;
    string|int|boolean a4 = "HelloWorld";

    string|int|boolean a5 = "check";
    string|int|boolean a6 = "check";

    string|int|boolean a7 = "NothingToMatch";
    string|int|boolean a8 = false;

    string|int|boolean a9 = 15;
    string|int|boolean a10 = 34;

    string|int|boolean a11 = true;
    string|int|boolean a12 = false;

    assertEquals("12", barWithCheck(a1, a2));
    assertEquals("15 & HelloWorld", barWithCheck(a3, a4));
    assertEquals("error(\"Check Error\")", barWithCheck(a5, a6));
    assertEquals("Default", barWithCheck(a7, a8));
    assertEquals("15 & 34", barWithCheck(a9, a10));
    assertEquals("true", barWithCheck(a11, a12));
}

function barWithCheck(string|int|boolean a, string|int|boolean b) returns string {
    match a {
        12 => {
            return "12";
        }
        "Hello" => {
            return "Hello";
        }
        15 => {
            match b {
                34 => {
                    return "15 & 34";
                }
                "HelloWorld" => {
                    return "15 & HelloWorld";
                }
            }
        }
        "check" => {
            match b {
                "check" => {
                    string str = check getCheckError();
                    return str;
                }
                "HelloWorld" => {
                    return "HelloAgain & HelloWorld";
                }
            }
        }
        true => {
            return "true";
        }
    } on fail error e {
        return e.toString();
    }

    return "Default";
}

const DECIMAL_NUMBER = 2;

function testVarInMatchPatternWithinOnfail() {
    string res1 = getDetailErrorWithMatchedInput([2, "10"]);
    assertEquals("error(\"Custom Error\") at onfail; input received: 2, 10", res1);
    string res2 = getDetailErrorWithMatchedInput([10, "50"]);
    assertEquals("error(\"Custom Error\") at onfail; input received: 10, 50", res2);
    string res3 = getDetailErrorWithMatchedInput([20, "100"]);
    assertEquals("error(\"Custom Error\") at onfail; input received: 20, 100", res3);
    string res4 = getErrorDetailFromMultipleThrow([2, "100"]);
    assertEquals("error(\"Custom Error\") at onfail; input received: 2, 100->" +
    " Error at outer onfail : error(\"Custom Error\")", res4);
    string res5 = getErrorDetailFromMultipleThrow([10, "100"]);
    assertEquals("error(\"Custom Error\") at onfail; input received: 10, 100->" +
    " Error at outer onfail : error(\"Custom Error\")", res5);
    string res6 = getErrorDetailNestedMatch([DECIMAL_NUMBER, "10"]);
    assertEquals("error(\"Custom Error\") at onfail; input received; digits:10 num:10->" +
    " Error at outer onfail : error(\"Custom Error\")", res6);
}

function getDetailErrorWithMatchedInput([int, string] dataEntry) returns string {
    string str = "";
    match dataEntry {
        [DECIMAL_NUMBER, var digits] => {
            do {
                string _ = check getError();
            } on fail error cause {
                str += cause.toString() + " at onfail; input received: " + DECIMAL_NUMBER.toString() + ", " + digits;
            }
        }

        [10, var digits] => {
            do {
                string _ = check getError();
            } on fail error cause {
                str += cause.toString() + " at onfail; input received: 10, " + digits;
            }
        }

        [20, var digits] => {
            do {
                string _ = check getError();
            } on fail error cause {
                str += cause.toString() + " at onfail; input received: 20, " + digits;
            }
        }
    }
    return str;
}

function getErrorDetailFromMultipleThrow([int, string] dataEntry) returns string {
    string str = "";
    match dataEntry {
        [DECIMAL_NUMBER, var digits] => {
            do {
                string _ = check getError();
            } on fail error cause {
                str += cause.toString() + " at onfail; input received: " + DECIMAL_NUMBER.toString() + ", " + digits;
                fail cause;
            }
        }

        [10, var digits] => {
            do {
                string _ = check getError();
            } on fail error cause {
                str += cause.toString() + " at onfail; input received: 10, " + digits;
                fail cause;
            }
        }
    } on fail error e {
        str += "-> Error at outer onfail : " + e.toString();
    }
    return str;
}

function getErrorDetailNestedMatch([int, string] dataEntry) returns string {
    string str = "";
    match dataEntry {
        [DECIMAL_NUMBER, var digits] => {
            match digits {
                var num => {
                    do {
                        string _ = check getError();
                    } on fail error cause {
                        str += cause.toString() + " at onfail; input received; digits:" + digits + " num:" + num;
                        fail cause;
                    }
                }
            }
        }
    } on fail error e {
        str += "-> Error at outer onfail : " + e.toString();
    }
    return str;
}

type SampleErrorData record {|
    int code;
    string reason;
|};

type SampleError error<SampleErrorData>;

string testMessage = "";
int testErrorCode = 0;
string testErrorReason = "";
string matchString = "fail";
string matchRes = "";

function testSimpleOnFailWithErrorBP() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error("error!");
        }
    } on fail error error(msg) {
        testMessage = msg;
    }
     assertEquals(testMessage, "error!");
}

function testSimpleOnFailWithErrorBPWithVar() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error("error!");
        }
    } on fail var error(msg) {
        testMessage = msg;
    }
     assertEquals(testMessage, "error!");
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithError() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
    } on fail error error(msg, code = errCode, reason = errReason) {
        testErrorCode = errCode;
        testErrorReason = errReason;
        testMessage = msg;
    }
    assertEquals(testMessage, "error!");
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorReason, "deadlock condition");
    assertEquals(testErrorCode is int, true);
    assertEquals(testErrorReason is string, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithVar() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
    } on fail var error(msg, code = errCode, reason = errReason) {
        testErrorCode = errCode;
        testErrorReason = errReason;
        testMessage = msg;
    }
    assertEquals(testMessage, "error!");
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorReason, "deadlock condition");
    assertEquals(testErrorCode is int, true);
    assertEquals(testErrorReason is string, true);
}

function testOnFailWithErrorBPHavingUserDefinedType() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
    } on fail SampleError error(msg, code = errCode, reason = errReason) {
        testErrorCode = errCode;
        testErrorReason = errReason;
        testMessage = msg;
    }
    assertEquals(testMessage, "error!");
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorReason, "deadlock condition");
    assertEquals(testErrorCode is int, true);
    assertEquals(testErrorReason is string, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail1() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
    } on fail error<SampleErrorData> error(msg, code = errCode, reason = errReason) {
        testErrorCode = errCode;
        testErrorReason = errReason;
        testMessage = msg;
    }
    assertEquals(testMessage, "error!");
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorReason, "deadlock condition");
    assertEquals(testErrorCode is int, true);
    assertEquals(testErrorReason is string, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail2() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
    } on fail error<record {int code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail3() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
    } on fail error<record {int code;}> error(code = errCode, reason = errReason) {
        testErrorCode = errCode;
        testErrorReason = errReason;
    }
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorReason, "deadlock condition");
    assertEquals(testErrorCode is int, true);
    assertEquals(testErrorReason is string, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail4() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
        }
    } on fail error<record {int|string code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingAnonDetailRecord() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            error<record {int code;}> errVar = error("error", code = 34);
            fail errVar;
        }
    } on fail error<record {int code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquals(testErrorCode, 34);
    assertEquals(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingAnonDetailRecordWithVar() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            error<record {int code;}> errVar = error("error", code = 34);
            fail errVar;
        }
    } on fail var error(msg, code = errCode) {
        testErrorCode = errCode;
        testErrorReason = msg;
    }
    assertEquals(testErrorCode, 34);
    assertEquals(testErrorCode is int, true);
    assertEquals(testErrorReason is string, true);
    assertEquals(testErrorReason, "error");
}

function testOnFailWithErrorBPHavingAnonDetailRecordWithUnionType() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            error<record {int code;}> errVar = error("error", code = 34);
            fail errVar;
        }
    } on fail error<record {int|string code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquals(testErrorCode, 34);
    assertEquals(testErrorCode is int, true);
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
    string causeMsg;
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
    } on fail SampleComplexError error(_, cause = errCause, code = errCode, pos = errorPos, infoDetails = errInfo) {
        testErrorCode = errCode;
        testErrorPosRow = errorPos[0];
        testErrorPosCol = errorPos[1];
        testErrorMoreInfo = errInfo.moreInfo;
        causeMsg = errCause.message();
    }
    assertEquals(testErrorPosRow, 30);
    assertEquals(testErrorPosRow is int, true);
    assertEquals(testErrorPosCol, 45);
    assertEquals(testErrorPosCol is int, true);
    assertEquals(testErrorMoreInfo, "deadlock condition");
    assertEquals(testErrorMoreInfo is string,  true);
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorCode is int, true);
    assertEquals(causeMsg, "Database Error");
    assertEquals(causeMsg is string, true);
}

function testOnFailWithErrorBPWithErrorArgsHavingBP2() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
    } on fail SampleComplexError error(message, cause = error(msg), code = errCode, pos = errorPos, infoDetails = errInfo) {
        testErrorCode = errCode;
        testErrorPosRow = errorPos[0];
        testErrorPosCol = errorPos[1];
        testErrorMoreInfo = errInfo.moreInfo;
        testMessage = msg;
    }
    assertEquals(testErrorPosRow, 30);
    assertEquals(testErrorPosRow is int, true);
    assertEquals(testErrorPosCol, 45);
    assertEquals(testErrorPosCol is int, true);
    assertEquals(testErrorMoreInfo, "deadlock condition");
    assertEquals(testErrorMoreInfo is string,  true);
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorCode is int, true);
    assertEquals(testMessage, "Database Error");
    assertEquals(testMessage is string, true);
}

function testOnFailWithErrorBPWithErrorArgsHavingBP3() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
    } on fail SampleComplexError error(message, cause = error(msg), code = errCode, pos = [row, col], infoDetails = errInfo) {
        testErrorCode = errCode;
        testErrorPosRow = row;
        testErrorPosCol = col;
        testErrorMoreInfo = errInfo.moreInfo;
        testMessage = msg;
    }
    assertEquals(testErrorPosRow, 30);
    assertEquals(testErrorPosRow is int, true);
    assertEquals(testErrorPosCol, 45);
    assertEquals(testErrorPosCol is int, true);
    assertEquals(testErrorMoreInfo, "deadlock condition");
    assertEquals(testErrorMoreInfo is string,  true);
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorCode is int, true);
    assertEquals(testMessage, "Database Error");
    assertEquals(testMessage is string, true);
}

function testOnFailWithErrorBPWithErrorArgsHavingBP4() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
    } on fail SampleComplexError error(_, cause = error(msg), code = errCode, pos = [row, col], infoDetails = {moreInfo: errInfo}) {
        testErrorCode = errCode;
        testErrorPosRow = row;
        testErrorPosCol = col;
        testErrorMoreInfo = errInfo;
        testMessage = msg;
    }
    assertEquals(testErrorPosRow, 30);
    assertEquals(testErrorPosRow is int, true);
    assertEquals(testErrorPosCol, 45);
    assertEquals(testErrorPosCol is int, true);
    assertEquals(testErrorMoreInfo, "deadlock condition");
    assertEquals(testErrorMoreInfo is string,  true);
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorCode is int, true);
    assertEquals(testMessage, "Database Error");
    assertEquals(testMessage is string, true);
}

function testOnFailWithErrorBPWithErrorArgsHavingBP5() {
    match matchString {
        12 => {
            matchRes = "12";
        }
        "fail" => {
            fail error SampleComplexError("Transaction Failure", cause = error("Database Error"), code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
    } on fail var error(_, cause = error(msg), code = errCode, pos = [row, col], infoDetails = {moreInfo: errInfo}) {
        testErrorCode = errCode;
        testErrorPosRow = row;
        testErrorPosCol = col;
        testErrorMoreInfo = errInfo;
        testMessage = msg;
    }
    assertEquals(testErrorPosRow, 30);
    assertEquals(testErrorPosRow is int, true);
    assertEquals(testErrorPosCol, 45);
    assertEquals(testErrorPosCol is int, true);
    assertEquals(testErrorMoreInfo, "deadlock condition");
    assertEquals(testErrorMoreInfo is string,  true);
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorCode is int, true);
    assertEquals(testMessage, "Database Error");
    assertEquals(testMessage is string, true);
}

function testMultiLevelOnFailWithErrorBP() {
    int i = 2;
    string str = "";

    while i <= 2 {
        match matchString {
            12 => {
                matchRes = "12";
            }
            "fail" => {
                str += " -> Iteration " + i.toString() + ", ";
                fail error SampleError("error!", code = 30, reason = "deadlock condition");
            }
        } on fail var error(msg1, code = errCode1, reason = errReason1) {
                str += " -> On Fail #" + i.toString();
                testMessage = msg1;
                testErrorCode = errCode1;
                testErrorReason = errReason1;
        }
        i = i + 1;
        fail error SampleError("database error!", code = 20, reason = "database connection error");
    } on fail SampleError error(msg2, code = errCode2, reason = errReason2) {
            str += " -> On Fail Final";
    }

    assertEquals(" -> Iteration 2,  -> On Fail #2 -> On Fail Final", str);
    assertEquals(testErrorCode, 30);
    assertEquals(testErrorReason, "deadlock condition");
    assertEquals(testMessage, "error!");
}

function testMultiLevelOnFailWithoutErrorInOneLevel() {
    int i = 2;
    string str = "";

    while i <= 2 {
        match matchString {
            12 => {
                matchRes = "12";
            }
            "fail" => {
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

    assertEquals(" -> Iteration 2,  -> On Fail #2", str);
    assertEquals(testErrorCode, 20);
    assertEquals(testErrorReason, "deadlock condition");
    assertEquals(testMessage, "error!");
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
