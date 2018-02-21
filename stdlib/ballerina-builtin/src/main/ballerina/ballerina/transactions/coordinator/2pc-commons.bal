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
}

struct AbortResponse {
    string message;
}

function twoPhaseCommit (TwoPhaseCommitTransaction txn) returns (string message, error err) {
    log:printInfo("Running 2-phase commit for transaction: " + txn.transactionId);

    var volatileEndpoints, durableEndpoints = getVolatileAndDurableEndpoints(txn);

    // Prepare phase & commit phase
    // First call prepare on all volatile participants
    boolean prepareVolatilesSuccessful = prepare(txn, volatileEndpoints);
    if (prepareVolatilesSuccessful) {
        // if all volatile participants voted YES, Next call prepare on all durable participants
        boolean prepareDurablesSuccessful = prepare(txn, durableEndpoints);
        if (prepareDurablesSuccessful) {
            // If all durable participants voted YES (PREPARED or READONLY), next call notify(commit) on all
            // (durable & volatile) participants and return committed to the initiator
            boolean notifyDurablesSuccessful = notify(txn, durableEndpoints, "commit");
            boolean notifyVolatilesSuccessful = notify(txn, volatileEndpoints, "commit");
            if (notifyDurablesSuccessful && notifyVolatilesSuccessful) {
                message = "committed";
            } else {
                // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                err = {msg:"Hazard-Outcome"};
            }
        } else {
            // If some durable participants voted NO, next call notify(abort) on all durable participants
            // and return aborted to the initiator
            boolean notifyDurablesSuccessful = notify(txn, durableEndpoints, "abort");
            boolean notifyVolatilesSuccessful = notify(txn, volatileEndpoints, "abort");
            if (notifyDurablesSuccessful && notifyVolatilesSuccessful) {
                if (txn.possibleMixedOutcome) {
                    message = "mixed";
                } else {
                    message = "aborted";
                }
            } else {
                // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                err = {msg:"Hazard-Outcome"};
            }
        }
    } else {
        boolean notifySuccessful = notify(txn, volatileEndpoints, "abort");
        if (notifySuccessful) {
            if (txn.possibleMixedOutcome) {
                message = "mixed";
            } else {
                message = "aborted";
            }
        } else {
            // return Hazard outcome if a participant cannot successfully end its branch of the transaction
            err = {msg:"Hazard-Outcome"};
        }
    }
    return;
}

