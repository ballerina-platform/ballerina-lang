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

const string PROTOCOL_COMPLETION = "completion";
const string PROTOCOL_VOLATILE = "volatile";
const string PROTOCOL_DURABLE = "durable";

enum Protocols {
    COMPLETION, DURABLE, VOLATILE
}

enum TransactionState {
    ACTIVE, PREPARED, COMMITTED, ABORTED
}

struct TwoPhaseCommitTransaction {
    string transactionId;
    string coordinationType = "2pc";
    map participants;
    Protocol[] coordinatorProtocols;
    TransactionState state;
    boolean possibleMixedOutcome;
}

struct CommitRequest {
    string transactionId;
}

struct CommitResponse {
    string message;
}

struct PrepareRequest {
    string transactionId;
}

struct PrepareResponse {
    string message;
}

struct NotifyRequest {
    string transactionId;
    string message;
}

struct NotifyResponse {
    string message;
}

struct AbortRequest {
    string transactionId;
    string participantId;
}

struct AbortResponse {
    string message;
}

function twoPhaseCommit (TwoPhaseCommitTransaction txn) returns (string message, error err) {
    log:printInfo("Running 2-phase commit for transaction: " + txn.transactionId);

    string transactionId = txn.transactionId;

    // Prepare local resource managers
    boolean localPrepareSuccessful = prepareResourceManagers(transactionId);
    if (!localPrepareSuccessful) {
        err = {message:"Local prepare failed"};
        return;
    }

    // Prepare phase & commit phase
    // First call prepare on all volatile participants
    boolean prepareVolatilesSuccessful = prepareParticipants(txn, PROTOCOL_VOLATILE);
    if (prepareVolatilesSuccessful) {
        // if all volatile participants voted YES, Next call prepare on all durable participants
        boolean prepareDurablesSuccessful = prepareParticipants(txn, PROTOCOL_DURABLE);
        if (prepareDurablesSuccessful) {
            // If all durable participants voted YES (PREPARED or READONLY), next call notify(commit) on all
            // (durable & volatile) participants and return committed to the initiator
            boolean notifyCommitSuccessful = notify(txn, "commit");
            if (notifyCommitSuccessful) {
                message = "committed";
            } else {
                // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                err = {message:"Hazard-Outcome"};
            }
            boolean localCommitSuccessful = commitResourceManagers(transactionId);
            if (!localCommitSuccessful) {
                err = {message:"Local commit failed"};
            }
        } else {
            // If some durable participants voted NO, next call notify(abort) on all durable participants
            // and return aborted to the initiator
            boolean notifyAbortSuccessful = notify(txn, "abort");
            if (notifyAbortSuccessful) {
                if (txn.possibleMixedOutcome) {
                    message = "mixed";
                } else {
                    message = "aborted";
                }
            } else {
                // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                err = {message:"Hazard-Outcome"};
            }
            boolean localAbortSuccessful = abortResourceManagers(transactionId);
            if (!localAbortSuccessful) {
                err = {message:"Local abort failed"};
            }
        }
    } else {
        boolean notifySuccessful = notifyAbortToVolatileParticipants(txn);
        if (notifySuccessful) {
            if (txn.possibleMixedOutcome) {
                message = "mixed";
            } else {
                message = "aborted";
            }
        } else {
            // return Hazard outcome if a participant cannot successfully end its branch of the transaction
            err = {message:"Hazard-Outcome"};
        }
        boolean localAbortSuccessful = abortResourceManagers(transactionId);
        if (!localAbortSuccessful) {
            err = {message:"Local abort failed"};
        }
    }
    return;
}

function notifyAbortToVolatileParticipants (TwoPhaseCommitTransaction txn) returns (boolean successful) {
    map participants = txn.participants;
    foreach _, p in participants {
        var participant, _ = (Participant)p;
        Protocol[] protocols = participant.participantProtocols;
        if (protocols != null) {
            foreach proto in protocols {
                if (proto.name == PROTOCOL_VOLATILE) {
                    var _, e = notifyParticipant(txn, participant, "abort");
                    if (e != null) {
                        successful = false;
                        return;
                    }
                }
            }
        }
    }
    successful = true;
    return;
}

