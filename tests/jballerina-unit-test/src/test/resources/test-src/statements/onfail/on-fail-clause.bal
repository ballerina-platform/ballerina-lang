// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/jballerina.java;

function testOnFailEdgeTestcases() {
    testUnreachableCodeWithIf();
    testSimpleOnFailWithoutVariable();
    testMultiLevelOnFail();
    testMultiLevelOnFailWithoutVariableVariationOne();
    testMultiLevelOnFailWithoutVariableVariationTwo();
    testMultiLevelOnFailWithoutVariableVariationThree();
    testRetryOnFailWithinWhile();
    testOnFailWithinAnonFunc();
    testRetryOnFailWithinObjectFunc();
    testFailExprWithinOnFail();
    testCheckExprWithinOnFail();
    testFailPassWithinOnFail();
    testTypeNarrowingInsideOnfail();
    testBreakWithinOnfail();
    assertEquality(" -> Before error thrown,  -> Error caught at level #1 -> Error caught at levet #2",
    testReturnWithinOnfail());
    assertEquality(1, testIntReturnWithinOnfail());
    testBreakWithinOnfailForOuterLoop();
    assertEquality(44, testLambdaFunctionWithOnFail());
    testOnFailWithinInLineServiceObj();
    assertTrue(testOnFailInAnonFunctionExpr() is ());
    testNoPossibleFailureWithOnFail();
}

function testUnreachableCodeWithIf(){
    int i = 0;
    string str = "";

    while (i < 3) {
        if(i == 2) {
            str += " -> Before error thrown";
            fail getError();
        }

        str += " -> Inside loop: " + i.toString() + ",";
        i = i + 1;
    } on fail error e {
        str += " -> Error caught!";
    }

    assertEquality(" -> Inside loop: 0, -> Inside loop: 1, -> Before error thrown -> Error caught!", str);
}

function testMultiLevelOnFail() {
    int i = 0;
    string str = "";

    while (i < 2) {
        do {
            str += " -> Before error thrown, ";
            fail getError();
        } on fail error e {
            str += " -> Error caught at level #1";
        }

        i = i + 1;
    } on fail error e {
        str += " -> Error caught at levet #2";
    }

    assertEquality(" -> Before error thrown,  -> Error caught at level #1 -> Before error thrown,  -> Error caught at level #1", str);
}

function testMultiLevelOnFailWithoutVariableVariationOne() {
    int i = 1;
    string str = "";

    while i <= 2 {
        do {
            str += " -> Iteration " + i.toString() + ", ";
            fail getError();
        } on fail {
            str += " -> On Fail #" + i.toString();
        }
        i = i + 1;
    } on fail error e {
        str += " -> On Fail Final";
    }

    assertEquality(" -> Iteration 1,  -> On Fail #1 -> Iteration 2,  -> On Fail #2", str);
}

function testMultiLevelOnFailWithoutVariableVariationTwo() {
    int i = 1;
    string str = "";

    while i <= 2 {
        do {
            str += " -> Iteration " + i.toString() + ", ";
            fail getError();
        } on fail error e {
            str += " -> On Fail #" + i.toString();
        }
        i = i + 1;
    } on fail {
        str += " -> On Fail Final";
    }

    assertEquality(" -> Iteration 1,  -> On Fail #1 -> Iteration 2,  -> On Fail #2", str);
}

function testMultiLevelOnFailWithoutVariableVariationThree() {
    int i = 1;
    string str = "";

    while i <= 2 {
        do {
            str += " -> Iteration " + i.toString() + ", ";
            fail getError();
        } on fail {
            str += " -> On Fail #" + i.toString();
        }
        i = i + 1;
    } on fail {
        str += " -> On Fail Final";
    }

    assertEquality(" -> Iteration 1,  -> On Fail #1 -> Iteration 2,  -> On Fail #2", str);
}

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

function testRetryOnFailWithinWhile() {
    int i = 0;
    while (i < 2) {
        string str = "start";
        int count = 0;
        retry<MyRetryManager> (2) {
            count = count+1;
            if (count < 2) {
                str += (" attempt " + count.toString() + ":error,");
                fail trxError();
            }
            str += (" attempt "+ count.toString() + ":result returned end.");
        } on fail error e {
            str += "-> error handled";
        }

        i = i+1;
        assertEquality("start attempt 1:error, attempt 2:result returned end.", str);
    }

    assertEquality(2, i);
}

