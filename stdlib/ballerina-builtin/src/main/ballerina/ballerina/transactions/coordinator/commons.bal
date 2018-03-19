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
import ballerina.io;
import ballerina.log;
import ballerina.net.http;
import ballerina.util;

const string TRANSACTION_CONTEXT_VERSION = "1.0";

documentation {
    ID of the local participant used when registering with the initiator.
}
string localParticipantId = util:uuid();

const boolean initialized = initialize();

documentation {
    This cache is used for caching transaction that are initiated.
}
Cache initiatedTransactions;
//caching:Cache initiatedTransactions = caching:createCache("ballerina.transactions.initiated.cache", 1200000, 100000000, 0.1);

documentation {
    This cache is used for caching transaction that are this Ballerina instance participates in.
}
Cache participatedTransactions;
//caching:Cache participatedTransactions = caching:createCache("ballerina.transactions.participated.cache", 1200000, 100000000, 0.1);

documentation {
    This cache is used for caching HTTP connectors against the URL, since creating connectors is expensive.
}
caching:Cache httpClientCache = caching:createCache("ballerina.http.client.cache", 900000, 100, 0.1);

struct Cache {
    map content;
}

function <Cache c> get (string key) returns (any) {
    return c.content[key];
}

function <Cache c> put (string key, any value) {
    c.content[key] = value;
}

function <Cache c> remove (string key) {
    boolean removed = c.content.remove(key);
    if(!removed) {
        log:printError("Remove from cache failed for key:" + key);
    }
}

function <Cache c> hasKey (string key) returns (boolean) {
    return c.content.hasKey(key);
}

function initialize () returns (boolean) {
    initiatedTransactions = {};
    initiatedTransactions.content = {};
    participatedTransactions = {};
    participatedTransactions.content = {};
    return true;
}

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

documentation {
    This represents the protocol associated with the coordination type.

    F{{name}} - protocol name
    F{{url}}  - protocol URL. This URL will have a value only if the participant is remote. If the participant is local,
                the `protocolFn` will be called
    F{{protocolFn}} - This function will be called only if the participant is local. This avoid calls over the network.
}
struct Protocol {
    string name;
    string url;
    int transactionBlockId;
    function (string transactionId,
              int transactionBlockId,
              string protocolAction) returns (boolean successful) protocolFn;
}

struct RegistrationRequest {
    string transactionId;
    string participantId;
    Protocol[] participantProtocols;
}

transformer <RegistrationRequest req, json j> regRequestToJson() {
    j.transactionId = req.transactionId;
    j.participantId = req.participantId;
    json[] protocols = req.participantProtocols.map(
                                               function (Protocol proto) returns (json) {
                                                   json j2 = {name:proto.name, url:proto.url};
                                                   return j2;
                                               });
    j.participantProtocols = protocols;
}

transformer <json j, RegistrationRequest req> jsonToRegRequest() {
    var transactionId, _ = (string)j.transactionId;
    var participantId, _ = (string)j.participantId;
    req.transactionId = transactionId;
    req.participantId = participantId;
    Protocol[] protocols = j.participantProtocols.map(
                                                 function (json proto) returns (Protocol p) {
                                                     var name, _ = (string)proto.name;
                                                     var url, _ = (string)proto.url;
                                                     p = {name:name, url:url};
                                                     return;
                                                 });
    req.participantProtocols = protocols;
}

struct RegistrationResponse {
    string transactionId;
    Protocol[] coordinatorProtocols;
}

transformer <RegistrationResponse res, json j> regResposeToJson () {
    j.transactionId = res.transactionId;
    json[] protocols = res.coordinatorProtocols.map(
                                               function (Protocol proto) returns (json) {
                                                   json j2 = {name:proto.name, url:proto.url};
                                                   return j2;
                                               });
    j.coordinatorProtocols = protocols;
}

