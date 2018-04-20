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

endpoint http:Listener initiatorEP {
    port:8888
};

endpoint http:Client participant1EP {
    url: "http://localhost:8889"
};

State state = new();

@http:ServiceConfig {
    basePath:"/"
}
service<http:Service> InitiatorService bind initiatorEP {

    getState(endpoint ep, http:Request req) {
        http:Response res = new;
        res.setStringPayload(state.toString());
        state.reset();
        _ = ep -> respond(res);
    }

    testInitiatorAbort(endpoint ep, http:Request req) {


        transaction with oncommit=onCommit, onabort=onAbort {
            http:Request newReq = new;
            _ = participant1EP -> get("/noOp", req = newReq);

            transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
            }

            state.abortedByInitiator = true;
            abort;
        }

        http:Response res = new; res.statusCode = 200;
        _ = ep -> respond(res);
    }

    testRemoteParticipantAbort(endpoint ep, http:Request req) {


        transaction with oncommit=onCommit, onabort=onAbort {
            http:Request newReq = new;
            _ = participant1EP -> get("/testRemoteParticipantAbort", req = newReq);

            transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
            }
        }

        http:Response res = new;  res.statusCode = 200;
        _ = ep -> respond(res);
    }

    testLocalParticipantAbort(endpoint ep, http:Request req) {


        transaction with oncommit=onCommit, onabort=onAbort {
            http:Request newReq = new;
            _ = participant1EP -> get("/noOp", req = newReq);

            transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
                state.abortedByLocalParticipant = true;
                abort;
            }
        }

        http:Response res = new;  res.statusCode = 200;
        _ = ep -> respond(res);
    }

    testLocalParticipantSuccess(endpoint ep, http:Request req) {


        transaction with oncommit=onCommit, onabort=onAbort {
            http:Request newReq = new;
            _ = participant1EP -> get("/noOp", req = newReq);

            transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
            }
        }

        http:Response res = new;  res.statusCode = 200;
        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        transactionInfectable: false
    }
    testTransactionInfectableFalse (endpoint ep, http:Request req) {

        http:Response res = new;  res.statusCode = 500;
        transaction with oncommit=onCommit, onabort=onAbort {
            http:Request newReq = new;
            var result = participant1EP -> get("/nonInfectable", req = newReq);
            match result {
                http:Response participant1Res => {
                    transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
                    }
                    res = participant1Res;
                    if(participant1Res.statusCode == 500) {
                        state.abortedByInitiator = true;
                        abort;
                    }
                }
                error => {
                    res.statusCode = 500;
                }
            }
        }
        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        transactionInfectable: true
    }
    testTransactionInfectableTrue (endpoint ep, http:Request req) {

        transaction with oncommit=onCommit, onabort=onAbort {
            http:Request newReq = new;
            _ = participant1EP -> get("/infectable", req = newReq);

            transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
            }
        }
        http:Response res = new;  res.statusCode = 200;

        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        path:"/"
    }
    member (endpoint conn, http:Request req) {

        transaction {
            http:Request newReq = new;
            var getResult = participant1EP -> get("/", req = newReq);
            match getResult {
                http:HttpConnectorError err => {
                    io:print("Initiator could not send get request to participant. Error:");
                    sendErrorResponseToCaller(conn);
                    abort;
                }
                http:Response participant1Res => {
                    var fwdResult = conn -> respond(participant1Res);
                    match fwdResult {
                        http:HttpConnectorError err => {
                            io:print("Initiator could not forward response from participant 1 to originating client. Error:");
                            io:print(err);
                        }
                        () => io:print("");
                    }
                }
            }
        } onretry {
            io:println("Intiator failed");
        }
    }

    testSaveToDatabaseSuccessfulInParticipant(endpoint ep, http:Request req) {
        http:Response res = new;  res.statusCode = 500;
        transaction with oncommit=onCommit, onabort=onAbort {
            http:Request newReq = new;
            var result = participant1EP -> get("/testSaveToDatabaseSuccessfulInParticipant", req = newReq);
            match result {
                http:Response participant1Res => {
                    transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
                    }
                    res = participant1Res;
                    if(participant1Res.statusCode == 500) {
                        state.abortedByInitiator = true;
                        abort;
                    }
                }
                error => {
                    res.statusCode = 500;
                }
            }
        }
        _ = ep -> respond(res);
    }

    testSaveToDatabaseFailedInParticipant(endpoint ep, http:Request req) {
        http:Response res = new;  res.statusCode = 500;
        transaction with oncommit=onCommit, onabort=onAbort {
            http:Request newReq = new;
            var result = participant1EP -> get("/testSaveToDatabaseFailedInParticipant", req = newReq);
            match result {
                http:Response participant1Res => {
                    transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
                    }
                    res = participant1Res;
                    if(participant1Res.statusCode == 500) {
                        state.abortedByInitiator = true;
                        abort;
                    }
                }
                error => {
                    res.statusCode = 500;
                }
            }
        }
        _ = ep -> respond(res);
    }
}

function sendErrorResponseToCaller(http:Listener conn) {
    endpoint http:Listener conn2 = conn;
    http:Response errRes = new; errRes.statusCode = 500;
    var respondResult = conn2 -> respond(errRes);
    match respondResult {
        http:HttpConnectorError respondErr => {
            io:print("Initiator could not send error response to originating client. Error:");
            io:println(respondErr);
        }
        () => return;
    }
}

function onAbort(string transactionid) {
    state.abortedFunctionCalled = true;
}

function onCommit(string transactionid) {
    state.committedFunctionCalled = true;
}

function onLocalParticipantAbort(string transactionid) {
    state.localParticipantAbortedFunctionCalled = true;
}

function onLocalParticipantCommit(string transactionid) {
    state.localParticipantCommittedFunctionCalled = true;
}

type State object {
    private {
        boolean abortedByInitiator;
        boolean abortedByLocalParticipant;
        boolean abortedFunctionCalled;
        boolean committedFunctionCalled;
        boolean localParticipantAbortedFunctionCalled;
        boolean localParticipantCommittedFunctionCalled;
    }

    function reset() {
        abortedByInitiator = false;
        abortedByLocalParticipant = false;
        abortedFunctionCalled = false;
        committedFunctionCalled = false;
        localParticipantAbortedFunctionCalled = false;
        localParticipantCommittedFunctionCalled = false;
    }

    function toString() returns string {
        return io:sprintf("abortedByInitiator=%b,abortedByLocalParticipant=%b,abortedFunctionCalled=%b," +
                            "committedFunctionCalled=%s,localParticipantCommittedFunctionCalled=%s," +
                            "localParticipantAbortedFunctionCalled=%s",
                            abortedByInitiator, abortedByLocalParticipant, abortedFunctionCalled,
                            committedFunctionCalled, localParticipantCommittedFunctionCalled,
                            localParticipantAbortedFunctionCalled);
    }
};