public function testOnFailWithinAnonFunc() {
    function () anonFunction =
        function () {
            int i = 0;
            string str = "";

            while (i < 2) {
                do {
                    str += " -> Before error thrown, ";
                    fail getError();
                } on fail error e {
                    str += " -> Error caught at level #1";
                }

                i = i + 1;
            } on fail error e {
                str += " -> Error caught at levet #2";
            }

        assertEquality(" -> Before error thrown,  -> Error caught at level #1 -> Before error thrown,  -> Error caught at level #1", str);
    };

    anonFunction();
}

class TestClass {
    function runOnFailClause() returns string {
        int i = 0;
        string str = "";

        while (i < 2) {
            do {
                str += " -> Before error thrown, ";
                fail getError();
            } on fail error e {
                str += " -> Error caught at level #1";
            }

            i = i + 1;
        } on fail error e {
            str += " -> Error caught at levet #2";
        }
        return str;
    }
}

function testRetryOnFailWithinObjectFunc() {
    TestClass testClass = new ();
    string str = testClass.runOnFailClause();
    assertEquality(" -> Before error thrown,  -> Error caught at level #1 -> Before error thrown,  -> Error caught at level #1", str);
}

function testFailExprWithinOnFail() {
    int i = 0;
    string str = "";
    while (i < 2) {
        do {
            str += " -> Before error thrown, ";
            i = i + 1;
            fail getError();
        } on fail error e {
            str += " -> Error caught at level #1";
            fail getError();
        }
    } on fail error e {
        str += " -> Error caught at level #2";
    }

    assertEquality(" -> Before error thrown,  -> Error caught at level #1 -> Error caught at level #2", str);
}

function testCheckExprWithinOnFail() {
    int i = 0;
    string str = "";
    while (i < 2) {
        do {
            str += " -> Before error thrown, ";
            i = i + 1;
            fail getError();
        } on fail error e {
            str += " -> Error caught at level #1";
            int val = check getCheckError();
        }
    } on fail error e {
        str += " -> Error caught at level #2";
    }

    assertEquality(" -> Before error thrown,  -> Error caught at level #1 -> Error caught at level #2", str);
}

function testFailPassWithinOnFail() {
    int i = 0;
    string str = "";
    do {
        do {
            str += "-> Before error thrown";
            i = i + 1;
            int ign = check getCheckError();
            str += " -> After error thrown, ";
        } on fail error e {
            str += " -> Error caught at level #1";
            int ii = check getCheckInt();
            str += " -> After level #1 check";
        }
        str += " -> After level #1 on-fail";
    } on fail error e {
        str += " -> Error caught at level #2";
    }
    str += " -> After handling all failures";

    assertEquality("-> Before error thrown -> Error caught at level #1 -> After level #1 check"
    + " -> After level #1 on-fail -> After handling all failures", str);
}

function testTypeNarrowingInsideOnfail() {
    string str = "";
    do {
        do {
            str += "-> Before error thrown";
            int parsedStr = check getCheckError();
        } on fail error e1 {
            str += " -> Error caught at level #1. Retrying...";
            var res = getCheckError();
            if (res is int) {
                str += " -> Should not reach here";
            } else {
                str += " -> Retry failed";
                fail res;
            }
        }
    } on fail error e {
        str += " -> Error caught at level #2";
    }
    str += " -> After handling all failures";

    assertEquality("-> Before error thrown -> Error caught at level #1. Retrying... -> Retry failed"
        + " -> Error caught at level #2 -> After handling all failures", str);
}

function testBreakWithinOnfail() {
    string str = "";
    do {
        str += "-> Before error thrown";
        fail getError();
    } on fail error e {
        str += " -> Error caught! ";
        int[] arr = [1, 2, 3];
        foreach int digit in arr {
            if (digit > 1) {
                break;
            }
            str += "Loop broke with digit: " + digit.toString();
        }
    }

    assertEquality("-> Before error thrown -> Error caught! Loop broke with digit: 1", str);
}

