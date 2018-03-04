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

import ballerina.log;
import ballerina.net.http;
import ballerina.util;

const string TRANSACTION_CONTEXT_VERSION = "1.0";

// The participant ID of this participant
string localParticipantId = util:uuid();

map initiatedTransactions = {};
map participatedTransactions = {};

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
    string url;
}

struct RegistrationRequest {
    string transactionId;
    string participantId;
    Protocol[] participantProtocols;
}

struct RegistrationResponse {
    string transactionId;
    Protocol[] coordinatorProtocols;
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

function getCoordinatorProtocolAt (string protocol, ) returns (string coordinatorProtocolAt, int transactionBlockId) {
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
                                       initiatorCoordinatorBasePath + "/" + transactionBlockId + "/" + registrationPath};

        log:printInfo("Created transaction: " + txnId);
    }
    return;
}

// Registers a participant with the initiator's coordinator
// This function will be called by the participant
function registerParticipantWithRemoteCoordinator (string transactionId,
                                                   int transactionBlockId,
                                                   string registerAtURL) returns (TransactionContext txnCtx, error err) {
    endpoint<InitiatorClient> coordinatorEP {
        create InitiatorClient(registerAtURL);
    }

    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);

    // Register with the coordinator only if the participant has not already done so
    if (participatedTransactions[participatedTxnId] != null) {
        return;
    }
    log:printInfo("Registering for transaction: " + participatedTxnId + " with coordinator: " + registerAtURL);
    var regRes, e = coordinatorEP.register(transactionId, transactionBlockId);
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
                     coordinationType:coordinationType, registerAtURL:registerAtUrl};
        log:printInfo("Registered with coordinator for transaction: " + transactionId);
    }
    return;
}

function getParticipatedTransactionId (string transactionId, int transactionBlockId) returns (string id) {
    id = transactionId + ":" + transactionBlockId;
    return;
}

native function getAvailablePort () returns (int port);

native function getHostAddress () returns (string address);
