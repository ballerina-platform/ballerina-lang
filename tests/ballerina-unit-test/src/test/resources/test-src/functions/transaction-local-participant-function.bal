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

map<string> S = {"log":""};

@transactions:Participant {
    oncommit:commitFunc,
    onabort:abortFunc
}
public function participantFoo() {
    S["log"] = <string>S["log"] + " in-participantFoo";
    io:println("Hello, World!");
}

public function commitFunc(string trxId) {
    S["log"] = <string>S["log"] + " commitFun";
    io:println("[][[][][[][[][[]" + trxId);
    io:println("commitFunc" + trxId);
}

public function abortFunc(string trxId) {
    S["log"] = <string>S["log"] + " abortFunc";
    io:println("abortFunc"+trxId);
}


@transactions:Participant {
}
public function erroredFunc() {
    io:println("error: **************************[][][][][]**********************************");
    S["log"] = <string>S["log"] + " in-participantErroredFunc";
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

    int b1= 2;
    transaction with retries=2 {
       
        S["log"] = <string>S["log"] + " in-trx-block";
        participantFoo();

        if (thrown1 && !thrown2 && error2) {
            thrown2 = true;
            var er = trap erroredFunc();
            if (er is error) {
                S["log"] = <string>S["log"] + " " + er.reason();
            }
        }
        if (!thrown1 && error1) {
            thrown1 = true;
            var er = trap erroredFunc();
            if (er is error) {
                S["log"] = <string>S["log"] + " " + er.reason();
            }
        }

        S["log"] = <string>S["log"] + " in-trx-lastline";
    } onretry {
        S["log"] = <string>S["log"] + " onretry-block";
    } committed {
        S["log"] = <string>S["log"] + " committed-block";
    } aborted {
        S["log"] = <string>S["log"] + " aborted-block";
    }
    int a21  =10;
    S["log"] = <string>S["log"] + " after-trx";
    return <string>S["log"];
}

function blowUp()  returns int {
    if (5 == 5) {
        error err = error("TransactionError");
        panic err;
    }
    return 5;
}


function initiatorWithLocalNonParticipantError() returns string {
    map<string> s = {"log":""};
    transaction {
        s["log"] = <string>s["log"] + " in-trx";
        var t = trap nonParticipantNestedTrxStmt(<string>s["log"]);
        if (t is string) {
            s["log"] = <string>s["log"] + t;
            s["log"] = <string>s["log"] + t;
        } else {
            s["log"] = <string>s["log"] + " trapped:[" + <string>t.detail().message + "]";
        }
        s["log"] = <string>s["log"] + " last-line";
    } onretry {
        s["log"] = <string>s["log"] + " onretry";
    } committed {
        s["log"] = <string>s["log"] + " committed";
    } aborted {
        s["log"] = <string>s["log"] + " aborted";
    }
    return <string>s["log"];
}

function nonParticipantNestedTrxStmt(string s) returns string {
    map<string> q = {"log":s};
    transaction {
        q["log"] = " in-local-nonparticipant-trx";
    }
    return <string>q["log"];
}

function nonParticipantFunctionNesting(string failureCondition) returns string {
    map<string> s = {"log":""};
    S["log"] = "";
    transaction {
        s["log"] = " in-trx";
        s["log"] = nonParticipant(failureCondition, <string>s["log"]);
        s["log"] = <string>s["log"] + " in-trx-last-line";
    } onretry {
        s["log"] = <string>s["log"] + " onretry";
    } committed {
        s["log"] = <string>s["log"] + " committed";
    } aborted {
        s["log"] = <string>s["log"] + " aborted";
    }
    return <string>s["log"] + " |" + <string>S["log"];
}

function nonParticipant(string failureCondition, string s) returns string {
    string p = s + " in-non-participant";
    io:println(p);
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
    io:println("****()()())()()()()*************" + p);
    return p;
}

map<string> S2 = {"log":""};

function participantInNonStrand() returns string {
    map<string> s = {"log":""};
    S2["log"] = "";
    transaction {
        s["log"] = <string>s["log"] + " in-trx";
        var t = trap startANewStrand(<string>s["log"]);
        if (t is string) {
            s["log"] = <string>s["log"] + t;
        } else {
            s["log"] = <string>s["log"] + " trapped:[" + t.reason() + "]";
        }
        s["log"] = <string>s["log"] + " last-line";
        
    } onretry {
        s["log"] = <string>s["log"] + " onretry";
    } committed {
        s["log"] = <string>s["log"] + " committed";
    } aborted {
        s["log"] = <string>s["log"] + " aborted";
    }
    io:println(<string>S["log"]);
    if (<string>S2["log"] != "") {
        s["log"] = <string>s["log"] + " | " + <string>S2["log"];
    }
    return <string>s["log"];
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
        S2["log"] = <string>S2["log"] + "error in otherStrand: " + r.reason();
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
    io:println("*********===============================******"+failureCondition);
    if (failureCondition == "participantFail") {
        error er = error("failed");
        panic er;
    }
    io:println("*********=================^^^^^^^==============******"+failureCondition);
    return s + " localParticipant";
}

function failable(string failureCondition, string s) returns string {
    if (failureCondition == "failInNonParticipant") {
        error er = error("failed");
        panic er;
    }
    return s;
}