function notifyAbort (TwoPhaseCommitTransaction txn) returns (string message, error err) {
    map participants = txn.participants;
    message = "aborted";
    foreach _, p in participants {
        var participant, _ = (Participant)p;
        Protocol[] protocols = participant.participantProtocols;
        if (protocols != null) {
            foreach proto in protocols {
                var status, e = notifyParticipant(txn, participant, "abort");
                if (e != null) {
                    err = {message:"Hazard-Outcome"};
                    return;
                } else if (status == "committed") {
                    txn.possibleMixedOutcome = true;
                    message = "mixed";
                    return;
                }
            }
        }
    }
    return;
}

function prepareParticipants (TwoPhaseCommitTransaction txn, string protocol) returns (boolean successful) {
    foreach _, p in txn.participants {
        var participant, _ = (Participant)p;
        Protocol[] protocols = participant.participantProtocols;
        if (protocols != null) {
            foreach proto in protocols {
                if (proto.name == protocol) {
                    if (!prepareParticipant(txn, participant, proto.url)) {
                        successful = false;
                        return;
                    }
                }
            }
        }
    }
    successful = true;
    return;
}

function prepareParticipant (TwoPhaseCommitTransaction txn,
                             Participant participant, string protocolURL) returns (boolean successful) {
    endpoint<Participant2pcClient> participantEP {
        create Participant2pcClient(protocolURL);
    }
    string transactionId = txn.transactionId;
    // Let's set this to true and change it to false only if a participant aborted or an error occurred while trying
    // to prepare a participant
    successful = true;
    string participantId = participant.participantId;

    log:printInfo("Preparing participant: " + participantId);
    // If a participant voted NO then abort
    var status, e = participantEP.prepare(transactionId);
    if (e != null || status == "aborted") {
        log:printInfo("Participant: " + participantId + " failed or aborted");
        // Remove the participant who sent the abort since we don't want to do a notify(Abort) to that
        // participant
        txn.participants.remove(participantId);
        successful = false;
    } else if (status == "committed") {
        log:printInfo("Participant: " + participantId + " committed");
        // If one or more participants returns "committed" and the overall prepare fails, we have to
        // report a mixed-outcome to the initiator
        txn.possibleMixedOutcome = true;
        // Don't send notify to this participant because it is has already committed. We can forget about this participant.
        txn.participants.remove(participantId);
    } else if (status == "read-only") {
        log:printInfo("Participant: " + participantId + " read-only");
        // Don't send notify to this participant because it is read-only. We can forget about this participant.
        txn.participants.remove(participantId);
    } else {
        log:printInfo("Participant: " + participantId + ", status: " + status);
    }
    return;
}

function notify (TwoPhaseCommitTransaction txn, string message) returns (boolean successful) {
    foreach _, p in txn.participants {
        var participant, _ = (Participant)p;
        Protocol[] protocols = participant.participantProtocols;
        if (protocols != null) {
            foreach proto in protocols {
                var _, e = notifyParticipant(txn, participant, message);
                if (e != null) {
                    successful = false;
                    return;
                }
            }
        }
    }
    successful = true;
    return;
}

function notifyParticipant (TwoPhaseCommitTransaction txn,
                            Participant participant, string message) returns (string status, error err) {
    endpoint<Participant2pcClient> participantEP {
    }

    string participantId = participant.participantId;
    string transactionId = txn.transactionId;
    log:printInfo("Notify(" + message + ") participant: " + participantId);

    foreach protocol in participant.participantProtocols {
        string protoURL = protocol.url;
        Participant2pcClient participant2pcClient = create Participant2pcClient(protoURL);
        bind participant2pcClient with participantEP;
        var notificationStatus, participantErr, communicationErr = participantEP.notify(transactionId, message);
        status = notificationStatus;
        if (communicationErr != null) {
            if (message != "abort") {
                err = communicationErr;
            }
            log:printErrorCause("Communication error occurred while notify(" + message + ") participant: " + protoURL +
                                " for transaction: " + transactionId, communicationErr);
        } else if (participantErr != null) { // participant may return "Transaction-Unknown", "Not-Prepared" or "Failed-EOT"
            log:printErrorCause("Participant replied with an error", participantErr);
            err = participantErr;
        } else if (notificationStatus == "aborted") {
            log:printInfo("Participant: " + participantId + " aborted");
        } else if (notificationStatus == "committed") {
            log:printInfo("Participant: " + participantId + " committed");
        }
    }
    return;
}

