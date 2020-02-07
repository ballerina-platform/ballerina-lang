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
import ballerina/log;
import ballerina/transactions;


@http:ServiceConfig {
    basePath: "/"
}
service hello on new http:Listener(8890) {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/success",
        transactionInfectable: true
    }
    @transactions:Participant {
        oncommit: baz,
        onabort: bar
    }
    resource function remoteResource(http:Caller caller, http:Request req) {
        log:printInfo("in-remote: ");
        S1 = " in-remote";
        var payload = req.getTextPayload();
        if (payload is string) {
            log:printInfo("in remote: " + payload);
            if (payload == "blowUp") {
                int blowNum = blowUp();
            }
        }

        http:Response res = new;
        res.setPayload(S1 + " payload-from-remote");

        var resp = caller->respond(res);
        if (resp is error) {
            log:printError("Error sending response", resp);
        }
    }
}


string S1 = "";

function baz(string id) {
    log:printInfo("exe-baz-oncommittedFunc in sep proc");
    S1 = S1 + " in-baz[oncommittedFunc]";
}

function bar(string id) {
    log:printInfo("exe-bar-onabortFunc");
    S1 = S1 + " in-bar[onabortFunc]";
}

boolean thrown1 = false;
boolean thrown2 = false;
boolean remoteExecuted = false;
boolean remoteExecuted2 = false;
boolean trx_ran_once = false;

function initGlobalVar() {
    thrown1 = false;
    thrown2 = false;
    remoteExecuted = false;
    remoteExecuted2 = false;
    trx_ran_once = false;
}

function blowUp()  returns int {
    if (5 == 5) {
        error err = error("TransactionError");
        panic err;
    }
    return 5;
}




