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

http:Client separateRMParticipant01 = new("http://localhost:8890");

@http:ServiceConfig {
    basePath: "/"
}
service hello on new http:Listener(8889) {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/",
        transactionInfectable: true
    }
    @transactions:Participant {
        oncommit: baz,
        onabort: bar
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        log:printInfo("in-remote: ");
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

        var resp = caller->respond(res);
        if (resp is error) {
            log:printError("Error sending response", resp);
        }
    }

    @transactions:Participant {
        oncommit: baz,
        onabort: bar
    }
    resource function returnError(http:Caller caller, http:Request req) returns error? {
        log:printInfo("in-remote: ");
        S1 = S1 + " in-remote";
        var payload =  req.getTextPayload();

        var b = trap blowUp();
        int c = check b;

        http:Response res = new;
        res.setPayload("payload-from-remote");

        var resp = caller->respond(res);
        if (resp is error) {
            log:printError("Error sending response", resp);
        }
        return;
    }

     @http:ResourceConfig {
        methods: ["POST"],
        path: "/nestedTrx",
        transactionInfectable: true
    }
    @transactions:Participant {
        oncommit: baz,
        onabort: bar
    }
    resource function nestedTrx(http:Caller caller, http:Request req) {
        boolean nestedTrxInRemote = false;
        boolean nestedTrxInParticipant = false;
        boolean nestedTrxInNonParticipantLocalFunc = false;
        string s = "";
        log:printInfo("in-remote: ");
        var payload =  req.getTextPayload();
        if (payload is string) {
            log:printInfo("in remote: " + payload);
            if (payload == "nestedInRemote") {
                nestedTrxInRemote = true;
            } else if (payload == "nestedInRemotesLocalParticipant") {
                nestedTrxInParticipant = true;
            } else if (payload == "nestedTrxInNonParticipantLocalFunc") {
                nestedTrxInNonParticipantLocalFunc = true;
            }
        }

        if (nestedTrxInRemote) {
            transaction {
                log:printInfo("In nested trx in remote");
                s += "in nested transaction";
            }
        } else if (nestedTrxInParticipant) {
            log:printInfo("In nested trx in remote's local participant");
            var participant = trap participantWithNestedTransaction();
            if (participant is string) {
                s += participant;
            } else {
                s += "remote-local-error:[" + <string>participant.detail().message + "]";
            }
        } else if (nestedTrxInNonParticipantLocalFunc) {
            log:printInfo("In nested trx in remote's local non participant");
            var participant = trap localNonParticipantWithNestedTransaction();
            if (participant is string) {
                s += participant;
            } else {
                s += "remote-local-error-trapped:[" + <string>participant.detail().message + "]";
            }
        }


        http:Response res = new;
        res.setPayload(s);
        var resp = caller->respond(res);
        if (resp is error) {
            log:printError("Error sending response", resp);
        }
    }
}

@transactions:Participant {}
function participantWithNestedTransaction() returns string {
    string s = "";
    log:printInfo("In remote's local participant");
    transaction {
        log:printInfo("In nested trx in remote's local");
        s += "in nested transaction";
    }
    return s;
}

function localNonParticipantWithNestedTransaction() returns string {
    string s = "";
    log:printInfo("In remote's local non participant");
    transaction {
        log:printInfo("In nested trx in remote's local");
        s += "in nested transaction";
    }
    return s;
}

@transactions:Participant {}
function localParticipant() returns string {
    return " from-init-local-participant";
}


string S1 = "";

