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

package ballerina.transactions.coordinator;

import ballerina/caching;
import ballerina/log;
import ballerina/net.http;
import ballerina/util;

const string TRANSACTION_CONTEXT_VERSION = "1.0";

documentation {
    ID of the local participant used when registering with the initiator.
}
string localParticipantId = util:uuid();

documentation {
    This map is used for caching transaction that are initiated.
}
map<Transaction> initiatedTransactions;

documentation {
    This map is used for caching transaction that are this Ballerina instance participates in.
}
map<Transaction> participatedTransactions;

documentation {
    This cache is used for caching HTTP connectors against the URL, since creating connectors is expensive.
}
caching:Cache httpClientCache = caching:createCache("ballerina.http.client.cache", 900000, 100, 0.1);

struct Transaction {
    string transactionId;
    string coordinationType = "2pc";
    map<Participant> participants;
    Protocol[] coordinatorProtocols;
}

struct Participant {
    string participantId;
    Protocol[] participantProtocols;
}

public struct TransactionContext {
    string contextVersion = "1.0";
    string transactionId;
    int transactionBlockId;
    string coordinationType;
    string registerAtURL;
}

documentation {
    This represents the protocol associated with the coordination type.

    F{{name}} - protocol name
    F{{url}}  - protocol URL. This URL will have a value only if the participant is remote. If the participant is local,
                the `protocolFn` will be called
    F{{protocolFn}} - This function will be called only if the participant is local. This avoid calls over the network.
}
public struct Protocol {
    string name;
    string url;
    int transactionBlockId;
    (function (string transactionId,
               int transactionBlockId,
               string protocolAction) returns boolean)|null protocolFn;
}

public struct RegistrationRequest {
    string transactionId;
    string participantId;
    Protocol[] participantProtocols;
}

public transformer <RegistrationRequest req, json j> regRequestToJson() {
    j.transactionId = req.transactionId;
    j.participantId = req.participantId;
    json[] protocols = req.participantProtocols.map(
                                               function (Protocol proto) returns json {
                                                   json j2 = {name:proto.name, url:proto.url};
                                                   return j2;
                                               });
    j.participantProtocols = protocols;
}

public transformer <json j, RegistrationRequest req> jsonToRegRequest() {
    var transactionId =? <string>j.transactionId;
    var participantId =? <string>j.participantId;
    req.transactionId = transactionId;
    req.participantId = participantId;
    Protocol[] protocols = j.participantProtocols.map(
                                                 function (json proto) returns Protocol {
                                                     var name =? <string>proto.name;
                                                     var url =? <string>proto.url;
                                                     Protocol p = {name:name, url:url};
                                                     return p;
                                                 });
    req.participantProtocols = protocols;
}

public struct RegistrationResponse {
    string transactionId;
    Protocol[] coordinatorProtocols;
}

public transformer <RegistrationResponse res, json j> regResposeToJson () {
    j.transactionId = res.transactionId;
    json[] protocols = res.coordinatorProtocols.map(
                                               function (Protocol proto) returns json {
                                                   json j2 = {name:proto.name, url:proto.url};
                                                   return j2;
                                               });
    j.coordinatorProtocols = protocols;
}

public transformer <json j, RegistrationResponse res> jsonToRegResponse () {
    var transactionId =? <string>j.transactionId;
    res.transactionId = transactionId;
    Protocol[] protocols = j.coordinatorProtocols.map(
                                                 function (json proto) returns Protocol {
                                                     var name =? <string>proto.name;
                                                     var url =? <string>proto.url;
                                                     Protocol p = {name:name, url:url};
                                                     return p;
                                                 });
    res.coordinatorProtocols = protocols;
}

public struct RequestError {
    string errorMessage;
}

function isRegisteredParticipant (string participantId, map<Participant> participants) returns boolean {
    return participants.hasKey(participantId);
}

function isValidCoordinationType (string coordinationType) returns boolean {
    foreach coordType in coordinationTypes {
        if (coordinationType == coordType) {
            return true;
        }
    }
    return false;
}

function protocolCompatible (string coordinationType,
                             Protocol[] participantProtocols) returns boolean {
    boolean participantProtocolIsValid = false;
    var validProtocols =? coordinationTypeToProtocolsMap[coordinationType];
    foreach participantProtocol in participantProtocols {
        foreach validProtocol in validProtocols {
            if (participantProtocol.name == validProtocol) {
                participantProtocolIsValid = true;
                break;
            } else {
                participantProtocolIsValid = false;
            }
        }
        if (!participantProtocolIsValid) {
            break;
        }
    }
    return participantProtocolIsValid;
}

