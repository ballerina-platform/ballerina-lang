// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// The participant ID is of this participant
string participantId = util:uuid();

map transactions = {};

struct Transaction {
    string transactionId;
    string coordinationType = "2pc";
    map participants;
}

struct Participant {
    string participantId;
    Protocol[] participantProtocols;
    boolean isInitiator;
}

struct TransactionContext {
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

function createTransaction (string coordinationType) returns (Transaction txn) {
    if (coordinationType == TWO_PHASE_COMMIT) {
        TwoPhaseCommitTransaction twopcTxn = {transactionId:util:uuid(), coordinationType:TWO_PHASE_COMMIT};
        var tx, _ = (Transaction)twopcTxn;
        txn = tx;
    } else {
        error e = {msg:"Unknown coordination type: " + coordinationType};
        throw e;
    }
    return;
}

// The initiator will create a new transaction context by calling this function
function createTransactionContext (string coordinationType) returns (TransactionContext txnContext, error e) {
    if (!isValidCoordinationType(coordinationType)) {
        string msg = "Invalid-Coordination-Type:" + coordinationType;
        log:printError(msg);
        e = {msg:msg};
    } else {
        Transaction txn = createTransaction(coordinationType);
        string txnId = txn.transactionId;

        // Add the map of participants for the transaction with ID tid to the transactions map
        transactions[txnId] = txn;
        txnContext = {transactionId:txnId,
                         coordinationType:coordinationType,
                         registerAtURL:"http://" + coordinatorHost + ":" + coordinatorPort + basePath + registrationPath};

        log:printInfo("Created transaction: " + txnId);
    }
    return;
}

// Registers a participant with the initiator's coordinator
function registerParticipant (string transactionId, string registerAtURL) returns (error err) {
    endpoint<InitiatorCoordinatorClient> coordinatorEP {
        create InitiatorCoordinatorClient();
    }

    // Register with the coordinator only if you have not already done so
    if (transactions[transactionId] != null) {
        return;
    }
    log:printInfo("Registering for transaction: " + transactionId + " with coordinator: " + registerAtURL);
    var j, e = coordinatorEP.register(transactionId, participantId, registerAtURL);
    println(j);
    if (e != null) {
        string msg = "Cannot register with coordinator for transaction: " + transactionId;
        log:printErrorCause(msg, e);
        err = {msg: msg};
    } else {
        log:printInfo("Registered with coordinator for transaction: " + transactionId);
    }
    return;
}
