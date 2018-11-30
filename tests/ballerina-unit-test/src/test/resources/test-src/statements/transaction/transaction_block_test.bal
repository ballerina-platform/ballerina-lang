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

public type TrxError record {
    string message;
    error? cause;
    string data;
    !...
};

function testTransactionFailing() returns string|error {
    return trap testTransactionStmtWithCommitedAndAbortedBlocks(2, false);
}

function testTransactionStmtWithCommitedAndAbortedBlocks(int failureCutOff, boolean requestAbort) returns (string) {
    string a = "";
    a = a + "start";
    a = a + " fc-" + failureCutOff;
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
                ss += " trapped[err: " + res.reason() + "]";
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
        ss += " [err: " + res.reason() + "]";
    }
    return ss;
}