function respondToBadRequest (http:ServiceEndpoint conn, string msg) {
    endpoint http:ServiceEndpoint ep = conn;
    log:printError(msg);
    http:Response res = {statusCode:400};
    RequestError err = {errorMessage:msg};
    var resPayload =? <json>err;
    res.setJsonPayload(resPayload);
    var respondResult = ep -> respond(res);
    match respondResult {
        http:HttpConnectorError respondErr => {
            log:printErrorCause("Could not send Bad Request error response to caller", respondErr);
        }
    }
}

function createNewTransaction (string coordinationType) returns Transaction {
    if (coordinationType == TWO_PHASE_COMMIT) {
        TwoPhaseCommitTransaction twopcTxn = {transactionId:util:uuid(), coordinationType:TWO_PHASE_COMMIT};
        Transaction txn = <Transaction>twopcTxn;
        return txn;
    } else {
        error e = {message:"Unknown coordination type: " + coordinationType};
        throw e;
    }
}

function getCoordinatorProtocolAt (string protocol, int transactionBlockId) returns string {
    return "http://" + coordinatorHost + ":" + coordinatorPort + initiator2pcCoordinatorBasePath + "/" +
           transactionBlockId;
}

function getParticipantProtocolAt (string protocol, int transactionBlockId) returns string {
    return "http://" + coordinatorHost + ":" + coordinatorPort + participant2pcCoordinatorBasePath + "/" +
           transactionBlockId;
}

// The initiator will create a new transaction context by calling this function
function createTransactionContext (string coordinationType,
                                   int transactionBlockId) returns TransactionContext|error {
    if (!isValidCoordinationType(coordinationType)) {
        string msg = "Invalid-Coordination-Type:" + coordinationType;
        log:printError(msg);
        error err = {message:msg};
        return err;
    } else {
        Transaction txn = createNewTransaction(coordinationType);
        string txnId = txn.transactionId;
        initiatedTransactions[txnId] = txn;
        TransactionContext txnContext = {transactionId:txnId,
                                            transactionBlockId:transactionBlockId,
                                            coordinationType:coordinationType,
                                            registerAtURL:"http://" + coordinatorHost + ":" + coordinatorPort +
                                                          initiatorCoordinatorBasePath + "/" + transactionBlockId +
                                                          registrationPath};
        log:printInfo("Created transaction: " + txnId);
        return txnContext;
    }
}

function registerParticipantWithLocalInitiator (string transactionId,
                                                int transactionBlockId,
                                                string registerAtURL) returns TransactionContext|error {
    string participantId = getParticipantId(transactionBlockId);
    //TODO: Protocol name should be passed down from the transaction statement
    Protocol participantProtocol = {name:"durable", transactionBlockId:transactionBlockId,
                                       protocolFn:localParticipantProtocolFn};
    if (!initiatedTransactions.hasKey(transactionId)) {
        error err = {message:"Transaction-Unknown. Invalid TID:" + transactionId};
        return err;
    } else {
        var txn =? <TwoPhaseCommitTransaction>initiatedTransactions[transactionId];
        if (isRegisteredParticipant(participantId, txn.participants)) { // Already-Registered
            error err = {message:"Already-Registered. TID:" + transactionId + ",participant ID:" + participantId};
            return err;
        } else if (!protocolCompatible(txn.coordinationType, [participantProtocol])) { // Invalid-Protocol
            error err = {message:"Invalid-Protocol. TID:" + transactionId + ",participant ID:" + participantId};
            return err;
        } else {
            Participant participant = {participantId:participantId};
            participant.participantProtocols = [participantProtocol];
            txn.participants[participantId] = participant;

            //Set initiator protocols
            TwoPhaseCommitTransaction twopcTxn = {transactionId:transactionId, coordinationType:TWO_PHASE_COMMIT};
            Protocol initiatorProto = {name:"durable", transactionBlockId:transactionBlockId};
            twopcTxn.coordinatorProtocols = [initiatorProto];

            string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
            participatedTransactions[participatedTxnId] = twopcTxn;
            TransactionContext txnCtx = {transactionId:transactionId, transactionBlockId:transactionBlockId,
                                            coordinationType:"2pc", registerAtURL:registerAtURL};
            log:printInfo("Registered local participant: " + participantId + " for transaction:" + transactionId);
            return txnCtx;
        }
    }
}