function notifyAbort (TwoPhaseCommitTransaction txn) returns (string message, error err) {
    map participants = txn.participants;
    string transactionId = txn.transactionId;
    message = "aborted";
    foreach _, p in participants {
        var participant, _ = (Participant)p;
        Protocol[] protocols = participant.participantProtocols;
        if (protocols != null) {
            foreach proto in protocols {
                var status, e = notifyParticipant(transactionId, proto.url, "abort");
                if (e != null) {
                    err = {msg:"Hazard-Outcome"};
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

function getVolatileAndDurableEndpoints (TwoPhaseCommitTransaction txn) returns
                                                                        (string[] volatileEndpoints,
                                                                         string[] durableEndpoints) {
    volatileEndpoints = [];
    durableEndpoints = [];
    map participants = txn.participants;
    foreach _, p in participants {
        var participant, _ = (Participant)p;
        Protocol[] protocols = participant.participantProtocols;
        if (protocols != null) {
            foreach proto in protocols {
                if (proto.name == PROTOCOL_VOLATILE) {
                    volatileEndpoints[lengthof volatileEndpoints] = proto.url;
                } else if (proto.name == PROTOCOL_DURABLE) {
                    durableEndpoints[lengthof durableEndpoints] = proto.url;
                }
            }
        }
    }
    return;
}

function prepare (TwoPhaseCommitTransaction txn, string[] participantURLs) returns (boolean successful) {
    endpoint<ParticipantClient> participantEP {
    }
    string transactionId = txn.transactionId;
    // Let's set this to true and change it to false only if a participant aborted or an error occurred while trying
    // to prepare a participant
    successful = true;
    foreach participantURL in participantURLs {
        ParticipantClient participantClient = create ParticipantClient();
        bind participantClient with participantEP;

        log:printInfo("Preparing participant: " + participantURL);
        // If a participant voted NO then abort
        var status, e = participantEP.prepare(transactionId, participantURL);
        if (e != null || status == "aborted") {
            log:printInfo("Participant: " + participantURL + " failed or aborted");
            successful = false;
            return;
        } else if (status == "committed") {
            log:printInfo("Participant: " + participantURL + " committed");
            // If one or more participants returns "committed" and the overall prepare fails, we have to
            // report a mixed-outcome to the initiator
            txn.possibleMixedOutcome = true;
            // Don't send notify to this participant because it is has already committed. We can forget about this participant.
            participantURL = null; //TODO: Nulling this out because there is no way to remove an element from an array
        } else if (status == "read-only") {
            log:printInfo("Participant: " + participantURL + " read-only");
            // Don't send notify to this participant because it is read-only. We can forget about this participant.
            participantURL = null; //TODO: Nulling this out because there is no way to remove an element from an array
        } else {
            log:printInfo("Participant: " + participantURL + ", status: " + status);
        }
    }
    return;
}

function notify (TwoPhaseCommitTransaction txn, string[] participantURLs, string message) returns (boolean successful) {
    string transactionId = txn.transactionId;
    successful = true;
    foreach participantURL in participantURLs {
        if (participantURL != null) {
            var _, err = notifyParticipant(transactionId, participantURL, message);
            if (err != null) {
                successful = false;
                return;
            }
        }
    }
    return;
}

function notifyParticipant (string transactionId, string url, string message) returns (string, error) {
    endpoint<ParticipantClient> participantEP {
    }
    ParticipantClient participantClient = create ParticipantClient();
    bind participantClient with participantEP;

    log:printInfo("Notify(" + message + ") participant: " + url);
    var status, participantErr, communicationErr = participantEP.notify(transactionId, url, message);

    error err;
    if (communicationErr != null) {
        if (message != "abort") {
            err = communicationErr;
        }
        log:printErrorCause("Communication error occurred while notify(" + message + ") participant: " + url +
                            " for transaction: " + transactionId, communicationErr);
    } else if (participantErr != null) { // participant may return "Transaction-Unknown", "Not-Prepared" or "Failed-EOT"
        log:printErrorCause("Participant replied with an error", participantErr);
        err = participantErr;
    } else if (status == "aborted") {
        log:printInfo("Participant: " + url + " aborted");
    } else if (status == "committed") {
        log:printInfo("Participant: " + url + " committed");
    }
    return status, err;
}

function commitTransaction (string transactionId) returns (string message, error e) {
    var txn, _ = (TwoPhaseCommitTransaction)transactions[transactionId];
    if (txn == null) {
        string msg = "Transaction-Unknown. Invalid TID:" + transactionId;
        log:printError(msg);
        e = {msg:msg};
    } else {
        log:printInfo("Committing transaction: " + transactionId);
        // return response to the initiator. ( Committed | Aborted | Mixed )
        var msg, err = twoPhaseCommit(txn);
        if (err == null) {
            message = msg;
        } else {
            e = err;
        }
        transactions.remove(transactionId);
    }
    return;
}

function abortTransaction (string transactionId) returns (string message, error e) {
    var txn, _ = (TwoPhaseCommitTransaction)transactions[transactionId];
    if (txn == null) {
        string msg = "Transaction-Unknown. Invalid TID:" + transactionId;
        log:printError(msg);
        e = {msg:msg};
    } else {
        log:printInfo("Aborting transaction: " + transactionId);
        // return response to the initiator. ( Aborted | Mixed )
        var msg, err = notifyAbort(txn);
        if (err == null) {
            message = msg;
        } else {
            e = err;
        }
        transactions.remove(transactionId);
    }
    return;
}