function testReturnWithinOnfail() returns string {
    int i = 0;
    string str = "";
    while (i < 2) {
        do {
            str += " -> Before error thrown, ";
            fail getError();
        } on fail error e {
            str += " -> Error caught at level #1";
            fail getError();
        }
        i = i + 1;
    } on fail error e {
        str += " -> Error caught at levet #2";
        return str;
    }
    return "should not reach here";
}

function testIntReturnWithinOnfail() returns int {
    int i = 1;
    while (i < 2) {
        do {
            fail getError();
        } on fail error e {
            fail getError();
        }
        i = i + 1;
    } on fail error e {
        return i;
    }
    return 0;
}

function testBreakWithinOnfailForOuterLoop() {
    string str = "";
    foreach int digit in 1 ... 5 {
        do {
            fail getError();
        } on fail error e {
            if (digit < 4) {
                str += "Loop continued with digit: " + digit.toString() + " ->";
                continue;
            } else {
                str += "Loop broke with digit: " + digit.toString();
                break;
            }
        }
    }
    assertEquality("Loop continued with digit: 1 ->Loop continued with digit: 2 ->Loop continued with digit: 3 " +
    "->Loop broke with digit: 4", str);
}

function testLambdaFunctionWithOnFail() returns int {
    var lambdaFunc = function () returns int {
          int a = 10;
          int b = 11;
          int c = 0;
          do {
              error err = error("custom error", message = "error value");
              c = a + b;
              fail err;
          } on fail error e {
              function (int, int) returns int arrow = (x, y) => x + y + a + b + c;
              a = arrow(1, 1);
          }
          return a;
    };
    return lambdaFunc();
}

function testSimpleOnFailWithoutVariable() {
    string str = "";
    do {
        error err = error("Custom error thrown explicitly.");
        fail err;
    } on fail {
        str += "On Fail Executed";
    }
     assertEquality("On Fail Executed", str);
}

function getCheckError()  returns int|error {
    error err = error("Custom Error");
    return err;
}

function getCheckInt()  returns int|error {
    return 0;
}

function trxError()  returns error {
    return error("TransactionError");
}

function getError() returns error {
    error err = error("Custom Error");
    return err;
}

public class Listener {

    public isolated function 'start() returns error? {
        return externStart(self);
    }
    public isolated function gracefulStop() returns error? {
    }
    public isolated function immediateStop() returns error? {
    }
    public isolated function detach(service object {} s) returns error? {
    }
    public isolated function attach(service object {} s, string[]|string? name = ()) returns error? {
        return self.register(s, name);
    }
    isolated function register(service object {} s, string[]|string? name) returns error? {
        return externAttach(s, name);
    }

    public function init() returns error? {
        check externLInit(self);
    }
}

type S service object {
};

service S / on new Listener() {
    resource isolated function get foo() returns service object {} {
        return service object {
            resource function get foo() returns string {
                do {
                    _ = check getCheckError();
                    return "should not reach here";
                } on fail error e {
                    return "Error thrown from on-fail: " + e.message();
                }
            }
        };
    }

    resource isolated function get bar() returns service object {} {
        return service object {
            resource function get bar() returns string {
                do {
                    do {
                        _ = check getCheckError();
                    } on fail error e1 {
                        fail e1;
                    }
                } on fail error e2 {
                    return "Error thrown from on-fail: " + e2.message();
                }
                return "should not reach here";
            }
        };
    }
}

function testNoPossibleFailureWithOnFail() {
    assertEquality(returnStringWithoutFailure(), "packages");
    assertEquality(returnIntWithoutFailure(true), 1);
    assertEquality(returnIntWithoutFailure(false), 2);
}

function returnStringWithoutFailure() returns string {
    do {
        return "packages";
    } on fail var e {
        return e.toString();
    }
}

function returnIntWithoutFailure(boolean val) returns int {
    if (val) {
        do {
            return 1;
        } on fail {
        }
    }
    return 2;
}

function getService() returns object {} = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "getService"
} external;

function reset() = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "reset"
} external;

public function callMethod(service object {} s, string name) returns future<any|error> = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "callMethod"
} external;

isolated function externLInit(object {} o) returns error? = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "listenerInit"
} external;

isolated function externAttach(service object {} s, string[]|string? name) returns error? = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "attach"
} external;

isolated function externStart(object {} o) returns error? = @java:Method {
    'class: "org/ballerinalang/nativeimpl/jvm/servicetests/ServiceValue",
    name: "start"
} external;