function localParticipantProtocolFn (string transactionId,
                                     int transactionBlockId,
                                     string protocolAction) returns boolean {
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
    if (!participatedTransactions.hasKey(participatedTxnId)) {
        return false;
    }
    var txn =? <TwoPhaseCommitTransaction>participatedTransactions[participatedTxnId];
    if (protocolAction == "prepare") {
        if (txn.state == TransactionState.ABORTED) {
            removeParticipatedTransaction(participatedTxnId);
            return true;
        } else {
            boolean successful = prepareResourceManagers(transactionId, transactionBlockId);
            if (successful) {
                txn.state = TransactionState.PREPARED;
            }
            return successful;
        }
    } else if (protocolAction == "notifycommit") {
        if (txn.state == TransactionState.PREPARED) {
            boolean successful = commitResourceManagers(transactionId, transactionBlockId);
            removeParticipatedTransaction(participatedTxnId);
            return successful;
        }
    } else if (protocolAction == "notifyabort") {
        if (txn.state == TransactionState.PREPARED) {
            boolean successful = abortResourceManagers(transactionId, transactionBlockId);
            removeParticipatedTransaction(participatedTxnId);
            return successful;
        }
    } else {
        error err = {message:"Invalid protocol action:" + protocolAction};
        throw err;
    }
    return false;
}
function removeParticipatedTransaction (string participatedTxnId) {
    boolean removed = participatedTransactions.remove(participatedTxnId);
    if (!removed) {
        error err = {message:"Removing participated transaction: " + participatedTxnId + " failed"};
        throw err;
    }
}

function removeInitiatedTransaction (string transactionId) {
    boolean removed = initiatedTransactions.remove(transactionId);
    if (!removed) {
        error err = {message:"Removing initiated transaction: " + transactionId + " failed"};
        throw err;
    }
}

function getInitiatorClientEP (string registerAtURL) returns InitiatorClientEP {
    if (httpClientCache.hasKey(registerAtURL)) {
        var initiatorEP =? <InitiatorClientEP>httpClientCache.get(registerAtURL);
        return initiatorEP;
    } else {
        InitiatorClientEP initiatorEP = {};
        InitiatorClientConfig config = {registerAtURL:registerAtURL,
                                           endpointTimeout:120000, retryConfig:{count:5, interval:5000}};
        initiatorEP.init(config);
        httpClientCache.put(registerAtURL, initiatorEP);
        return initiatorEP;
    }
}

documentation {
    Registers a participant with the initiator's coordinator. This function will be called by the participant

    P{{transactionId}} - ID of the transaction to which this participant is registering with
    P{{transactionBlockId}} - The local ID of the transaction block on the participant
    P{{registerAtURL}} - The URL of the initiator to which this participant will register with
}
public function registerParticipantWithRemoteInitiator (string transactionId,
                                                        int transactionBlockId,
                                                        string registerAtURL,
                                                        Protocol[] participantProtocols) returns TransactionContext|error {
    endpoint InitiatorClientEP initiatorEP;
    initiatorEP = getInitiatorClientEP(registerAtURL);
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);

    // Register with the coordinator only if the participant has not already done so
    if (participatedTransactions.hasKey(participatedTxnId)) {
        string msg = "Already registered with initiator for transaction:" + participatedTxnId;
        log:printError(msg);
        error err = {message:msg};
        return err;
    }
    log:printInfo("Registering for transaction: " + participatedTxnId + " with coordinator: " + registerAtURL);

    var result = initiatorEP -> register(transactionId, transactionBlockId, participantProtocols);
    match result {
        error e => {
            string msg = "Cannot register with coordinator for transaction: " + transactionId;
            log:printErrorCause(msg, e);
            error err = {message:msg, cause:[e]};
            return err;
        }
        RegistrationResponse regRes => {
            Protocol[] coordinatorProtocols = regRes.coordinatorProtocols;
            TwoPhaseCommitTransaction twopcTxn = {transactionId:transactionId, coordinationType:TWO_PHASE_COMMIT};
            twopcTxn.coordinatorProtocols = coordinatorProtocols;
            participatedTransactions[participatedTxnId] = twopcTxn;
            TransactionContext txnCtx = {transactionId:transactionId, transactionBlockId:transactionBlockId,
                                            coordinationType:"2pc", registerAtURL:registerAtURL};
            log:printInfo("Registered with coordinator for transaction: " + transactionId);
            return txnCtx;
        }
    }
}

function getParticipatedTransactionId (string transactionId, int transactionBlockId) returns string {
    string id = transactionId + ":" + transactionBlockId;
    return id;
}

function getParticipantId (int transactionBlockId) returns string {
    string participantId = localParticipantId + ":" + transactionBlockId;
    return participantId;
}

native function getAvailablePort () returns int;

native function getHostAddress () returns string;
