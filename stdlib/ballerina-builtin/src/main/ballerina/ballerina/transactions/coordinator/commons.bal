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

import ballerina.net.http;
import ballerina.util;
import ballerina.log;

const string TRANSACTION_CONTEXT_VERSION = "1.0";

map transactions = {};

public struct Transaction {
    string transactionId;
    string coordinationType = "2pc";
    map participants;
}

public struct Participant {
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

public struct Protocol {
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

public function respondToBadRequest (string msg) returns (http:OutResponse res) {
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

function createContext (string participantId, string coordinationType) returns (TransactionContext txnContext,
                                                                                error e) {
    if (!isValidCoordinationType(coordinationType)) {
        string msg = "Invalid-Coordination-Type:" + coordinationType;
        log:printError(msg);
        e = {msg:msg};
    } else {
        Transaction txn = createTransaction(coordinationType);
        //TODO: We may not need to make the initiator a participant
        Participant participant = {participantId:participantId, isInitiator:true};
        txn.participants = {};

        // Add the initiator, who is also the first participant
        txn.participants[participant.participantId] = participant;

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
