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
service<http:Service> hello bind { port: 10234 } {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/",
        transactionInfectable: true
    }
    @transactions:Participant {
        oncommitFunc: baz,
        onabortFunc: bar
    }
    sayHello(endpoint caller, http:Request req) {
        S1 = S1 + " in-remote";
        var payload =  req.getTextPayload();
            if (payload is string) {
                log:printInfo("in remote: " + payload);
                if (payload == "blowUp") {
                    int blowNum = blowUp();
            }
        }

        http:Response res = new;
        res.setPayload("payload-from-remote");

        caller->respond(res) but { error e => log:printError("Error sending response", err = e) };
    }
}


string S1 = "";

function baz(string id) {
    S1 = S1 + " in-baz[oncommittedFunc]";
}

function bar(string id) {
    S1 = S1 + " in-bar[onabortFunc]";
}

boolean thrown1 = false;
boolean thrown2 = false;
boolean remoteExecuted = false;
boolean remoteExecuted2 = false;
boolean trx_ran_once = false;

function initiatorFunc(boolean throw1, boolean throw2, boolean remote1, boolean remote2,
                            boolean blowRemote1, boolean blowRemote2) returns string {
    endpoint http:Client clientEP {
        url:"http://localhost:10234"
    };
    transaction with retries=2 {
        S1 = S1 + " in-trx-block";
        //http:Request req = new;
        //req.setTextPayLoad("blowUp");
        if (remoteExecuted == false && remote1) {
            remoteExecuted = true;
            string blowOrNot = blowRemote1 ? "blowUp" : "Don't-blowUp";
            var resp = clientEP->post("/", blowOrNot);
            match (resp) {
                http:Response res => {
                    log:printInfo("remote response status code: " + res.statusCode);
                    if (res.statusCode == 500) {
                        S1 = S1 + " remote1-blown";
                    } else {
                        match (res.getTextPayload()) {
                            string r => {
                                log:printInfo(r);
                                S1 = S1 + " <" + untaint r + ">";
                            }
                            error err => log:printError(err.reason());
                        }
                    }
                }
                error err => log:printError(err.reason());
            }
        }
        if (trx_ran_once && remoteExecuted2 == false && remote2) {
            remoteExecuted = true;
            string blowOrNot = blowRemote2 ? "blowUp" : "Don't-blowUp";
            var resp = clientEP->post("/", blowOrNot);
            match (resp) {
                http:Response res => {
                    log:printInfo("remote response status code: " + res.statusCode);
                    if (res.statusCode == 500) {
                        S1 = S1 + " remote2-blown";
                    } else {
                        match (res.getTextPayload()) {
                            string r => {
                                log:printInfo(r);
                                S1 = S1 + " <" + untaint r + ">";
                            }
                            error err => log:printError(err.reason());
                        }
                    }
                }
                error err => log:printError(err.reason());
            }
        }
        if (throw1 && !thrown1) {
            S1 = S1 + " throw-1";
            thrown1 = true;
            log:printInfo("blowUp-1");
            int blowNum = blowUp();
        }
        if (throw2 && !thrown2) {
            S1 = S1 + " throw-2";
            thrown2 = true;
            log:printInfo("blowUp-2");
            int blowNum = blowUp();
        }
        S1 = S1 + " in-trx-lastline";
        log:printInfo("trx-last-line");
    } onretry {
        log:printInfo("on retry block ran");
        S1 = S1 + " onretry-block";
        trx_ran_once = true;
    } committed {
        log:printInfo("commited block ran");

        S1 = S1 + " committed-block";
    } aborted {
        log:printInfo("aborted ran");

        S1 = S1 + " aborted-block";
    }
    S1 = S1 + " after-trx";
    return S1;
}

function blowUp()  returns int {
    if (5 == 5) {
        error err = error("TransactionError");
        panic err;
    }
    return 5;
}
