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

import ballerina/io;
public type TrxError record {
    string message;
    error? cause;
    string data;
    !...;
};

map<string> logMap = {"log":""};

function testTransactionFailing() returns string|error {
    return trap testTransactionStmtWithCommitedAndAbortedBlocks(2, false);
}

function testTransactionStmtWithCommitedAndAbortedBlocks(int failureCutOff, boolean requestAbort) returns (string) {
    logMap["log"] = "";
    logMap["log"] = <string>logMap["log"] + "start";
    logMap["log"] = <string>logMap["log"] + " fc-" + failureCutOff;
    map<int> count = {"count":0};
    transaction with retries=2 {
        logMap["log"] = <string>logMap["log"]+ " inTrx";
        count["count"] = <int>count["count"] + 1;
        if (<int>count["count"] <= failureCutOff) {
            logMap["log"] = <string>logMap["log"] + " blowUp";
            int bV = blowUp();
        }

        if (requestAbort) {
            logMap["log"] = <string>logMap["log"] + " aborting";
            abort;
        }
        logMap["log"] = <string>logMap["log"] + " endTrx";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " retry";
    } committed {
        logMap["log"] = <string>logMap["log"] + " committed";
    } aborted {
        logMap["log"] = <string>logMap["log"] + " aborted";
    }
   logMap["log"] = (<string>logMap["log"] + " end");
    return <string>logMap["log"];
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
                logMap["log"] = <string>logMap["log"] + res;
            } else {
                logMap["log"] = <string>logMap["log"] + " trapped[err: " + <string>res.detail().message + "]";
            }
        } else {
            var res = functionWithATransactionStmt();
            logMap["log"] = <string>logMap["log"] + res;
        }
    } onretry {
        logMap["log"] = <string>logMap["log"] + " retry";
    } committed {
        logMap["log"] = <string>logMap["log"] + " outer-committed";
    } aborted {
        logMap["log"] = <string>logMap["log"] + " outer-aborted";
    }
    logMap["log"] = <string>logMap["log"] + " endTrx";
    return <string>logMap["log"] ;
}

function functionWithATransactionStmt() returns string {
    logMap["log"] = <string>logMap["log"] + " in func";
    transaction {
        logMap["log"] = <string>logMap["log"] + " in-trx";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " retry-func-trx";
    } committed {
        logMap["log"] = <string>logMap["log"] + " committed-fun-trx";
    }
    logMap["log"] = <string>logMap["log"] + " after-fun-trx";
    return  <string>logMap["log"];
}


function runtimeNestedTransactionErrorTraped() returns string {
    logMap["log"] = "";
    logMap["log"] = <string>logMap["log"] + runtimeNestedTransactions(true);
    return <string>logMap["log"];
}

function runtimeNestedTransactionsError() returns string {
    logMap["log"] = "";
    var res = trap runtimeNestedTransactions(false);
    if (res is string) {
        logMap["log"] = <string>logMap["log"] + res;
    } else {
        logMap["log"] = <string>logMap["log"] + " [err: " + <string>res.detail().message + "]";
    }
    return <string>logMap["log"];
}

function testAbortStatementWithNoAbortBlock() returns (string) {
    logMap["log"] = "";
    logMap["log"] = <string>logMap["log"] + "start";
    int count = 0;
    transaction with retries=2 {
        logMap["log"] = <string>logMap["log"] + " in-trx";
        abort;
    } onretry {
       logMap["log"] = <string>logMap["log"] + " retry";
    } committed {
        logMap["log"] = <string>logMap["log"] + " committed";
    }
    logMap["log"] = <string>logMap["log"] + " end";
    return <string>logMap["log"];
}

function testAbortStatementWithAbortBlock() returns (string) {
    logMap["log"] = "";
    logMap["log"] = <string>logMap["log"] + "start";
    int count = 0;
    transaction with retries=2 {
        logMap["log"] = <string>logMap["log"] + " in-trx";
        abort;
    } onretry {
        logMap["log"] = <string>logMap["log"] + " retry";
    } committed {
        logMap["log"] = <string>logMap["log"] +  " committed";
    } aborted {
        logMap["log"] = <string>logMap["log"] +  " aborted";
    }
    logMap["log"] = <string>logMap["log"] +  " end";
    return <string>logMap["log"];
}