transformer <json j, RegistrationResponse res> jsonToRegResponse () {
    var transactionId, _ = (string)j.transactionId;
    res.transactionId = transactionId;
    Protocol[] protocols = j.coordinatorProtocols.map(
                                                 function (json proto) returns (Protocol p) {
                                                     var name, _ = (string)proto.name;
                                                     var url, _ = (string)proto.url;
                                                     p = {name:name, url:url};
                                                     return;
                                                 });
    res.coordinatorProtocols = protocols;
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

function respondToBadRequest (string msg) returns (http:Response res) {
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

        initiatedTransactions.put(txnId, txn);
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
    var txn, _ = (TwoPhaseCommitTransaction)initiatedTransactions.get(transactionId);
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
        participatedTransactions.put(participatedTxnId, twopcTxn);
        txnCtx = {transactionId:transactionId, transactionBlockId:transactionBlockId,
                     coordinationType:"2pc", registerAtURL:registerAtURL};
        log:printInfo("Registered local participant: " + participantId + " for transaction:" + transactionId);
    }
    return;
}

function localParticipantProtocolFn (string transactionId,
                                     int transactionBlockId,
                                     string protocolAction) returns (boolean successful) {
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
    var txn, _ = (TwoPhaseCommitTransaction)participatedTransactions.get(participatedTxnId);
    if(txn == null) {
        successful = false;
        return;
    }
    if (protocolAction == "prepare") {
        if (txn.state == TransactionState.ABORTED) {
            participatedTransactions.remove(participatedTxnId);
        } else {
            successful = prepareResourceManagers(transactionId, transactionBlockId);
            if (successful) {
                txn.state = TransactionState.PREPARED;
            }
            successful = true;
        }
    } else if (protocolAction == "notifycommit") {
        if (txn.state == TransactionState.PREPARED) {
            successful = commitResourceManagers(transactionId, transactionBlockId);
            participatedTransactions.remove(participatedTxnId);
            successful = true;
        }
    } else if (protocolAction == "notifyabort") {
        if (txn.state == TransactionState.PREPARED) {
            successful = abortResourceManagers(transactionId, transactionBlockId);
            participatedTransactions.remove(participatedTxnId);
            successful = true;
        }
    }
    return;
}

function getInitiatorClientEP(string registerAtURL) returns (InitiatorClientEP) {
    var initiatorEP, cacheErr = (InitiatorClientEP)httpClientCache.get(registerAtURL);
    if (cacheErr != null) {
        throw cacheErr; // We can't continue due to a programmer error
    }
    if (initiatorEP == null) {
        initiatorEP = {};
        InitiatorClientConfig config = {registerAtURL: registerAtURL,
                                           endpointTimeout: 120000, retryConfig:{count:5, interval:5000}};
        initiatorEP.init (config);
        httpClientCache.put(registerAtURL, initiatorEP);
    }
    return initiatorEP;
}

documentation {
    Registers a participant with the initiator's coordinator. This function will be called by the participant

    P{{transactionId}} - ID of the transaction to which this participant is registering with
    P{{transactionBlockId}} - The local ID of the transaction block on the participant
    P{{registerAtURL}} - The URL of the initiator to which this participant will register with
    R{{txnCtx}} - The transaction context which will be created on successfully registering with the initiator
    R{{err}} - The error which will be returned if registration fails
}
function registerParticipantWithRemoteInitiator (string transactionId,
                                                 int transactionBlockId,
                                                 string registerAtURL) returns (TransactionContext txnCtx, error err) {
    endpoint InitiatorClientEP initiatorEP;
    initiatorEP = getInitiatorClientEP(registerAtURL);
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);

    // Register with the coordinator only if the participant has not already done so
    if (participatedTransactions.hasKey(participatedTxnId)) {
        log:printError("Already registered with initiator for transaction:" + participatedTxnId);
        return;
    }
    log:printInfo("Registering for transaction: " + participatedTxnId + " with coordinator: " + registerAtURL);
    var regRes, e = initiatorEP -> register(transactionId, transactionBlockId);
    if (e != null) {
        string msg = "Cannot register with coordinator for transaction: " + transactionId;
        log:printErrorCause(msg, e);
        err = {message:msg};
    } else {
        Protocol[] coordinatorProtocols = regRes.coordinatorProtocols;
        TwoPhaseCommitTransaction twopcTxn = {transactionId:transactionId, coordinationType:TWO_PHASE_COMMIT};
        twopcTxn.coordinatorProtocols = coordinatorProtocols;
        participatedTransactions.put(participatedTxnId, twopcTxn);
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
