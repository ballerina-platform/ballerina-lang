// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/transactions;
import ballerina/runtime;

listener http:Listener initiatorEP00 = new(8888);

http:Client participant1EP00 = new("http://localhost:8889");

State0 state0 = new();

@http:ServiceConfig {
    basePath:"/"
}
service InitiatorService00 on initiatorEP00 {

    resource function getState(http:Caller ep, http:Request req) {
        http:Response res = new;
        res.setTextPayload(state0.toString());
        state0.reset();
        checkpanic ep->respond(res);
    }

    resource function testInitiatorAbort(http:Caller ep, http:Request req) {
        transaction {
            _ = checkpanic participant1EP00 -> get("/noOp");

            testInitiatorAbort_ParticipantTransaction();

            state0.abortedByInitiator = true;
            abort;
        } committed {
            log:printInfo("testInitiatorAbort.committed");
            state0.committedFunctionCalled = true;
        } aborted {
            log:printInfo("testInitiatorAbort.aborted");
            state0.abortedFunctionCalled = true;
        }
        checkpanic ep -> respond("response");
    }

    resource function testRemoteParticipantAbort(http:Caller ep, http:Request req) {
        transaction {
            var response = participant1EP00 -> get("/testRemoteParticipantAbort");
            if (response is http:Response) {
                log:printInfo("/testRemoteParticipantAbort response code: " + response.statusCode.toString());
            } else {
                log:printInfo("/testRemoteParticipantAbort errored");
            }

            testRemoteParticipantAbort_ParticipantTransaction();

            http:Response res = new;  res.statusCode = 200;
            checkpanic ep->respond(res);
        }
    }

    // local participant function can not abort with new syntax.
    //resource function testLocalParticipantAbort(http:Caller ep, http:Request req) {
    //
    //
    //    transaction {
    //        checkpanic participant1EP00 -> get("/noOp");
    //
    //        transaction { // local participant
    //            state0.abortedByLocalParticipant = true;
    //            abort;
    //        } committed {
    //            state0.localParticipantCommittedFunctionCalled = true;
    //        } aborted {
    //            state0.localParticipantAbortedFunctionCalled = true;
    //        }
    //    } committed {
    //        state0.committedFunctionCalled = true;
    //    } aborted {
    //        state0.abortedFunctionCalled = true;
    //    }
    //
    //
    //    http:Response res = new;  res.statusCode = 200;
    //    checkpanic ep->respond(res);
    //}

    resource function testLocalParticipantSuccess(http:Caller ep, http:Request req) {
        transaction {
            _ = checkpanic participant1EP00 -> get("/noOp");

            testLocalParticipantSuccess_ParticipantTransaction();
        } committed {
            log:printInfo("testLocalParticipantSuccess-committed block");
            state0.committedFunctionCalled = true;
        } aborted {
            log:printInfo("testLocalParticipantSuccess-aborted block");
            state0.abortedFunctionCalled = true;
        }
        if (state0.committedFunctionCalled) {
            boolean waitResult = waitForCondition(5000, 20,
                                function () returns boolean {return state0.localParticipantCommittedFunctionCalled;});
            if (!waitResult) {
                  error err = error("Participant failed to commit");
                  panic err;
            }
        }
        http:Response res = new;  res.statusCode = 200;
        checkpanic ep->respond(res);
    }

    @http:ResourceConfig {
        transactionInfectable: false
    }
    resource function testTransactionInfectableFalse (http:Caller ep, http:Request req) {

        http:Response res = new;  res.statusCode = 500;
        transaction {
            testLocalParticipantSuccess_ParticipantTransaction();
            var result = participant1EP00 -> get("/nonInfectable");
            if (result is http:Response) {
                res = result;
                if(result.statusCode == 500) {
                    state0.abortedByInitiator = true;
                    abort;
                }
            } else {
                res.statusCode = 500;
            }
        } committed {
            state0.committedFunctionCalled = true;
        } aborted {
            state0.abortedFunctionCalled = true;
        }
        checkpanic ep->respond(res);
    }


    @http:ResourceConfig {
        transactionInfectable: true
    }
    resource function testTransactionInfectableTrue (http:Caller ep, http:Request req) {

        transaction {
            _ = checkpanic participant1EP00 -> get("/infectable");
            testTransactionInfectableTrue_ParticipantTransaction();
        } committed {
            state0.committedFunctionCalled = true;
        } aborted {
            state0.abortedFunctionCalled = true;
        }

        http:Response res = new;  res.statusCode = 200;

        checkpanic ep->respond(res);
    }


    @http:ResourceConfig {
        path:"/"
    }
    resource function member (http:Caller conn, http:Request req) {

        transaction {
            var getResult = participant1EP00 -> get("/");
            log:printInfo("initiator.transaction.after./.call");
            if (getResult is error) {
                log:printInfo("initiator.transaction.after./.call.error");
                io:print("Initiator could not send get request to participant. Error:");
                sendErrorResponseToCaller(conn);
                abort;
            } else {
                var fwdResult = conn -> respond(getResult);
                if (fwdResult is error) {
                    io:print("Initiator could not forward response from participant 1 to originating client. Error:");
                    io:print(fwdResult.reason());
                } else {
                    io:print("");
                }
            }
            log:printInfo("initiator.transaction.lastLine");
        } onretry {
            io:println("Intiator failed");
        } committed {
            io:println("Intiator committed");
        } aborted {
            io:println("Intiator aborted");
        }
    }

    resource function testSaveToDatabaseSuccessfulInParticipant(http:Caller ep, http:Request req) {
        http:Response res = new;  res.statusCode = 500;
        transaction {
            var result = participant1EP00 -> get("/testSaveToDatabaseSuccessfulInParticipant");
            if (result is http:Response) {
                testSaveToDatabaseSuccessfulInParticipant_ParticipantTransaction();
                res = result;
                if(result.statusCode == 500) {
                    state0.abortedByInitiator = true;
                    abort;
                }
            } else {
                res.statusCode = 500;
            }
        } committed {
            state0.committedFunctionCalled = true;
        } aborted {
            state0.abortedFunctionCalled = true;
        }
        checkpanic ep->respond(res);
    }

    resource function testSaveToDatabaseFailedInParticipant(http:Caller ep, http:Request req) {
        http:Response res = new;  res.statusCode = 500;
        transaction {
            var result = participant1EP00 -> get("/testSaveToDatabaseFailedInParticipant");
            if(result is http:Response) {
                testSaveToDatabaseFailedInParticipant_ParticipantTransaction();
                res = result;
                if(result.statusCode == 500) {
                    state0.abortedByInitiator = true;
                    abort;
                }
            } else {
                res.statusCode = 500;
            }
        } committed {
            state0.committedFunctionCalled = true;
        } aborted {
            state0.abortedFunctionCalled = true;
        }
        checkpanic ep->respond(res);
    }
}


