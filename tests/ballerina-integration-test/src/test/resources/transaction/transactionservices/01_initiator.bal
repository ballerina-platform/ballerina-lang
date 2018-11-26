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
        _ = ep -> respond(res);
    }

    resource function testInitiatorAbort(http:Caller ep, http:Request req) {
        transaction {
            _ = participant1EP00 -> get("/noOp");

            transaction { // local participant
            } committed {
                state0.localParticipantCommittedFunctionCalled = true;
            } aborted {
                state0.localParticipantAbortedFunctionCalled = true;
            }

            state0.abortedByInitiator = true;
            abort;
        } committed {
            state0.committedFunctionCalled = true;
        } aborted {
            state0.abortedFunctionCalled = true;
        }

        http:Response res = new; res.statusCode = 200;
        _ = ep -> respond(res);
    }

    resource function testRemoteParticipantAbort(http:Caller ep, http:Request req) {


        transaction {
            _ = participant1EP00 -> get("/testRemoteParticipantAbort");

            transaction { // local participant
            }
            } committed {
                state0.committedFunctionCalled = true;
            } aborted {
                state0.abortedFunctionCalled = true;
            }

        http:Response res = new;  res.statusCode = 200;
        _ = ep -> respond(res);
    }

    resource function testLocalParticipantAbort(http:Caller ep, http:Request req) {


        transaction {
            _ = participant1EP00 -> get("/noOp");

            transaction { // local participant
                state0.abortedByLocalParticipant = true;
                abort;
            } committed {
                state0.localParticipantCommittedFunctionCalled = true;
            } aborted {
                state0.localParticipantAbortedFunctionCalled = true;
            }
        } committed {
            state0.committedFunctionCalled = true;
        } aborted {
            state0.abortedFunctionCalled = true;
        }


        http:Response res = new;  res.statusCode = 200;
        _ = ep -> respond(res);
    }

    resource function testLocalParticipantSuccess(http:Caller ep, http:Request req) {


        transaction {
            _ = participant1EP00 -> get("/noOp");

            transaction { // local participant
            } committed {
                state0.localParticipantCommittedFunctionCalled = true;
            } aborted {
                state0.localParticipantAbortedFunctionCalled = true;
            }
        } committed {
            state0.committedFunctionCalled = true;
        } aborted {
            state0.abortedFunctionCalled = true;
        }

        http:Response res = new;  res.statusCode = 200;
        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        transactionInfectable: false
    }
    resource function testTransactionInfectableFalse (http:Caller ep, http:Request req) {

        http:Response res = new;  res.statusCode = 500;
        transaction {
            var result = participant1EP00 -> get("/nonInfectable");
            match result {
                http:Response participant1Res => {
                    transaction { // local participant
                    } committed {
                        state0.localParticipantCommittedFunctionCalled = true;
                    } aborted {
                        state0.localParticipantAbortedFunctionCalled = true;
                    }
                    res = participant1Res;
                    if(participant1Res.statusCode == 500) {
                        state0.abortedByInitiator = true;
                        abort;
                    }
                }
                error => {
                    res.statusCode = 500;
                }
            }
        } committed {
            state0.committedFunctionCalled = true;
        } aborted {
            state0.abortedFunctionCalled = true;
        }
        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        transactionInfectable: true
    }
    resource function testTransactionInfectableTrue (http:Caller ep, http:Request req) {

        transaction {
            _ = participant1EP00 -> get("/infectable");

            transaction { // local participant
            } committed {
                state0.localParticipantCommittedFunctionCalled = true;
            } aborted {
                state0.localParticipantAbortedFunctionCalled = true;
            }
        } committed {
            state0.committedFunctionCalled = true;
        } aborted {
            state0.abortedFunctionCalled = true;
        }

        http:Response res = new;  res.statusCode = 200;

        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        path:"/"
    }
    resource function member (http:Caller conn, http:Request req) {

        transaction {
            var getResult = participant1EP00 -> get("/");
            match getResult {
                error err => {
                    io:print("Initiator could not send get request to participant. Error:");
                    sendErrorResponseToCaller(conn);
                    abort;
                }
                http:Response participant1Res => {
                    var fwdResult = conn -> respond(participant1Res);
                    match fwdResult {
                        error err => {
                            io:print("Initiator could not forward response from participant 1 to originating client. Error:");
                            io:print(err.reason());
                        }
                        () => io:print("");
                    }
                }
            }
        } onretry {
            io:println("Intiator failed");
        }
    }

    resource function testSaveToDatabaseSuccessfulInParticipant(http:Caller ep, http:Request req) {
        http:Response res = new;  res.statusCode = 500;
        transaction {
            var result = participant1EP00 -> get("/testSaveToDatabaseSuccessfulInParticipant");
            match result {
                http:Response participant1Res => {
                    transaction { // local participant
                    } committed {
                        state0.localParticipantCommittedFunctionCalled = true;
                    } aborted {
                        state0.localParticipantAbortedFunctionCalled = true;
                    }
                    res = participant1Res;
                    if(participant1Res.statusCode == 500) {
                        state0.abortedByInitiator = true;
                        abort;
                    }
                }
                error => {
                    res.statusCode = 500;
                }
            }
        } committed {
            state0.committedFunctionCalled = true;
        } aborted {
            state0.abortedFunctionCalled = true;
        }
        _ = ep -> respond(res);
    }

    resource function testSaveToDatabaseFailedInParticipant(http:Caller ep, http:Request req) {
        http:Response res = new;  res.statusCode = 500;
        transaction {
            var result = participant1EP00 -> get("/testSaveToDatabaseFailedInParticipant");
            match result {
                http:Response participant1Res => {
                    transaction { // local participant
                    } committed {
                        state0.localParticipantCommittedFunctionCalled = true;
                    } aborted {
                        state0.localParticipantAbortedFunctionCalled = true;
                    }
                    res = participant1Res;
                    if(participant1Res.statusCode == 500) {
                        state0.abortedByInitiator = true;
                        abort;
                    }
                }
                error => {
                    res.statusCode = 500;
                }
            }
        } committed {
            state0.committedFunctionCalled = true;
        } aborted {
            state0.abortedFunctionCalled = true;
        }
        _ = ep -> respond(res);
    }
}

function sendErrorResponseToCaller(http:Caller conn) {
    http:Caller conn2 = conn;
    http:Response errRes = new; errRes.statusCode = 500;
    var respondResult = conn2 -> respond(errRes);
    match respondResult {
        error respondErr => {
            io:print("Initiator could not send error response to originating client. Error:");
            io:println(respondErr.reason());
        }
        () => return;
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
