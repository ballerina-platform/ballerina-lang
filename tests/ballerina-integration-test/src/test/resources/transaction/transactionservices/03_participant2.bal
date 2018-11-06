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

import ballerina/http;
import ballerina/io;
import ballerina/sql;
import ballerina/h2;
import ballerina/system;

endpoint http:Listener participant2EP02 {
    port:8890
};

endpoint h2:Client testDB {
    path: "../../../target/tempdb/",
    name: "TEST_SQL_CONNECTOR",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 10 },
    dbOptions: { "IFEXISTS": true }
};

State2 state2 = new;

@http:ServiceConfig {
    basePath:"/"
}
service<http:Service> participant2 bind participant2EP02 {

    getState(endpoint ep, http:Request req) {
        http:Response res = new;
        res.setTextPayload(state2.toString());
        state2.reset();
        _ = ep -> respond(res);
    }

    task1 (endpoint conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Resource is invoked");
        var forwardRes = conn -> respond(res);  
        match forwardRes {
            error err => {
                io:print("Participant2 could not send response to participant1. Error:");
                io:println(err);
            }
            () => io:print("");
        }
    }

    task2 (endpoint conn, http:Request req) {
        http:Response res = new;
        string result = "incorrect id";
        transaction {
            if (req.getHeader("x-b7a-xid") == req.getHeader("participant-id")) {
                result = "equal id";
            }
        }
        res.setTextPayload(result);
        var forwardRes = conn -> respond(res);  
        match forwardRes {
            error err => {
                io:print("Participant2 could not send response to participant1. Error:");
                io:println(err);
            }
            () => io:print("");
        }
    }

    testSaveToDatabaseSuccessfulInParticipant(endpoint ep, http:Request req) {
        saveToDatabase(ep, req, false);
    }

    testSaveToDatabaseFailedInParticipant(endpoint ep, http:Request req) {
        saveToDatabase(ep, req, true);
    }

    @http:ResourceConfig {
        path: "/checkCustomerExists/{uuid}"
    }
    checkCustomerExists(endpoint ep, http:Request req, string uuid) {
        http:Response res = new;  res.statusCode = 200;
        sql:Parameter para1 = {sqlType:sql:TYPE_VARCHAR, value:uuid};
        var x = testDB -> select("SELECT registrationID FROM Customers WHERE registrationID = ?", Registration, para1);
        match x {
            table dt => {
               string payload;
               while (dt.hasNext()) {
                   Registration reg = check <Registration>dt.getNext();
                   io:println(reg);
                   payload = reg.REGISTRATIONID;
               }
               res.setTextPayload(untaint payload);
            }
            error err1 => {
               res.statusCode = 500;
            }
        }

        _ = ep -> respond(res);
    }
}

type Registration record {
    string REGISTRATIONID;
};

function saveToDatabase(http:Listener conn, http:Request req, boolean shouldAbort) {
    endpoint http:Listener ep = conn;
    http:Response res = new;  res.statusCode = 200;
    transaction with oncommit=onCommit2, onabort=onAbort2 {
        transaction with oncommit=onLocalParticipantCommit2, onabort=onLocalParticipantAbort2 {
        }
        string uuid = system:uuid();

        try {
            var result = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                                 values ('John', 'Doe', '" + uuid + "', 5000.75, 'USA')");
            match result {
                int insertCount => io:println(insertCount);
                error => io:println("");
            }
        } catch (error e) {
            io:println("FAILED!!!!!");
        }

        res.setTextPayload(uuid);
        var forwardRes = ep -> respond(res);
        match forwardRes {
            error err => {
                io:print("Participant2 could not send response to participant1. Error:");
                io:println(err);
            }
            () => io:print("");
        }
        if(shouldAbort) {
            abort;
        }
    }
}

function onAbort2(string transactionid) {
    state2.abortedFunctionCalled = true;
}

function onCommit2(string transactionid) {
    state2.committedFunctionCalled = true;
}

function onLocalParticipantAbort2(string transactionid) {
    state2.localParticipantAbortedFunctionCalled = true;
}

function onLocalParticipantCommit2(string transactionid) {
    state2.localParticipantCommittedFunctionCalled = true;
}

type State2 object {

    boolean abortedFunctionCalled;
    boolean committedFunctionCalled;
    boolean localParticipantCommittedFunctionCalled;
    boolean localParticipantAbortedFunctionCalled;


    function reset() {
        abortedFunctionCalled = false;
        committedFunctionCalled = false;
        localParticipantCommittedFunctionCalled = false;
        localParticipantAbortedFunctionCalled = false;
    }

    function toString() returns string {
        return io:sprintf("abortedFunctionCalled=%b,committedFunctionCalled=%s," +
                            "localParticipantCommittedFunctionCalled=%s,localParticipantAbortedFunctionCalled=%s",
                            abortedFunctionCalled, committedFunctionCalled,
                                localParticipantCommittedFunctionCalled, localParticipantAbortedFunctionCalled);
    }
};