@transactions:Participant {
    oncommit:testInitiatorAbortParticipantTransaction_committed,
    onabort:testInitiatorAbortParticipantTransaction_aborted
}
function testInitiatorAbort_ParticipantTransaction() {
    log:printInfo("in testInitiatorAbortParticipantTransaction");
}

function testInitiatorAbort_ParticipantTransaction_committed(string tid) {
    log:printInfo("testInitiatorAbort_ParticipantTransaction_committed");
    state0.localParticipantCommittedFunctionCalled = true;
}

function testInitiatorAbort_ParticipantTransaction_aborted(string tid) {
    log:printInfo("testInitiatorAbort_ParticipantTransaction_aborted");
    state0.localParticipantAbortedFunctionCalled = true;
}


@transactions:Participant {
    oncommit:testLocalParticipantSuccess_committed,
    onabort:testLocalParticipantSuccess_aborted
}
function testLocalParticipantSuccess_ParticipantTransaction() {
    log:printInfo("in testLocalParticipantSuccess_ParticipantTransaction");
}

function testLocalParticipantSuccess_committed(string tid) {
    log:printInfo("testLocalParticipantSuccess_committed");
    state0.localParticipantCommittedFunctionCalled = true;
}

function testLocalParticipantSuccess_aborted(string tid) {
    log:printInfo("testLocalParticipantSuccess_aborted");
    state0.localParticipantAbortedFunctionCalled = true;
}

@transactions:Participant {
    oncommit:testTransactionInfectableFalse_committed,
    onabort:testTransactionInfectableFalse_aborted
}
function testTransactionInfectableFalse_ParticipantTransaction() {
    log:printInfo("in testTransactionInfectableFalse_ParticipantTransaction");
}

