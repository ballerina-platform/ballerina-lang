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
import ballerina/transactions;
import ballerina/runtime;

string S = "";
boolean functionCommited = false;

@transactions:Participant {
    oncommit:commitFunc,
    onabort:abortFunc
}
public function participantFoo() {
    S = S + " in-participantFoo";
    io:println("Hello, World!");
}

public function commitFunc(string trxId) {
    S = S + " commitFun";
    functionCommited = true;
    io:println("commitFunc");
}

public function abortFunc(string trxId) {
    S = S + " abortFunc";
    io:println("abortFunc");
}


@transactions:Participant {
}
public function erroredFunc() {
    S = S + " in-participantErroredFunc";
    int k = 5;
    if (k == 5) {
        io:println("throw!!");
        error err = error("TransactionError");
        panic err;
    }
}

boolean thrown1 = false;
boolean thrown2 = false;

function initiatorFunc(boolean error1, boolean error2) returns string {
    thrown1 = false;
    thrown2 = false;
    S = "";
    transaction with retries=2 {
        S = S + " in-trx-block";
        participantFoo();

        if (thrown1 && !thrown2 && error2) {
            thrown2 = true;
            var er = trap erroredFunc();
            if (er is error) {
                S = S + " " + er.reason();
            }
        }
        if (!thrown1 && error1) {
            thrown1 = true;
            var er = trap erroredFunc();
            if (er is error) {
                S = S + " " + er.reason();
            }
        }

        S = S + " in-trx-lastline";
    } onretry {
        S = S + " onretry-block";
    } committed {
        S = S + " committed-block";
    } aborted {
        S = S + " aborted-block";
    }
    S = S + " after-trx";
    return S;
}

function blowUp()  returns int {
    if (5 == 5) {
        error err = error("TransactionError");
        panic err;
    }
    return 5;
}


function initiatorWithLocalNonParticipantError() returns string {
    string s = "";
    transaction {
        s += " in-trx";
        var t = trap nonParticipantNestedTrxStmt(s);
        if (t is string) {
            s += t;
        } else {
            s += " trapped:[" + <string>t.detail()["message"] + "]";
        }
        s += " last-line";
    } onretry {
        s += " onretry";
    } committed {
        s += " committed";
    } aborted {
        s += " aborted";
    }
    return s;
}

function nonParticipantNestedTrxStmt(string s) returns string {
    string q = s;
    transaction {
        q += " in-local-nonparticipant-trx";
    }
    return q;
}

function nonParticipantFunctionNesting(string failureCondition) returns string {
    string s = "";
    boolean isAborted = false;
    S = "";
    functionCommited = false;
    transaction {
        s = " in-trx";
        s = nonParticipant(failureCondition, s);
        s += " in-trx-last-line";
    } onretry {
        s += " onretry";
    } committed {
        s += " committed";
    } aborted {
        isAborted = true;
        s += " aborted";
    }
    if (!isAborted) {
        boolean waitResult = waitForCondition(5000, 20, function () returns boolean { return functionCommited; });
        if (!waitResult) {
              error err = error("Participants failed to commit");
              panic err;
        }
    }
    return s + " |" + S;
}

function nonParticipant(string failureCondition, string s) returns string {
    string p = s + " in-non-participant";
    var q = trap localParticipant(failureCondition, p);
    if (q is string) {
        p = q;
    } else {
        p += " traped: local-participant";
    }
    p += " after-local-participant";
    var p2 = trap failable(failureCondition, p);
    if (p2 is error) {
        p += " non-participants-callee-fail-and-trapped";
    } else {
        p = p2;
    }
    return p;
}

function participantInNonStrand() returns string {
    string s = "";
    S = "";
    transaction {
        s += " in-trx";
        var t = trap startANewStrand(s);
        if (t is string) {
            s += t;
        } else {
            s += " trapped:[" + t.reason() + "]";
        }
        s += " last-line";
    } onretry {
        s += " onretry";
    } committed {
        s += " committed";
    } aborted {
        s += " aborted";
    }

    if (S != "") {
        s += " | " + S;
    }
    return s;
}

function startANewStrand(string s) returns string {
    io:println("starting onSomeOtherStrand on a new strand");
    var f = start onSomeOtherStrand(s);
    wait f;
    return " from-startANewStrand";
}

function onSomeOtherStrand(string s) {
    io:println("in some other strand");
    var r = trap otherStrand(s);
    if (r is error) {
        S += "error in otherStrand: " + r.reason();
        io:println("trapped error from otherStrand()");
    }
}

@transactions:Participant {
    oncommit:commitFunc,
    onabort:abortFunc
}
public function otherStrand(string s) {
    io:println("Hello, World!");
    error err = error("error!!!");
    panic err;
}



@transactions:Participant {
    oncommit:commitFunc,
    onabort:abortFunc
}
public function localParticipant(string failureCondition, string s) returns string {
    if (failureCondition == "participantFail") {
        error er = error("failed");
        panic er;
    }
    return s + " localParticipant";
}

function failable(string failureCondition, string s) returns string {
    if (failureCondition == "failInNonParticipant") {
        error er = error("failed");
        panic er;
    }
    return s;
}

function waitForCondition(int maxWaitInMillySeconds, int noOfRounds, function() returns boolean conditionFunc)
             returns boolean {
    int sleepTimePerEachRound = maxWaitInMillySeconds/noOfRounds;
    int count = 0;
    while (count < noOfRounds) {
        if (conditionFunc()){
            return true;
        }
        count = count + 1;
        runtime:sleep(sleepTimePerEachRound);
    }
    return false;
}
