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
import ballerina/http;

listener http:Listener participant1EP01 = new(8889);

http:Client participant2EP01 = new("http://localhost:8890");

State1 state1 = new();

@http:ServiceConfig {
    basePath:"/"
}
service participant1 on participant1EP01 {

    resource function getState(http:Caller ep, http:Request req) {
        http:Response res = new;
        res.setTextPayload(state1.toString());
        state1.reset();
        _ = ep -> respond(res);
    }

    resource function testRemoteParticipantAbort(http:Caller ep, http:Request req) {

        transaction {
            transaction { // local participant
            } committed {
                state1.localParticipantCommittedFunctionCalled = true;
            } aborted {
                state1.localParticipantAbortedFunctionCalled = true;
            }
            state1.abortedByParticipant = true;
            abort;
        } committed {
            state1.committedFunctionCalled = true;
        } aborted {
            state1.abortedFunctionCalled = true;
        }
        http:Response res = new;  res.statusCode = 200;
        _ = ep -> respond(res);
    }

    resource function noOp(http:Caller ep, http:Request req) {

        transaction {
            transaction { // local participant
            } committed {
                state1.localParticipantCommittedFunctionCalled = true;
            } aborted {
                state1.localParticipantAbortedFunctionCalled = true;
            }
        } committed {
            state1.committedFunctionCalled = true;
        } aborted {
            state1.abortedFunctionCalled = true;
        }
        http:Response res = new;  res.statusCode = 200;
        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        transactionInfectable: false
    }
    resource function nonInfectable(http:Caller ep, http:Request req) {

        transaction {
            transaction { // local participant
                abort;
            } committed {
                state1.localParticipantCommittedFunctionCalled = true;
            } aborted {
                state1.localParticipantAbortedFunctionCalled = true;
            }
        } committed {
            state1.committedFunctionCalled = true;
        } aborted {
            state1.abortedFunctionCalled = true;
        }
        http:Response res = new;  res.statusCode = 200;
        res.setTextPayload("Non infectable resource call successful");
        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        transactionInfectable: true
    }
    resource function infectable(http:Caller ep, http:Request req) {

        transaction {
            transaction { // local participant
                abort;
            } committed {
                state1.localParticipantCommittedFunctionCalled = true;
            } aborted {
                state1.localParticipantAbortedFunctionCalled = true;
            }
        } committed {
            state1.committedFunctionCalled = true;
        } aborted {
            state1.abortedFunctionCalled = true;
        }
        http:Response res = new;  res.statusCode = 200;
        _ = ep -> respond(res);
    }

    @http:ResourceConfig {
        path:"/"
    }
    resource function member (http:Caller conn, http:Request req) {

        http:Request newReq = new;
        newReq.setHeader("participant-id", req.getHeader("x-b7a-xid"));
        transaction {
            var forwardResult = participant2EP01 -> forward("/task1", req);
            match forwardResult {
                error err => {
                    io:print("Participant1 could not send get request to participant2/task1. Error:");
                    sendErrorResponseToInitiator(conn);
                    abort;
                }
                http:Response forwardRes => {
                    var getResult = participant2EP01 -> get("/task2", message = newReq);
                    match getResult {
                        error err => {
                            io:print("Participant1 could not send get request to participant2/task2. Error:");
                            sendErrorResponseToInitiator(conn);
                            abort;
                        }
                        http:Response getRes => {
                            var forwardRes2 = conn -> respond(getRes);
                            match forwardRes2 {
                                error err => {
                                    io:print("Participant1 could not forward response from participant2 to initiator. Error:");
                                    io:println(err.reason());
                                }
                                () => io:print("");
                            }
                        }
                    }
                }
            }
        } onretry {
            io:println("Participant1 failed");
        }
    }

    resource function testSaveToDatabaseSuccessfulInParticipant(http:Caller ep, http:Request req) {
        http:Response res = new;  res.statusCode = 500;
        http:Request newReq = new;
        var result = participant2EP01 -> get("/testSaveToDatabaseSuccessfulInParticipant", message = newReq);
        match result {
            http:Response participant1Res => {
                res = participant1Res;
            }
            error => {
                res.statusCode = 500;
            }
        }
        _ = ep -> respond(res);
    }

    resource function testSaveToDatabaseFailedInParticipant(http:Caller ep, http:Request req) {
        http:Response res = new;  res.statusCode = 500;
        transaction {
            transaction {
            } committed {
                state1.localParticipantCommittedFunctionCalled = true;
            } aborted {
                state1.localParticipantAbortedFunctionCalled = true;
            }
            http:Request newReq = new;
            var result = participant2EP01 -> get("/testSaveToDatabaseFailedInParticipant", message = newReq);
            match result {
                http:Response participant1Res => {
                    res = participant1Res;
                }
                error => {
                    res.statusCode = 500;
                }
            }
        } committed {
            state1.committedFunctionCalled = true;
        } aborted {
            state1.abortedFunctionCalled = true;
        }
        _ = ep -> respond(res);
    }
}

function sendErrorResponseToInitiator(http:Caller conn) {
    http:Caller conn2 = conn;
    http:Response errRes = new; errRes.statusCode = 500;
    var respondResult = conn2 -> respond(errRes);
    match respondResult {
        error respondErr => {
            io:print("Participant1 could not send error response to initiator. Error:");
            io:println(respondErr.reason());
        }
        () => return;
    }
}

type State1 object {

    boolean abortedByParticipant = false;
    boolean abortedFunctionCalled = false;
    boolean committedFunctionCalled = false;
    boolean localParticipantAbortedFunctionCalled = false;
    boolean localParticipantCommittedFunctionCalled = false;


    function reset() {
        self.abortedByParticipant = false;
        self.abortedFunctionCalled = false;
        self.committedFunctionCalled = false;
        self.localParticipantAbortedFunctionCalled = false;
        self.localParticipantCommittedFunctionCalled = false;
    }

    function toString() returns string {
        return io:sprintf("abortedByParticipant=%b,abortedFunctionCalled=%b,committedFunctionCalled=%s," +
                            "localParticipantAbortedFunctionCalled=%s,localParticipantCommittedFunctionCalled=%s",
                        self.abortedByParticipant, self.abortedFunctionCalled, self.committedFunctionCalled,
                        self.localParticipantAbortedFunctionCalled, self.localParticipantCommittedFunctionCalled);
    }
};
