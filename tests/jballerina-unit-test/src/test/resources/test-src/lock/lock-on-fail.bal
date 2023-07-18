// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
int lockWithinLockInt1 = 0;

string lockWithinLockString1 = "";

byte[] blobValue = [];

boolean boolValue = false;

function failLockWithinLock() returns [int, string] {
    lock {
        lockWithinLockInt1 = 50;
        lockWithinLockString1 = "sample value";
        lock {
            lockWithinLockString1 = "second sample value";
            lockWithinLockInt1 = 99;
            lock {
                lockWithinLockInt1 = 90;
            }
            error err = error("custom error", message = "error value");
            fail err;
        }
    } on fail error e {
        lockWithinLockInt1 = 100;
        lockWithinLockString1 = "Error caught";
    }
    return [lockWithinLockInt1, lockWithinLockString1];
}


function checkLockWithinLock() returns [int, string] {
    lock {
        lockWithinLockInt1 = 50;
        lockWithinLockString1 = "sample value";
        lock {
            lockWithinLockString1 = "second sample value";
            lockWithinLockInt1 = 99;
            lock {
                lockWithinLockInt1 = 90;
            }
            lockWithinLockInt1 = check getError();
        }
        lockWithinLockInt1 = 77;
    } on fail error e {
        lockWithinLockInt1 = 100;
        lockWithinLockString1 = "Error caught";
    }
    return [lockWithinLockInt1, lockWithinLockString1];
}

function onFailLockWithinLockWithoutVariable() {
    lock {
        lockWithinLockInt1 = 50;
        lockWithinLockString1 = "sample value";
        lock {
            lockWithinLockString1 = "second sample value";
            lockWithinLockInt1 = 99;
            lock {
                lockWithinLockInt1 = 90;
            }
            error err = error("custom error", message = "error value");
            fail err;
        }
    } on fail {
        lockWithinLockInt1 = 100;
        lockWithinLockString1 = "Error handled";
    }

    assertEquality(100, lockWithinLockInt1);
    assertEquality("Error handled", lockWithinLockString1);
}

function getError()  returns int|error {
    error err = error("Custom Error");
    return err;
}

type SampleErrorData record {|
    int code;
    string reason;
|};

type SampleError error<SampleErrorData>;

string testMessage = "";
int testErrorCode = 0;
string testErrorReason = "";

function testSimpleOnFailWithErrorBP() {
    lock {
        lockWithinLockInt1 = 99;
        fail error("error!");
    } on fail error error(msg) {
        testMessage = msg;
    }
    assertEquality(testMessage, "error!");
}

function testSimpleOnFailWithErrorBPWithVar() {
    lock {
        lockWithinLockInt1 = 99;
        fail error("error!");
    } on fail var error(msg) {
        testMessage = msg;
    }
     assertEquality(testMessage, "error!");
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithError() {
    lock {
        lockWithinLockInt1 = 99;
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
    lock {
        lockWithinLockInt1 = 99;
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
    lock {
        lockWithinLockInt1 = 99;
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
    lock {
        lockWithinLockInt1 = 99;
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
    lock {
        lockWithinLockInt1 = 99;
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail error<record {int code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail3() {
    lock {
        lockWithinLockInt1 = 99;
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
    lock {
        lockWithinLockInt1 = 99;
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail error<record {int|string code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingAnonDetailRecord() {
    lock {
        lockWithinLockInt1 = 99;
        error<record {int code;}> errVar = error("error", code = 34);
        fail errVar;
    } on fail error<record {int code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 34);
    assertEquality(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingAnonDetailRecordWithVar() {
    lock {
        lockWithinLockInt1 = 99;
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
    lock {
        lockWithinLockInt1 = 99;
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

int testErrorPosRow = 0;
int testErrorPosCol = 0;
string testErrorMoreInfo = "";


function testOnFailWithErrorBPWithErrorArgsHavingBP1() {
    string causeMsg;
    lock {
        lockWithinLockInt1 = 99;
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
    lock {
        lockWithinLockInt1 = 99;
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
    lock {
        lockWithinLockInt1 = 99;
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
    lock {
        lockWithinLockInt1 = 99;
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
    lock {
        lockWithinLockInt1 = 99;
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

function testMultiLevelOnFailWithErrorBP() {
    int i = 2;
    string str = "";

    lock {
        lock {
            str += " -> Iteration " + i.toString() + ", ";
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
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

    assertEquality(" -> Iteration 2,  -> On Fail #2 -> On Fail Final", str);
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorReason, "deadlock condition");
    assertEquality(testMessage, "error!");
}

function testMultiLevelOnFailWithoutErrorInOneLevel() {
    int i = 2;
    string str = "";

    lock {
        lock {
            str += " -> Iteration " + i.toString() + ", ";
            fail error SampleError("error!", code = 20, reason = "deadlock condition");
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

    assertEquality(" -> Iteration 2,  -> On Fail #2", str);
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorReason, "deadlock condition");
    assertEquality(testMessage, "error!");
}

function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