function testOnFailWithinInLineServiceObj() {
    service object {} serviceObj1 = <service object {}>
            (checkpanic (wait callMethod(<service object {}>getService(), "$get$foo")));
    string str1 = <string>(checkpanic (wait callMethod(serviceObj1, "$get$foo")));
    assertEquality(str1, "Error thrown from on-fail: Custom Error");

    service object {} serviceObj2 = <service object {}>
                (checkpanic (wait callMethod(<service object {}>getService(), "$get$bar")));
    string str2 = <string>(checkpanic (wait callMethod(serviceObj2, "$get$bar")));
    assertEquality(str2, "Error thrown from on-fail: Custom Error");

    reset();
}

function testOnFailInAnonFunctionExpr() returns error? {
    stream<string, error?> strStream = ["a1", "a2"].toStream();
    error? result1 = ();
    check strStream.forEach(function(string str) {
        do {
            int _ = check int:fromString(str);
        } on fail error e {
            //loop continues as the error is handled. result should contain the error for second element
            result1 = e;
        }
    });
    assertTrue(result1 is error);
    assertEquality((<error>result1).detail().get("message"), "'string' value 'a2' cannot be converted to 'int'");

    string[] strArray = ["a1", "a2"];
    error? result2 = ();
    strArray.forEach(function(string str) {
        do {
            do {
                int _ = check int:fromString(str);
            } on fail error e1 {
                fail e1;
            }
        } on fail error e2 {
            result2 = e2;
        }
    });
    assertEquality((<error>result2).detail().get("message"), "'string' value 'a2' cannot be converted to 'int'");
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
    do {
        fail error("error!");
    } on fail error error(msg) {
        testMessage = msg;
    }
     assertEquality(testMessage, "error!");
}

function testSimpleOnFailWithErrorBPWithVar() {
    do {
        fail error("error!");
    } on fail var error(msg) {
        testMessage = msg;
    }
     assertEquality(testMessage, "error!");
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithError() {
    do {
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
    do {
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
    do {
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
    do {
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
    do {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail error<record {int code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingUserDefinedTypeWithErrDetail3() {
    do {
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
    do {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail error<record {int|string code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 20);
    assertEquality(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingAnonDetailRecord() {
    do {
        error<record {int code;}> errVar = error("error", code = 34);
        fail errVar;
    } on fail error<record {int code;}> error(code = errCode) {
        testErrorCode = errCode;
    }
    assertEquality(testErrorCode, 34);
    assertEquality(testErrorCode is int, true);
}

function testOnFailWithErrorBPHavingAnonDetailRecordWithVar() {
    do {
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
    do {
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
    do {
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
    do {
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
    do {
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
    do {
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
    do {
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

int testErrorCodeNested = 0;
string testMessageNested = "";

function testNestedOnFailWithErrorBP() {
    string testErrorReasonNested = "";
    do {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail var error(msg1, code = errCode1, reason = errReason1) {
        if errCode1 == 20 {
            do {
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
    do {
        fail error SampleError("error!", code = 20, reason = "deadlock condition");
    } on fail var error(msg1, code = errCode1, reason = errReason1) {
        if errCode1 == 20 {
            do {
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
    int i = 2;
    string str = "";

    while i <= 2 {
        do {
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

    while i <= 2 {
        do {
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

type Error distinct error;

function testOnFailWithCheckpanicOfDifferentErrorInDoClause() returns error? {
    [int, Error]? res = functionWithCheckpanicInDoClause(1);
    assertTrue(res !is ());
    [int, Error] [a, b] = checkpanic res.ensureType();
    assertEquality(1, a);
    assertEquality("Error", b.message());

    [int, Error]|error? errRes = trap functionWithCheckpanicInDoClause(2);
    assertTrue(errRes is error && errRes !is Error);
    error c = <error> errRes;
    assertEquality("error!", c.message());
}

function functionWithCheckpanicInDoClause(1|2 val) returns [int, Error]? {
    do {
        _ = checkpanic fn(val);
        fail error Error("Error");
    } on fail Error e {
        return [val, e];
    }
    return ();
}

function fn(1|2 val) returns int|error => val == 1 ? 1 : error("error!");
//-------------------------------------------------------------------------------

type AssertionError error;

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
}

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
