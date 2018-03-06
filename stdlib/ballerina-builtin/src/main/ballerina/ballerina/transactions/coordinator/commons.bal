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

import ballerina.caching;
import ballerina.log;
import ballerina.net.http;
import ballerina.util;

const string TRANSACTION_CONTEXT_VERSION = "1.0";

// The participant ID of this participant
string localParticipantId = util:uuid();

map initiatedTransactions = {};
map participatedTransactions = {};

// This cache is used for caching HTTP connectors against the URL, since creating connectors is expecsive.
caching:Cache httpClientCache = caching:createCache("ballerina.http.client.cache", 3600000, 10, 0.1);

struct Transaction {
    string transactionId;
    string coordinationType = "2pc";
    map participants;
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

struct Protocol {
    string name;

    // This URL will have a value only if the participant is remote. If the participant is local, the protocolFn will
    // be called
    string url;
    int transactionBlockId;

    // This function will be called only if the participant is local
    function (string transactionId,
              int transactionBlockId,
              string protocolAction) returns (boolean successful) protocolFn;
}

struct RegistrationRequest {
    string transactionId;
    string participantId;
    Protocol[] participantProtocols;
}

// TODO: Make this function into a transformer once https://github.com/ballerina-lang/ballerina/issues/5002 is fixed
function regRequestToJson (RegistrationRequest req) returns (json) {
    json j = {};
    j.transactionId = req.transactionId;
    j.participantId = req.participantId;
    json[] protocols = req.participantProtocols.map(
                                               function (Protocol proto) returns (json) {
                                                   json j2 = {name:proto.name, url:proto.url};
                                                   return j2;
                                               });
    j.participantProtocols = protocols;
    return j;
}

// TODO: Make this function into a transformer once https://github.com/ballerina-lang/ballerina/issues/5002 is fixed
function jsonToRegRequest (json j) returns (RegistrationRequest req) {
    var transactionId, _ = (string)j.transactionId;
    var participantId, _ = (string)j.participantId;
    req = {transactionId:transactionId, participantId:participantId};
    Protocol[] protocols = j.participantProtocols.map(
                                                 function (json proto) returns (Protocol p) {
                                                     var name, _ = (string)proto.name;
                                                     var url, _ = (string)proto.url;
                                                     p = {name:name, url:url};
                                                     return;
                                                 });
    req.participantProtocols = protocols;
    return;
}

struct RegistrationResponse {
    string transactionId;
    Protocol[] coordinatorProtocols;
}

// TODO: Make this function into a transformer once https://github.com/ballerina-lang/ballerina/issues/5002 is fixed
function regResposeToJson (RegistrationResponse res) returns (json) {
    json j = {};
    j.transactionId = res.transactionId;
    json[] protocols = res.coordinatorProtocols.map(
                                               function (Protocol proto) returns (json) {
                                                   json j2 = {name:proto.name, url:proto.url};
                                                   return j2;
                                               });
    j.coordinatorProtocols = protocols;
    return j;
}

// TODO: Make this function into a transformer once https://github.com/ballerina-lang/ballerina/issues/5002 is fixed
function jsonToRegResponse (json j) returns (RegistrationResponse res) {
    var transactionId, _ = (string)j.transactionId;
    res = {transactionId:transactionId};
    Protocol[] protocols = j.coordinatorProtocols.map(
                                                 function (json proto) returns (Protocol p) {
                                                     var name, _ = (string)proto.name;
                                                     var url, _ = (string)proto.url;
                                                     p = {name:name, url:url};
                                                     return;
                                                 });
    res.coordinatorProtocols = protocols;
    return;
}

struct RequestError {
    string errorMessage;
}

function isRegisteredParticipant (string participantId, map participants) returns (boolean) {
    return participants.hasKey(participantId);
}

function isValidCoordinationType (string coordinationType) returns (boolean) {
    foreach coordType in coordinationTypes {
        if (coordinationType == coordType) {
            return true;
        }
    }
    return false;
}

function protocolCompatible (string coordinationType,
                             Protocol[] participantProtocols) returns (boolean participantProtocolIsValid) {
    var validProtocols, _ = (string[])coordinationTypeToProtocolsMap[coordinationType];
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

function respondToBadRequest (string msg) returns (http:OutResponse res) {
    log:printError(msg);
    res = {statusCode:400};
    RequestError err = {errorMessage:msg};
    var resPayload, _ = <json>err;
    res.setJsonPayload(resPayload);
    return;
}

function createNewTransaction (string coordinationType) returns (Transaction txn) {
    if (coordinationType == TWO_PHASE_COMMIT) {
        TwoPhaseCommitTransaction twopcTxn = {transactionId:util:uuid(), coordinationType:TWO_PHASE_COMMIT};
        twopcTxn.participants = {};
        var tx, _ = (Transaction)twopcTxn;
        txn = tx;
    } else {
        error e = {message:"Unknown coordination type: " + coordinationType};
        throw e;
    }
    return;
}

function getCoordinatorProtocolAt (string protocol, int transactionBlockId) returns (string coordinatorProtocolAt) {
    coordinatorProtocolAt =
    "http://" + coordinatorHost + ":" + coordinatorPort + initiator2pcCoordinatorBasePath + "/" + transactionBlockId;
    return;
}

function getParticipantProtocolAt (string protocol, int transactionBlockId) returns (string participatProtocolAt) {
    participatProtocolAt =
    "http://" + coordinatorHost + ":" + coordinatorPort + participant2pcCoordinatorBasePath + "/" + transactionBlockId;
    return;
}

// The initiator will create a new transaction context by calling this function
function createTransactionContext (string coordinationType,
                                   int transactionBlockId) returns (TransactionContext txnContext, error e) {
    if (!isValidCoordinationType(coordinationType)) {
        string msg = "Invalid-Coordination-Type:" + coordinationType;
        log:printError(msg);
        e = {message:msg};
    } else {
        Transaction txn = createNewTransaction(coordinationType);
        string txnId = txn.transactionId;

        initiatedTransactions[txnId] = txn;
        txnContext = {transactionId:txnId,
                         transactionBlockId:transactionBlockId,
                         coordinationType:coordinationType,
                         registerAtURL:"http://" + coordinatorHost + ":" + coordinatorPort +
                                       initiatorCoordinatorBasePath + "/" + transactionBlockId + registrationPath};

        log:printInfo("Created transaction: " + txnId);
    }
    return;
}

function registerParticipantWithLocalInitiator (string transactionId,
                                                int transactionBlockId,
                                                string registerAtURL) returns (TransactionContext txnCtx, error err) {
    string participantId = getParticipantId(transactionBlockId);
    //TODO: Protocol name should be passed down from the transaction statement
    Protocol participantProtocol = {name:"durable", transactionBlockId:transactionBlockId,
                                       protocolFn:localParticipantProtocolFn};
    var txn, _ = (TwoPhaseCommitTransaction)initiatedTransactions[transactionId];
    if (txn == null) {
        err = {message:"Transaction-Unknown. Invalid TID:" + transactionId};
    } else if (isRegisteredParticipant(participantId, txn.participants)) { // Already-Registered
        err = {message:"Already-Registered. TID:" + transactionId + ",participant ID:" + participantId};
    } else if (!protocolCompatible(txn.coordinationType, [participantProtocol])) { // Invalid-Protocol
        err = {message:"Invalid-Protocol. TID:" + transactionId + ",participant ID:" + participantId};
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
        txnCtx = {transactionId:transactionId, transactionBlockId:transactionBlockId,
                     coordinationType:"2pc", registerAtURL:registerAtURL};
        log:printInfo("Registered local participant: " + participantId + " for transaction:" + transactionId);
    }
    return;
}

function localParticipantProtocolFn (string transactionId,
                                     int transactionBlockId,
                                     string protocolAction) returns (boolean successful) {
    if (protocolAction == "prepare") {
        successful = prepareResourceManagers(transactionId, transactionBlockId);
    } else if (protocolAction == "notifycommit") {
        successful = commitResourceManagers(transactionId, transactionBlockId);
    } else if (protocolAction == "notifyabort") {
        successful = abortResourceManagers(transactionId, transactionBlockId);
    }
    return;
}

// Registers a participant with the initiator's coordinator
// This function will be called by the participant
function registerParticipantWithRemoteInitiator (string transactionId,
                                                 int transactionBlockId,
                                                 string registerAtURL) returns (TransactionContext txnCtx, error err) {
    endpoint<InitiatorClient> initiatorEP {
    }

    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);

    // Register with the coordinator only if the participant has not already done so
    if (participatedTransactions[participatedTxnId] != null) {
        log:printError("Already registered with initiator for transaction:" + participatedTxnId);
        return;
    }
    var client, cacheErr = (InitiatorClient)httpClientCache.get(registerAtURL);
    if (cacheErr != null) {
        throw cacheErr; // We can't continue due to a programmer error
    }
    if (client == null) {
        client = create InitiatorClient(registerAtURL);
        httpClientCache.put(registerAtURL, client);
    }
    bind client with initiatorEP;
    log:printInfo("Registering for transaction: " + participatedTxnId + " with coordinator: " + registerAtURL);
    var regRes, e = initiatorEP.register(transactionId, transactionBlockId);
    if (e != null) {
        string msg = "Cannot register with coordinator for transaction: " + transactionId;
        log:printErrorCause(msg, e);
        err = {message:msg};
    } else {
        Protocol[] coordinatorProtocols = regRes.coordinatorProtocols;
        TwoPhaseCommitTransaction twopcTxn = {transactionId:transactionId, coordinationType:TWO_PHASE_COMMIT};
        twopcTxn.coordinatorProtocols = coordinatorProtocols;
        participatedTransactions[participatedTxnId] = twopcTxn;
        txnCtx = {transactionId:transactionId, transactionBlockId:transactionBlockId,
                     coordinationType:"2pc", registerAtURL:registerAtURL};
        log:printInfo("Registered with coordinator for transaction: " + transactionId);
    }
    return;
}

function getParticipatedTransactionId (string transactionId, int transactionBlockId) returns (string id) {
    id = transactionId + ":" + transactionBlockId;
    return;
}

function getParticipantId (int transactionBlockId) returns (string participantId) {
    participantId = localParticipantId + ":" + transactionBlockId;
    return;
}

native function getAvailablePort () returns (int port);

native function getHostAddress () returns (string address);
