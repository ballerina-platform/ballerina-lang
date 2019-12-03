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
import ballerinax/java.jdbc;
import ballerina/system;
import ballerina/transactions;
import ballerina/log;

listener http:Listener participant2EP02 = new(8890);

jdbc:Client testDB = new({
    url: "jdbc:h2:file:../../tempdb/TEST_SQL_CONNECTOR",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 10 },
    dbOptions: { "IFEXISTS": true }
});

State2 state2 = new;

@http:ServiceConfig {
    basePath:"/"
}
service participant2 on participant2EP02 {

    resource function getState(http:Caller ep, http:Request req) {
        http:Response res = new;
        res.setTextPayload(state2.toString());
        state2.reset();
        checkpanic ep->respond(res);
    }

    resource function task1 (http:Caller conn, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Resource is invoked");
        var forwardRes = conn -> respond(res);  
        if (forwardRes is error) {
            io:print("Participant2 could not send response to participant1. Error:");
            io:println(forwardRes.reason());
        } else {
            io:print("");
        }
    }
    @transactions:Participant {
    }
    resource function task2 (http:Caller conn, http:Request req) {
        http:Response res = new;
        string result = "incorrect id";
        if (req.getHeader("x-b7a-xid") == req.getHeader("participant-id")) {
            result = "equal id";
        }
        res.setTextPayload(result);
        var forwardRes = conn -> respond(res);  
        if (forwardRes is error) {
            io:print("Participant2 could not send response to participant1. Error:");
            io:println(forwardRes.reason());
        } else {
            io:print("");
        }
    }

    @transactions:Participant {}
    resource function testSaveToDatabaseSuccessfulInParticipant(http:Caller ep, http:Request req) {
        saveToDatabase(ep, req, false);
    }

    @transactions:Participant {}
    resource function testSaveToDatabaseFailedInParticipant(http:Caller ep, http:Request req) {
        io:println("testSaveToDatabaseFailedInParticipant");
        var er = trap saveToDatabase(ep, req, true);
        if (er is error) {
            http:Response res = new;
            res.statusCode = 200;
            res.setTextPayload("error in SaveToDatabase: " + er.reason());
            var resp2 = ep->respond(res);
        }
    }

    @http:ResourceConfig {
        path: "/checkCustomerExists/{uuid}"
    }
    resource function checkCustomerExists(http:Caller ep, http:Request req, string uuid) {
        http:Response res = new;  res.statusCode = 200;
        jdbc:Parameter para1 = {sqlType:jdbc:TYPE_VARCHAR, value:uuid};
        var x = testDB -> select("SELECT registrationID FROM Customers WHERE registrationID = ?", Registration, para1);
        if (x is error) {
            io:println("query returned error");
            res.statusCode = 500;
        } else {
            string payload = "";
            io:println("query returned");
            while (x.hasNext()) {
                io:println("has rows");
                var reg = <Registration>x.getNext();
                io:println(reg);
                payload = reg.REGISTRATIONID;
            }
           res.setTextPayload(<@untainted> payload);
        }

        checkpanic ep->respond(res);
    }
}

type Registration record {
    string REGISTRATIONID;
};


@transactions:Participant {
    oncommit:onCommit2,
    onabort:onAbort2
}
function saveToDatabase(http:Caller conn, http:Request req, boolean shouldAbort) {
    http:Caller ep = conn;
    http:Response res = new;  res.statusCode = 200;

    saveToDatabase_localParticipant();
    string uuid = system:uuid();
    var helperRes = trap saveToDatabaseUpdateHelper1(uuid);
    if (helperRes is error) {
        io:println("FAILED!!!");
    } else {
        io:println("saved");
    }

    if(shouldAbort) {
        io:println("panicking");
        error er = error("Some error occured in particpant after saving to Database");
        panic er;
    }

    res.setTextPayload(uuid);
    var forwardRes = ep -> respond(res);
    if (forwardRes is error) {
        io:print("Participant2 could not send response to participant1. Error:");
        io:println(forwardRes.reason());
    } else {
        io:print("");
    }
}

function saveToDatabaseUpdateHelper1(string uuid) {
    io:println("inserting uuid: " + uuid);
    var result = testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country) " +
                                                     "values ('John', 'Doe', '" + uuid + "', 5000.75, 'USA')");
    if (result is jdbc:UpdateResult) {
        io:println(result);
    } else {
        io:println("");
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

@transactions:Participant {
    oncommit:onLocalParticipantCommit2,
    onabort:onLocalParticipantAbort2
}
function saveToDatabase_localParticipant() {
    log:printInfo("saveToDatabase_localParticipant");
}        

type State2 object {

    boolean abortedFunctionCalled= false;
    boolean committedFunctionCalled= false;
    boolean localParticipantCommittedFunctionCalled= false;
    boolean localParticipantAbortedFunctionCalled= false;


    function reset() {
        self.abortedFunctionCalled = false;
        self.committedFunctionCalled = false;
        self.localParticipantCommittedFunctionCalled = false;
        self.localParticipantAbortedFunctionCalled = false;
    }

    function toString() returns string {
        return io:sprintf("abortedFunctionCalled=%b,committedFunctionCalled=%s," +
                            "localParticipantCommittedFunctionCalled=%s,localParticipantAbortedFunctionCalled=%s",
                            self.abortedFunctionCalled, self.committedFunctionCalled,
                            self.localParticipantCommittedFunctionCalled, self.localParticipantAbortedFunctionCalled);
    }
};
