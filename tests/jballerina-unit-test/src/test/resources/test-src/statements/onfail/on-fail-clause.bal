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

function testOnFailEdgeTestcases() {
    testUnreachableCodeWithIf();
    testMultiLevelOnFail();
    testRetryOnFailWithinWhile();
    testOnFailWithinAnonFunc();
    testRetryOnFailWithinObjectFunc();
    testFailExprWithinOnFail();
    testCheckExprWithinOnFail();
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

function getCheckError()  returns int|error {
    error err = error("Custom Error");
    return err;
}

function trxError()  returns error {
    return error("TransactionError");
}

function getError() returns error {
    error err = error("Custom Error");
    return err;
}

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

    panic AssertionError("AssertionError", message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
