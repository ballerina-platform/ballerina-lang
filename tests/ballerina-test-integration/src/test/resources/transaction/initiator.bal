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

import ballerina/net.http;
import ballerina/io;

endpoint http:ServiceEndpoint initiatorEP {
    port:8888
};

endpoint http:ClientEndpoint participant1EP {
    targets: [{uri: "http://localhost:8889"}]
};

@http:ServiceConfig {
    basePath:"/"
}
service<http:Service> InitiatorService bind initiatorEP {

    getState(endpoint ep, http:Request req) {
        string result = io:sprintf("abortedByInitiator=%b,abortedByLocalParticipant=%b,abortedFunctionCalled=%b," +
                                    "committedFunctionCalled=%s,localParticipantCommittedFunctionCalled=%s," +
                                    "localParticipantAbortedFunctionCalled=%s",
                                    [abortedByInitiator, abortedByLocalParticipant, abortedFunctionCalled,
                                    committedFunctionCalled, localParticipantCommittedFunctionCalled,
                                    localParticipantAbortedFunctionCalled]);

        http:Response res = {};
        res.setStringPayload(result);
        _ = ep -> respond(res);
    }

    testInitiatorAbort(endpoint ep, http:Request req) {
        reset();

        transaction with oncommit=onCommit, onabort=onAbort {
            http:Request newReq = {};
            _ = participant1EP -> get("/noOp", {});

            transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
            }

            abortedByInitiator = true;
            abort;
        }

        http:Response res = {statusCode: 200};
        _ = ep -> respond(res);
    }

    testRemoteParticipantAbort(endpoint ep, http:Request req) {
        reset();

        transaction with oncommit=onCommit, onabort=onAbort {
            http:Request newReq = {};
            _ = participant1EP -> get("/testRemoteParticipantAbort", {});

            transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
            }
        }

        http:Response res = {statusCode: 200};
        _ = ep -> respond(res);
    }

    testLocalParticipantAbort(endpoint ep, http:Request req) {
        reset();

        transaction with oncommit=onCommit, onabort=onAbort {
            http:Request newReq = {};
            _ = participant1EP -> get("/noOp", {});

            transaction with oncommit=onLocalParticipantCommit, onabort=onLocalParticipantAbort { // local participant
                abortedByLocalParticipant = true;
                abort;
            }
        }

        http:Response res = {statusCode: 200};
        _ = ep -> respond(res);
    }

    testLocalParticipantSuccess(endpoint ep, http:Request req) {
        reset();
        http:Response res = {statusCode: 200};

        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        transactionInfectable: false
    }
    testTransactionInfectableFalse (endpoint ep, http:Request req) {
        reset();
        http:Response res = {statusCode: 200};

        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        transactionInfectable: true
    }
    testTransactionInfectableTrue (endpoint ep, http:Request req) {
        reset();
        http:Response res = {statusCode: 200};

        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        path:"/"
    }
    member (endpoint conn, http:Request req) {

        transaction {
            http:Request newReq = {};
            var getResult = participant1EP -> get("/", newReq);
            match getResult {
                http:HttpConnectorError err => {
                    io:print("Initiator could not send get request to participant. Error:");
                    sendErrorResponseToCaller(conn);
                    abort;
                }
                http:Response participant1Res => {
                    var fwdResult = conn -> forward(participant1Res); 
                    match fwdResult {
                        http:HttpConnectorError err => {
                            io:print("Initiator could not forward response from participant 1 to originating client. Error:");
                            io:print(err);
                        }
                        null => io:print("");
                    }
                }
            }
        } onretry {
            io:println("Intiator failed");
        }
    }
}

function sendErrorResponseToCaller(http:ServiceEndpoint conn) {
    endpoint http:ServiceEndpoint conn2 = conn;
    http:Response errRes = {statusCode: 500};
    var respondResult = conn2 -> respond(errRes);
    match respondResult {
        http:HttpConnectorError respondErr => {
            io:print("Initiator could not send error response to originating client. Error:");
            io:println(respondErr);
        }
        null => return;
    }
}

function onAbort() {
    abortedFunctionCalled = true;
}

function onCommit() {
    committedFunctionCalled = true;
}

function onLocalParticipantAbort() {
    localParticipantAbortedFunctionCalled = true;
}

function onLocalParticipantCommit() {
    localParticipantCommittedFunctionCalled = true;
}

boolean abortedByInitiator;
boolean abortedByLocalParticipant;
boolean abortedFunctionCalled;
boolean committedFunctionCalled;
boolean localParticipantAbortedFunctionCalled;
boolean localParticipantCommittedFunctionCalled;

function reset() {
    abortedByInitiator = false;
    abortedByLocalParticipant = false;
    abortedFunctionCalled = false;
    committedFunctionCalled = false;
    localParticipantAbortedFunctionCalled = false;
    localParticipantCommittedFunctionCalled = false;
}