function testTrxSuccessWithAbortBlock() returns (string) {
    logMap["log"] = "";
    logMap["log"] = <string>logMap["log"] +  "start";
    int count = 0;
    transaction with retries=2 {
        logMap["log"] = <string>logMap["log"] + " in-trx";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " retry";
    } committed {
        logMap["log"] = <string>logMap["log"] + " committed";
    } aborted {
        logMap["log"] = <string>logMap["log"] + " aborted";
    }
    logMap["log"] = <string>logMap["log"] + " end";
    return <string>logMap["log"];
}

function testAbortedCommittedBlockMixedUpCommitted() returns (string) {
    logMap["log"] = "";
    logMap["log"] = <string>logMap["log"] + "start";
    int count = 0;
    transaction with retries=2 {
        logMap["log"] = <string>logMap["log"] + " in-trx";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " retry";
    } aborted {
        logMap["log"] = <string>logMap["log"] + " aborted";
    } committed {
        logMap["log"] = <string>logMap["log"] + " committed";
    }
    logMap["log"] = <string>logMap["log"] + " end";
    return <string>logMap["log"];
}

function testAbortedCommittedBlockMixedUpAborted() returns (string) {
    logMap["log"] = "";
    logMap["log"] = <string>logMap["log"] + "start";
    int count = 0;
    transaction with retries=2 {
        logMap["log"] = <string>logMap["log"] + " in-trx";
        abort;
    } onretry {
        logMap["log"] = <string>logMap["log"] + " retry";
    } aborted {
        logMap["log"] = <string>logMap["log"] + " aborted";
    } committed {
        logMap["log"] = <string>logMap["log"] + " committed";
    }
    logMap["log"] = <string>logMap["log"] + " end";
    return <string>logMap["log"];
}

function testAbortedCommittedBlockMixedUpNoRetryBlockAborted() returns (string) {
    logMap["log"] = "";
    logMap["log"] = <string>logMap["log"] + "start";
    int count = 0;
    transaction with retries=2 {
        logMap["log"] = <string>logMap["log"] + " in-trx";
        abort;
    } aborted {
        logMap["log"] = <string>logMap["log"] + " aborted";
    } committed {
        logMap["log"] = <string>logMap["log"] + " committed";
    }
    logMap["log"] = <string>logMap["log"] + " end";
    return <string>logMap["log"];
}

function testAbortedCommittedBlockMixedUpNoRetryBlockCommitted() returns (string) {
    logMap["log"] = "start";
    int count = 0;
    transaction with retries=2 {
        logMap["log"] = <string>logMap["log"] + " in-trx";
    } aborted {
        logMap["log"] = <string>logMap["log"] + " aborted";
    } committed {
        logMap["log"] = <string>logMap["log"] + " committed";
    }
    logMap["log"] = <string>logMap["log"] + " end";
    return <string>logMap["log"];
}


function multipleTrxSequence(boolean abort1, boolean abort2, boolean fail1, boolean fail2) returns string {
    logMap["log"] = "start";
    int count = 0;
    map<boolean> failed1 = {"fail":false};
    map<boolean> failed2 = {"fail":false};

    transaction with retries=2 {
        logMap["log"] = <string>logMap["log"] + " in-trx-1";
        if (abort1) {
            abort;
        }
        if (fail1 && !<boolean>failed1["fail"]) {
            failed1["fail"] = true;
            error err = error("TransactionError");
            panic err;
        }
    } onretry {
        logMap["log"] = <string>logMap["log"] + " retry-1";
    } committed {
        logMap["log"] = <string>logMap["log"] + " committed-1";
    } aborted {
        logMap["log"] = <string>logMap["log"] + " aborted-1";
    }

    logMap["log"] = <string>logMap["log"] + " end-1";

    transaction with retries=2 {
        logMap["log"] = <string>logMap["log"] + " in-trx-2";
        if (abort2) {
            abort;
        }
        if (fail2 && !<boolean>failed2["fail"]) {
            failed2["fail"] = true;
            error err = error("TransactionError");
            panic err;
        }
    } onretry {
        logMap["log"] = <string>logMap["log"] + " retry-2";
    } committed {
        logMap["log"] = <string>logMap["log"] + " committed-2";
    } aborted {
        logMap["log"] = <string>logMap["log"] + " aborted-2";
    }

    logMap["log"] = <string>logMap["log"] + " end-2";
    return <string>logMap["log"];
}