// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public type TrxError record {|
    string message;
    error? cause;
    string data;
|};

function testTransactionFailing() returns string|error {
    return trap testTransactionStmtWithCommitedAndAbortedBlocks(2, false);
}

function testTransactionStmtWithCommitedAndAbortedBlocks(int failureCutOff, boolean requestAbort) returns (string) {
    string a = "";
    a = a + "start";
    a = a + " fc-" + failureCutOff.toString();
    int count = 0;
    transaction with retries=2 {
        a = a + " inTrx";
        count = count + 1;
        if (count <= failureCutOff) {
            a = a + " blowUp";
            int bV = blowUp();
        }

        if (requestAbort) {
            a = a + " aborting";
            abort;
        }
        a = a + " endTrx";
    } onretry {
        a = a + " retry";
    } committed {
        a = a + " committed";
    } aborted {
        a = a + " aborted";
    }
    a = (a + " end");
    return a;
}

function blowUp()  returns int {
    if (5 == 5) {
        error err = error("TransactionError");
        panic err;
    }
    return 5;
}

function runtimeNestedTransactions(boolean trapError) returns string {
    transaction {
        if (trapError) {
            var res = trap functionWithATransactionStmt();
            if (res is string) {
                ss += res;
            } else {
                ss += " trapped[err: " + <string>res.detail()["message"] + "]";
            }
        } else {
            var res = functionWithATransactionStmt();
            ss += res;
        }
    } onretry {
        ss += " retry";
    } committed {
        ss += " outer-committed";
    } aborted {
        ss += " outer-aborted";
    }
    ss += " endTrx";
    return ss;
}

function functionWithATransactionStmt() returns string {
    ss = ss + " in func";
    transaction {
        ss += " in-trx";
    } onretry {
        ss += " retry-func-trx";
    } committed {
        ss += " committed-fun-trx";
    }
    ss += " after-fun-trx";
    return ss;
}

string ss = "";
function runtimeNestedTransactionErrorTraped() returns string {
    ss = "";
    ss += runtimeNestedTransactions(true);
    return ss;
}

function runtimeNestedTransactionsError() returns string {
    ss = "";
    var res = trap runtimeNestedTransactions(false);
    if (res is string) {
        ss += res;
    } else {
        ss += " [err: " + <string>res.detail()["message"] + "]";
    }
    return ss;
}

function testAbortStatementWithNoAbortBlock() returns (string) {
    string a = "";
    a = a + "start";
    int count = 0;
    transaction with retries=2 {
        a += " in-trx";
        abort;
    } onretry {
        a += " retry";
    } committed {
        a += " committed";
    }
    a += " end";
    return a;
}

function testAbortStatementWithAbortBlock() returns (string) {
    string a = "";
    a = a + "start";
    int count = 0;
    transaction with retries=2 {
        a += " in-trx";
        abort;
    } onretry {
        a += " retry";
    } committed {
        a += " committed";
    } aborted {
        a += " aborted";
    }
    a += " end";
    return a;
}

function testTrxSuccessWithAbortBlock() returns (string) {
    string a = "";
    a = a + "start";
    int count = 0;
    transaction with retries=2 {
        a += " in-trx";
    } onretry {
        a += " retry";
    } committed {
        a += " committed";
    } aborted {
        a += " aborted";
    }
    a += " end";
    return a;
}

function testAbortedCommittedBlockMixedUpCommitted() returns (string) {
    string a = "";
    a = a + "start";
    int count = 0;
    transaction with retries=2 {
        a += " in-trx";
    } onretry {
        a += " retry";
    } aborted {
        a += " aborted";
    } committed {
        a += " committed";
    }
    a += " end";
    return a;
}

function testAbortedCommittedBlockMixedUpAborted() returns (string) {
    string a = "";
    a = a + "start";
    int count = 0;
    transaction with retries=2 {
        a += " in-trx";
        abort;
    } onretry {
        a += " retry";
    } aborted {
        a += " aborted";
    } committed {
        a += " committed";
    }
    a += " end";
    return a;
}

function testAbortedCommittedBlockMixedUpNoRetryBlockAborted() returns (string) {
    string a = "";
    a = a + "start";
    int count = 0;
    transaction with retries=2 {
        a += " in-trx";
        abort;
    } aborted {
        a += " aborted";
    } committed {
        a += " committed";
    }
    a += " end";
    return a;
}

function testAbortedCommittedBlockMixedUpNoRetryBlockCommitted() returns (string) {
    string a = "";
    a = a + "start";
    int count = 0;
    transaction with retries=2 {
        a += " in-trx";
    } aborted {
        a += " aborted";
    } committed {
        a += " committed";
    }
    a += " end";
    return a;
}


function multipleTrxSequence(boolean abort1, boolean abort2, boolean fail1, boolean fail2) returns string {
    string a = "start";
    int count = 0;
    boolean failed1 = false;
    boolean failed2 = false;

    transaction with retries=2 {
        a += " in-trx-1";
        if (abort1) {
            abort;
        }
        if (fail1 && !failed1) {
            failed1 = true;
            error err = error("TransactionError");
            panic err;
        }
    } onretry {
        a += " retry-1";
    } committed {
        a += " committed-1";
    } aborted {
        a += " aborted-1";
    }

    a += " end-1";

    transaction with retries=2 {
        a += " in-trx-2";
        if (abort2) {
            abort;
        }
        if (fail2 && !failed2) {
            failed2 = true;
            error err = error("TransactionError");
            panic err;
        }
    } onretry {
        a += " retry-2";
    } committed {
        a += " committed-2";
    } aborted {
        a += " aborted-2";
    }
    a += " end-2";
    return a;
}

public function testTransactionInsideIfStmt() returns int {
    int a = 10;
    if (a == 10) {
        int c = 8;
        transaction with retries = 0 {
                int b = a + c;
                a = b;
         }
    }
    return a;
}

public function testArrowFunctionInsideTransaction() returns int {
    int a = 10;
    if (a == 10) {
        int b = 11;
        transaction with retries = 0 {
            int c = a + b;
            function (int, int) returns int arrow = (x, y) => x + y + a + b + c;
            a = arrow(1, 1);
        }
    }
    return a;
}