// This function will be called by the initiator
function commitTransaction (string transactionId) returns (string message, error e) {
    var txn, _ = (TwoPhaseCommitTransaction)initiatedTransactions[transactionId];
    if (txn == null) {
        string msg = "Transaction-Unknown. Invalid TID:" + transactionId;
        log:printError(msg);
        e = {message:msg};
    } else {
        log:printInfo("Committing transaction: " + transactionId);
        // return response to the initiator. ( Committed | Aborted | Mixed )
        var msg, err = twoPhaseCommit(txn);
        if (err == null) {
            message = msg;
        } else {
            e = err;
        }
    }
    return;
}

// This function will be called by the initiator
function abortInitiatorTransaction (string transactionId) returns (string message, error e) {
    var txn, _ = (TwoPhaseCommitTransaction)initiatedTransactions[transactionId];
    if (txn == null) {
        string msg = "Transaction-Unknown. Invalid TID:" + transactionId;
        log:printError(msg);
        e = {message:msg};
    } else {
        log:printInfo("Aborting transaction: " + transactionId);
        // return response to the initiator. ( Aborted | Mixed )
        var msg, err = notifyAbort(txn);
        if (err == null) {
            message = msg;
            txn.state = TransactionState.ABORTED;
        } else {
            e = err;
        }
        boolean localAbortSuccessful = abortResourceManagers(transactionId);
        if(!localAbortSuccessful) {
            log:printError("Aborting local resource managers failed");
        }
    }
    return;
}

// The participant should notify the initiator that it aborted
// This function is called by the participant
function abortLocalParticipantTransaction (string transactionId) returns (string message, error e) {
    endpoint<Initiator2pcClient> coordinatorEP {
        create Initiator2pcClient();
    }
    var txn, _ = (TwoPhaseCommitTransaction)participatedTransactions[transactionId];
    if (txn == null) {
        string msg = "Transaction-Unknown. Invalid TID:" + transactionId;
        log:printError(msg);
        e = {message:msg};
    } else {
        message, e = coordinatorEP.abortTransaction(transactionId, txn.coordinatorProtocols[0].url);
        if (e == null) {
            txn.state = TransactionState.ABORTED;
        }
    }
    return;
}

// If this is a new transaction, then this instance will become the initiator and will create a context
// If this is part of an existing transaction, this this instance will register as a participant
function beginTransaction (string transactionId, string registerAtUrl,
                           string coordinationType) returns (TransactionContext txnCtx, error err) {
    if (transactionId == null) {
        txnCtx, err = createTransactionContext(coordinationType);
    } else {
        err = registerParticipantWithRemoteCoordinator(transactionId, registerAtUrl);
        if (err == null) {
            txnCtx = {transactionId:transactionId, coordinationType:coordinationType, registerAtURL:registerAtUrl};
        }
    }
    return;
}

// Depending on the state of the transaction, and whether this instance is the initiator or participant,
// decide to commit or abort the transaction
function endTransaction (string transactionId) returns (string msg, error e) {
    if (initiatedTransactions.hasKey(transactionId)) {
        var txn, _ = (TwoPhaseCommitTransaction)initiatedTransactions[transactionId];
        if (txn.state != TransactionState.ABORTED) {
            msg, e = commitTransaction(transactionId);
            if (e == null) {
                initiatedTransactions.remove(transactionId);
            }
        }
    } // Nothing to do on endTransaction if you are a participant
    return;
}

function abortTransaction (string transactionId) returns (string msg, error e) {
    if (initiatedTransactions.hasKey(transactionId)) {
        msg, e = abortInitiatorTransaction(transactionId);
    } else {
        msg, e = abortLocalParticipantTransaction(transactionId);
    }
    if (e == null) {
        initiatedTransactions.remove(transactionId);
    }

    return;
}

native function prepareResourceManagers (string transactionId) returns (boolean prepareSuccessful);

native function commitResourceManagers (string transactionId) returns (boolean commitSuccessful);

native function abortResourceManagers (string transactionId) returns (boolean abortSuccessful);