function testTransactionInfectableFalse_committed(string tid) {
    state0.localParticipantCommittedFunctionCalled = true;
}

function testTransactionInfectableFalse_aborted(string tid) {
    state0.localParticipantAbortedFunctionCalled = true;
}

@transactions:Participant {
    oncommit:testTransactionInfectableTrue_committed,
    onabort:testTransactionInfectableTrue_aborted
}
function testTransactionInfectableTrue_ParticipantTransaction() {
    log:printInfo("in testTransactionInfectableTrue_ParticipantTransaction");
}

function testTransactionInfectableTrue_committed(string tid) {
    state0.localParticipantCommittedFunctionCalled = true;
}

function testTransactionInfectableTrue_aborted(string tid) {
    state0.localParticipantAbortedFunctionCalled = true;
}

@transactions:Participant {
    oncommit:testSaveToDatabaseFailedInParticipant_committed,
    onabort:testSaveToDatabaseFailedInParticipant_aborted
}
function testSaveToDatabaseFailedInParticipant_ParticipantTransaction() {
    log:printInfo("in testSaveToDatabaseFailedInParticipant_ParticipantTransaction");
}

function testSaveToDatabaseFailedInParticipant_committed(string tid) {
    state0.localParticipantCommittedFunctionCalled = true;
}

function testSaveToDatabaseFailedInParticipant_aborted(string tid) {
    state0.localParticipantAbortedFunctionCalled = true;
}

@transactions:Participant {
    oncommit:testSaveToDatabaseSuccessfulInParticipant_committed,
    onabort:testSaveToDatabaseSuccessfulInParticipant_aborted
}
function testSaveToDatabaseSuccessfulInParticipant_ParticipantTransaction() {
    log:printInfo("in testSaveToDatabaseSuccessfulInParticipant_ParticipantTransaction");
}

function testSaveToDatabaseSuccessfulInParticipant_committed(string tid) {
    state0.localParticipantCommittedFunctionCalled = true;
}

function testSaveToDatabaseSuccessfulInParticipant_aborted(string tid) {
    state0.localParticipantAbortedFunctionCalled = true;
}

@transactions:Participant {
    oncommit:testInitiatorAbortParticipantTransaction_committed,
    onabort:testInitiatorAbortParticipantTransaction_aborted
}
function testRemoteParticipantAbort_ParticipantTransaction() {
    log:printInfo("in testRemoteParticipantAbort_ParticipantTransaction");
}

function testInitiatorAbortParticipantTransaction_committed(string tid) {
    log:printInfo("state0.committedFunctionCalled = true");
    state0.committedFunctionCalled = true;
}

function testInitiatorAbortParticipantTransaction_aborted(string tid) {
    log:printInfo("state0.abortedFunctionCalled = true");
    state0.abortedFunctionCalled = true;
}

function sendErrorResponseToCaller(http:Caller conn) {
    http:Caller conn2 = conn;
    http:Response errRes = new; errRes.statusCode = 500;
    var respondResult = conn2 -> respond(errRes);
    if (respondResult is error) {
        io:print("Initiator could not send error response to originating client. Error:");
        io:println(respondResult.reason());
    } else {
        return;
    }
}

type State0 object {

    boolean abortedByInitiator = false;
    boolean abortedByLocalParticipant = false;
    boolean abortedFunctionCalled = false;
    boolean committedFunctionCalled = false;
    boolean localParticipantAbortedFunctionCalled = false;
    boolean localParticipantCommittedFunctionCalled = false;


    function reset() {
        self.abortedByInitiator = false;
        self.abortedByLocalParticipant = false;
        self.abortedFunctionCalled = false;
        self.committedFunctionCalled = false;
        self.localParticipantAbortedFunctionCalled = false;
        self.localParticipantCommittedFunctionCalled = false;
    }

    function toString() returns string {
        return io:sprintf("abortedByInitiator=%b,abortedByLocalParticipant=%b,abortedFunctionCalled=%b," +
                            "committedFunctionCalled=%s,localParticipantCommittedFunctionCalled=%s," +
                            "localParticipantAbortedFunctionCalled=%s",
                            self.abortedByInitiator, self.abortedByLocalParticipant, self.abortedFunctionCalled,
                            self.committedFunctionCalled, self.localParticipantCommittedFunctionCalled,
                            self.localParticipantAbortedFunctionCalled);
    }
};

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
