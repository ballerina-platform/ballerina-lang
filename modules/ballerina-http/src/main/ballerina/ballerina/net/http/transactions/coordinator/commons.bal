package transactions.coordinator;

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

struct CreateTransactionContextRequest {
    string participantId;
    string coordinationType;
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
    int i = 0;
    while (i < lengthof coordinationTypes) {
        if (coordinationType == coordinationTypes[i]) {
            return true;
        }
        i = i + 1;
    }
    return false;
}

function protocolCompatible (string coordinationType,
                             Protocol[] participantProtocols) returns (boolean participantProtocolIsValid) {
    var validProtocols, _ = (string[])coordinationTypeToProtocolsMap[coordinationType];
    int i = 0;
    while (i < lengthof participantProtocols) {
        int j = 0;
        while (j < lengthof validProtocols) {
            if (participantProtocols[i].name == validProtocols[j]) {
                participantProtocolIsValid = true;
                break;
            } else {
                participantProtocolIsValid = false;
            }
            j = j + 1;
        }
        if (!participantProtocolIsValid) {
            break;
        }
        i = i + 1;
    }
    return participantProtocolIsValid;
}

public function respondToBadRequest (http:Response res, string msg) {
    log:printError(msg);
    res.setStatusCode(400);
    RequestError err = {errorMessage:msg};
    var resPayload, _ = <json>err;
    res.setJsonPayload(resPayload);
}

function createTransaction(string coordinationType) returns (Transaction txn) {
    if(coordinationType == TWO_PHASE_COMMIT) {
        TwoPhaseCommitTransaction twopcTxn = {transactionId: util:uuid(), coordinationType: TWO_PHASE_COMMIT};
        txn = (Transaction) twopcTxn;
    } else {
        error e = {msg: "Unknown coordination type: " + coordinationType};
        throw e;
    }
    return;
}