function baz(string id) {
    log:printInfo("exe-baz-oncommittedFunc");
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

function initiatorFunc(boolean throw1, boolean throw2, 
                        boolean remote1, boolean remote2,
                        boolean blowRemote1, boolean blowRemote2) returns string {
    http:Client participantEP = new("http://localhost:8889");
    initGlobalVar();
    S1 = "";
    transaction with retries=2 {
        log:printInfo("trx-first-line");
        S1 = S1 + " in-trx-block";
        if (remoteExecuted == false && remote1) {
            log:printInfo("remote1-call");
            remoteExecuted = true;
            string blowOrNot = blowRemote1 ? "blowUp" : "Don't-blowUp";
            var resp = participantEP->post("/", blowOrNot);
            log:printInfo("remote1-call-responded [" + blowOrNot + "]");

            if (resp is http:Response) {
                log:printInfo("remote response status code: " + resp.statusCode);
                if (resp.statusCode == 500) {
                    S1 = S1 + " remote1-blown";
                } else {
                    var payload = resp.getTextPayload();
                    if (payload is string) {
                        log:printInfo(payload);
                        S1 = S1 + " <" + untaint payload + ">";
                    } else {
                        log:printError(payload.reason());
                    }
                }
            } else {
                log:printError(resp.reason());
            }
        }
        if (trx_ran_once && remoteExecuted2 == false && remote2) {
            log:printInfo("remote2-call");
            remoteExecuted = true;
            string blowOrNot = blowRemote2 ? "blowUp" : "Don't-blowUp";
            var resp = participantEP->post("/", blowOrNot);
            log:printInfo("remote2-call-responded [" + blowOrNot + "]");

            if (resp is http:Response) {
                log:printInfo("remote response status code: " + resp.statusCode);
                if (resp.statusCode == 500) {
                    S1 = S1 + " remote2-blown";
                } else {
                    var payload = resp.getTextPayload();
                    if (payload is string) {
                        log:printInfo(payload);
                        S1 = S1 + " <" + untaint payload + ">";
                    } else {
                        log:printError(payload.reason());
                    }
                }
            } else {
                log:printError(resp.reason());
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

function initiateNestedTransactionInRemote(string nestingMethod) returns string {
    http:Client remoteEp = new("http://localhost:8889");
    string s = "";
    transaction {
        s += " in initiator-trx";
        var resp = remoteEp->post("/nestedTrx", nestingMethod);
        if (resp is http:Response) {
            if (resp.statusCode == 500) {
                s += " remote1-excepted";
                var payload = resp.getTextPayload();
                if (payload is string) {
                    s += ":[" + untaint payload + "]";
                }
            } else {
                var text = resp.getTextPayload();
                if (text is string) {
                    log:printInfo(text);
                    s += " <" + untaint text + ">";
                } else {
                    s += " error-in-remote-response " + text.reason();
                    log:printError(text.reason());
                }
            }
        } else {
            s += " remote call error: " + resp.reason();
        }
    } onretry {
        s += " onretry";
    } committed {
        s += " committed";
    } aborted {
        s += " aborted";
    }
    return s;
}

function remoteErrorReturnInitiator() returns string {
    http:Client remoteEp = new("http://localhost:8889");
    string s = "";
    transaction {
        s += " in initiator-trx";
        var resp = remoteEp->get("/returnError");
        if (resp is http:Response) {
            if (resp.statusCode == 500) {
                s += " remote1-excepted";
                var payload = resp.getTextPayload();
                if (payload is string) {
                    s += ":[" + untaint payload + "]";
                }
            } else {
                var text = resp.getTextPayload();
                if (text is string) {
                    log:printInfo(text);
                    s += " <" + untaint text + ">";
                } else {
                    s += " error-in-remote-response " + text.reason();
                    log:printError(text.reason());
                }
            }
        } else {
            s += " remote call error: " + resp.reason();
        }
    } onretry {
        s += " onretry";
    } committed {
        s += " committed";
    } aborted {
        s += " aborted";
    }
    return s;
}

@http:ServiceConfig {
    basePath: "/"
}
service initiatorService on new http:Listener(8888) {
    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function remoteParticipantTransactionSuccessTest(http:Caller caller, http:Request req) {
        string result = initiatorFunc(false, false,
                                      true, false,
                                      false, false);
        http:Response res = new;
        res.setPayload(result);
        var r = caller->respond(res);
        if (r is error) {
            log:printError("Error sending response: " + result, r);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function remoteParticipantTransactionFailSuccessTest(http:Caller caller, http:Request req) {
        string result = initiatorFunc(true, false,
                                      true, true,
                                      false, false);
        http:Response res = new;
        res.setPayload(result);
        var r = caller->respond(res);
        if (r is error) {
            log:printError("Error sending response: " + result, r);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function remoteParticipantTransactionExceptionInRemoteNoSecondRemoteCall(http:Caller caller,
                                                                                http:Request req) {
        string result = initiatorFunc(false, false,
                                      true, false,
                                      true, false);
        http:Response res = new;
        res.setPayload(result);
        var r = caller->respond(res);
        if (r is error) {
            log:printError("Error sending response: " + result, r);
        }
    }


    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function remoteParticipantTransactionExceptionInRemoteThenSuccessInRemote(http:Caller caller,
                                                                                http:Request req) {
        string result = initiatorFunc(false, false,
                                      true, true,
                                      true, false);
        http:Response res = new;
        res.setPayload(result);
        var r = caller->respond(res);
        if (r is error) {
            log:printError("Error sending response: " + result, r);
        }
    }


    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function remoteParticipantTransactionExceptionInRemoteThenExceptionInRemote(http:Caller caller,
                                                                                http:Request req) {
        string result = initiatorFunc(false, false,
                                      true, true,
                                      true, true);
        http:Response res = new;
        res.setPayload(result);
        var r = caller->respond(res);
        if (r is error) {
            log:printError("Error sending response: " + result, r);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function remoteParticipantStartNestedTransaction(http:Caller caller, http:Request req) {
        string result = initiateNestedTransactionInRemote("nestedInRemote");
        http:Response res = new;
        res.setPayload(untaint result);
        var r = caller->respond(res);
        if (r is error) {
            log:printError("Error sending response: " + result, r);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function remoteParticipantStartNestedTransactionNestedInRemotesLocalParticipant(http:Caller
                            caller, http:Request req) {

        string result = initiateNestedTransactionInRemote("nestedInRemotesLocalParticipant");
        http:Response res = new;
        res.setPayload(untaint result);
        var r = caller->respond(res);
        if (r is error) {
            log:printError("Error sending response: " + result, r);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function remoteParticipantStartNestedTransactionInRemotesLocalNonParticipant(http:Caller
                            caller, http:Request req) {

        string result = initiateNestedTransactionInRemote("nestedTrxInNonParticipantLocalFunc");
        http:Response res = new;
        res.setPayload(untaint result);
        var r = caller->respond(res);
        if (r is error) {
            log:printError("Error sending response: " + result, r);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function remoteParticipantReturnsError(http:Caller
                            caller, http:Request req) {

        string result = remoteErrorReturnInitiator();
        http:Response res = new;
        res.setPayload(untaint result);
        var r = caller->respond(res);
        if (r is error) {
            log:printError("Error sending response: " + result, r);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function testInfectSeparateRM(http:Caller ep, http:Request req) {
        http:Response res = new;  res.statusCode = 200;
        string s = "in-remote-init";
        transaction {
            s += " in-trx";
            var reqText = req.getTextPayload();
            if (reqText is string) {
                log:printInfo("req to remote: " + reqText);
            }
            var result = separateRMParticipant01 -> post("/success", untaint req);
            if (result is http:Response) {
                s += " [remote-status:" + result.statusCode + "] ";
                var p = result.getTextPayload();
                if (p is string) {
                    s += p;
                } else {
                    s += " error-getTextPayload";
                }
            } else {
                s += " error-from-remote: " + result.reason() + "desc: " + <string> result.detail().message;
            }
            s += localParticipant();
        } onretry {
            s += " initiator-retry";
        } committed {
            log:printInfo("testLocalParticipantSuccess-committed block");
            s += " initiator-committed-block";
        } aborted {
            log:printInfo("testLocalParticipantSuccess-aborted block");
            s += " initiator-abort-block";
        }

        var stt = res.setTextPayload(untaint s);
        checkpanic ep->respond(res);
    }
}
