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
    return participants[participantId] != null;
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

function getCoordinatorProtocolAt (string protocol) returns (string coordinatorProtocolAt) {
    coordinatorProtocolAt = "http://" + coordinatorHost + ":" + coordinatorPort + initiator2pcCoordinatorBasePath;
    return;
}

function getParticipantProtocolAt(string protocol) returns (string coordinatorProtocolAt) {
    coordinatorProtocolAt = "http://" + coordinatorHost + ":" + coordinatorPort + participant2pcCoordinatorBasePath;
    return;
}

// The initiator will create a new transaction context by calling this function
function createTransactionContext (string coordinationType) returns (TransactionContext txnContext, error e) {
    if (!isValidCoordinationType(coordinationType)) {
        string msg = "Invalid-Coordination-Type:" + coordinationType;
        log:printError(msg);
        e = {message:msg};
    } else {
        Transaction txn = createNewTransaction(coordinationType);
        string txnId = txn.transactionId;

        initiatedTransactions[txnId] = txn;
        txnContext = {transactionId:txnId,
                         coordinationType:coordinationType,
                         registerAtURL:"http://" + coordinatorHost + ":" + coordinatorPort +
                                       initiatorCoordinatorBasePath + registrationPath};

        log:printInfo("Created transaction: " + txnId);
    }
    return;
}

// Registers a participant with the initiator's coordinator
// This function will be called by the participant
function registerParticipantWithRemoteCoordinator (string transactionId, string registerAtURL) returns (error err) {
    endpoint<InitiatorClient> coordinatorEP {
        create InitiatorClient(registerAtURL);
    }

    // Register with the coordinator only if the participant has not already done so
    if (participatedTransactions[transactionId] != null) {
        return;
    }
    log:printInfo("Registering for transaction: " + transactionId + " with coordinator: " + registerAtURL);
    var regRes, e = coordinatorEP.register(transactionId);
    if (e != null) {
        string msg = "Cannot register with coordinator for transaction: " + transactionId;
        log:printErrorCause(msg, e);
        err = {message:msg};
    } else {
        Protocol[] coordinatorProtocols = regRes.coordinatorProtocols;
        TwoPhaseCommitTransaction twopcTxn = {transactionId:transactionId, coordinationType:TWO_PHASE_COMMIT};
        twopcTxn.coordinatorProtocols = coordinatorProtocols;
        participatedTransactions[transactionId] = twopcTxn;
        log:printInfo("Registered with coordinator for transaction: " + transactionId);
    }
    return;
}

native function getAvailablePort() returns (int port);

native function getHostAddress() returns (string address);
