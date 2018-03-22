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

import ballerina/log;

const string PROTOCOL_COMPLETION = "completion";
const string PROTOCOL_VOLATILE = "volatile";
const string PROTOCOL_DURABLE = "durable";

enum Protocols {
    COMPLETION, DURABLE, VOLATILE
}

public enum TransactionState {
    ACTIVE, PREPARED, COMMITTED, ABORTED
}

struct TwoPhaseCommitTransaction {
    string transactionId;
    string coordinationType = "2pc";
    map<Participant> participants;
    Protocol[] coordinatorProtocols;
    TransactionState state;
    boolean possibleMixedOutcome;
}

public struct PrepareRequest {
    string transactionId;
}

public struct PrepareResponse {
    string message;
}

public struct NotifyRequest {
    string transactionId;
    string message;
}

public struct NotifyResponse {
    string message;
}

function twoPhaseCommit (TwoPhaseCommitTransaction txn, int transactionBlockId) returns string|error {
    log:printInfo("Running 2-phase commit for transaction: " + txn.transactionId);
    string|error ret = "";
    string transactionId = txn.transactionId;

    // Prepare local resource managers
    boolean localPrepareSuccessful = prepareResourceManagers(transactionId, transactionBlockId);
    if (!localPrepareSuccessful) {
        ret = {message:"Local prepare failed"};
        return ret;
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
                ret = "committed";
            } else {
                // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                ret = {message:"Hazard-Outcome"};
            }
            boolean localCommitSuccessful = commitResourceManagers(transactionId, transactionBlockId);
            if (!localCommitSuccessful) {
                ret = {message:"Local commit failed"};
            }
        } else {
            // If some durable participants voted NO, next call notify(abort) on all durable participants
            // and return aborted to the initiator
            boolean notifyAbortSuccessful = notify(txn, "abort");
            if (notifyAbortSuccessful) {
                if (txn.possibleMixedOutcome) {
                    ret = "mixed";
                } else {
                    ret = "aborted";
                }
            } else {
                // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                ret = {message:"Hazard-Outcome"};
            }
            boolean localAbortSuccessful = abortResourceManagers(transactionId, transactionBlockId);
            if (!localAbortSuccessful) {
                ret = {message:"Local abort failed"};
            }
        }
    } else {
        boolean notifySuccessful = notifyAbortToVolatileParticipants(txn);
        if (notifySuccessful) {
            if (txn.possibleMixedOutcome) {
                ret = "mixed";
            } else {
                ret = "aborted";
            }
        } else {
            // return Hazard outcome if a participant cannot successfully end its branch of the transaction
            ret = {message:"Hazard-Outcome"};
        }
        boolean localAbortSuccessful = abortResourceManagers(transactionId, transactionBlockId);
        if (!localAbortSuccessful) {
            ret = {message:"Local abort failed"};
        }
    }
    return ret;
}

function notifyAbortToVolatileParticipants (TwoPhaseCommitTransaction txn) returns boolean {
    map<Participant> participants = txn.participants;
    foreach _, participant in participants {
        Protocol[] protocols = participant.participantProtocols;
        foreach proto in protocols {
            if (proto.name == PROTOCOL_VOLATILE) {
                var ret = notifyRemoteParticipant(txn, participant, "abort");
                match ret {
                    error err => return false;
                    string s => return true;
                }
            }
        }
    }
    return true;
}

function notifyAbort (TwoPhaseCommitTransaction txn) returns string|error {
    map<Participant> participants = txn.participants;
    string|error ret = "aborted";
    foreach _, participant in participants {
        Protocol[] protocols = participant.participantProtocols;
        foreach proto in protocols {
            match proto.protocolFn {
                (function (string, int, string) returns boolean) protocolFn => {
                // if the participant is a local participant, i.e. protoFn is set, then call that fn
                    log:printInfo("Notify(abort) local participant: " + participant.participantId);
                    boolean successful = protocolFn(txn.transactionId, proto.transactionBlockId, "notifyabort");
                    if (!successful) {
                        error e = {message:"Hazard-Outcome"}; //TODO: Must set this for the entire transaction and not override during loop execution
                        ret = e;
                    }
                }
                any|null => {
                    var result = notifyRemoteParticipant(txn, participant, "abort");
                    match result {
                        string status => {
                            if (status == "committed") {
                                txn.possibleMixedOutcome = true;
                                ret = "mixed"; //TODO: Must set this for the entire transaction and not override during loop execution
                            }
                        }
                        error err => {
                            error e = {message:"Hazard-Outcome"}; //TODO: Must set this for the entire transaction and not override during loop execution
                            ret = e;
                        }
                    }
                }
            }
        }
    }
    return ret;
}

function prepareParticipants (TwoPhaseCommitTransaction txn, string protocol) returns boolean {
    boolean successful = true;
    foreach _, participant in txn.participants {
        Protocol[] protocols = participant.participantProtocols;
        foreach proto in protocols {
            if (proto.name == protocol) {
                match proto.protocolFn {
                    (function (string, int, string) returns boolean) protocolFn => {
                    // if the participant is a local participant, i.e. protoFn is set, then call that fn
                        log:printInfo("Preparing local participant: " + participant.participantId);
                        if (!protocolFn(txn.transactionId, proto.transactionBlockId, "prepare")) {
                            successful = false;
                        }
                    }
                    any|null => {
                        if (!prepareRemoteParticipant(txn, participant, proto.url)) {
                            successful = false;
                        }
                    }
                }
            }
        }
    }
    return successful;
}

function getParticipant2pcClientEP (string participantURL) returns Participant2pcClientEP {
    if (httpClientCache.hasKey(participantURL)) {
        var participantEP =? <Participant2pcClientEP>httpClientCache.get(participantURL);
        return participantEP;
    } else {
        Participant2pcClientEP participantEP = {};
        Participant2pcClientConfig config = {participantURL:participantURL,
                                                endpointTimeout:120000, retryConfig:{count:5, interval:5000}};
        participantEP.init(config);
        httpClientCache.put(participantURL, participantEP);
        return participantEP;
    }
}

function prepareRemoteParticipant (TwoPhaseCommitTransaction txn,
                                   Participant participant, string protocolUrl) returns boolean {
    endpoint Participant2pcClientEP participantEP;
    participantEP = getParticipant2pcClientEP(protocolUrl);

    string transactionId = txn.transactionId;
    // Let's set this to true and change it to false only if a participant aborted or an error occurred while trying
    // to prepare a participant
    boolean successful = true;
    string participantId = participant.participantId;

    log:printInfo("Preparing remote participant: " + participantId);
    // If a participant voted NO or failed then abort
    var result = participantEP -> prepare(transactionId);
    match result {
        error err => {
            log:printErrorCause("Remote participant: " + participantId + " failed", err);
            boolean participantRemoved = txn.participants.remove(participantId);
            if (!participantRemoved) {
                log:printError("Could not remove failed participant: " +
                               participantId + " from transaction: " + transactionId);
            }
            successful = false;
        }
        string status => {
            if (status == "aborted") {
                log:printInfo("Remote participant: " + participantId + " aborted.");
                // Remove the participant who sent the abort since we don't want to do a notify(Abort) to that
                // participant
                boolean participantRemoved = txn.participants.remove(participantId);
                if (!participantRemoved) {
                    log:printError("Could not remove aborted participant: " +
                                   participantId + " from transaction: " + transactionId);
                }
                successful = false;
            } else if (status == "committed") {
                log:printInfo("Remote participant: " + participantId + " committed");
                // If one or more participants returns "committed" and the overall prepare fails, we have to
                // report a mixed-outcome to the initiator
                txn.possibleMixedOutcome = true;
                // Don't send notify to this participant because it is has already committed. We can forget about this participant.
                boolean participantRemoved = txn.participants.remove(participantId);
                if (!participantRemoved) {
                    log:printError("Could not remove committed participant: " +
                                   participantId + " from transaction: " + transactionId);
                }
            } else if (status == "read-only") {
                log:printInfo("Remote participant: " + participantId + " read-only");
                // Don't send notify to this participant because it is read-only. We can forget about this participant.
                boolean participantRemoved = txn.participants.remove(participantId);
                if (!participantRemoved) {
                    log:printError("Could not remove read-only participant: " +
                                   participantId + " from transaction: " + transactionId);
                }
            } else if (status == "prepared") {
                log:printInfo("Remote participant: " + participantId + " prepared");
            } else {
                log:printInfo("Remote participant: " + participantId + ", status: " + status);
                successful = false;
            }
        }
    }
    return successful;
}

function notify (TwoPhaseCommitTransaction txn, string message) returns boolean {
    boolean successful = true;
    foreach _, participant in txn.participants {
        Protocol[] protocols = participant.participantProtocols;
        foreach proto in protocols {
            match proto.protocolFn {
                (function (string, int, string) returns boolean) protocolFn => {
                // if the participant is a local participant, i.e. protoFn is set, then call that fn
                    log:printInfo("Notify(" + message + ") local participant: " + participant.participantId);
                    if (!protocolFn(txn.transactionId, proto.transactionBlockId, "notify" + message)) {
                        successful = false;
                    }
                }
                any|null => {
                    var result = notifyRemoteParticipant(txn, participant, message);
                    match result {
                        error err => successful = false;
                        string s => successful = true;
                    }
                }
            }
        }
    }
    return successful;
}

function notifyRemoteParticipant (TwoPhaseCommitTransaction txn,
                                  Participant participant, string message) returns string|error {
    endpoint Participant2pcClientEP participantEP;

    string|error ret = "";
    string participantId = participant.participantId;
    string transactionId = txn.transactionId;
    log:printInfo("Notify(" + message + ") remote participant: " + participantId);

    foreach protocol in participant.participantProtocols {
        string protoURL = protocol.url;
        participantEP = getParticipant2pcClientEP(protoURL);
        var result = participantEP -> notify(transactionId, message);
        match result {
            error err => {
                log:printErrorCause("Remote participant: " + participantId + " replied with an error", err);
                ret = err;
            }
            string notificationStatus => {
                if (notificationStatus == "aborted") {
                    log:printInfo("Remote participant: " + participantId + " aborted");
                } else if (notificationStatus == "committed") {
                    log:printInfo("Remote participant: " + participantId + " committed");
                }
                ret = notificationStatus;
            }
        }
    }
    return ret;
}

// This function will be called by the initiator
function commitTransaction (string transactionId, int transactionBlockId) returns string|error {
    if (!initiatedTransactions.hasKey(transactionId)) {
        string msg = "Transaction-Unknown. Invalid TID:" + transactionId;
        log:printError(msg);
        error err = {message:msg};
        return err;
    } else {
        var txn =? <TwoPhaseCommitTransaction>initiatedTransactions[transactionId];
        log:printInfo("Committing transaction: " + transactionId);
        // return response to the initiator. ( Committed | Aborted | Mixed )
        return twoPhaseCommit(txn, transactionBlockId);
    }
}

// This function will be called by the initiator
function abortInitiatorTransaction (string transactionId, int transactionBlockId) returns string|error {
    string|error ret = "";
    if (!initiatedTransactions.hasKey(transactionId)) {
        string msg = "Transaction-Unknown. Invalid TID:" + transactionId;
        log:printError(msg);
        error err = {message:msg};
        return err;
    } else {
        var txn =? <TwoPhaseCommitTransaction>initiatedTransactions[transactionId];
        log:printInfo("Aborting transaction: " + transactionId);
        // return response to the initiator. ( Aborted | Mixed )
        ret = notifyAbort(txn);
        txn.state = TransactionState.ABORTED;
        boolean localAbortSuccessful = abortResourceManagers(transactionId, transactionBlockId);
        if (!localAbortSuccessful) {
            log:printError("Aborting local resource managers failed");
        } else {
            removeInitiatedTransaction(transactionId);
        }
    }
    return ret;
}

documentation {
    The participant should notify the initiator that it aborted. This function is called by the participant.
    The initiator is remote.

    P{{transactionId}} - Transaction ID
    P{{transactionBlockId}} - Transaction block ID. Each transaction block in a process will have a unique ID.
}
function abortLocalParticipantTransaction (string transactionId, int transactionBlockId) returns string|error {
    string|error ret = "";
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
    if (!participatedTransactions.hasKey(participatedTxnId)) {
        string msg = "Transaction-Unknown. Invalid TID:" + transactionId;
        log:printError(msg);
        error err = {message:msg};
        return err;
    } else {
        var txn =? <TwoPhaseCommitTransaction>participatedTransactions[participatedTxnId];
        boolean successful = abortResourceManagers(transactionId, transactionBlockId);
        if (successful) {
            txn.state = TransactionState.ABORTED;
            log:printInfo("Local participant aborted transaction: " + participatedTxnId);
        } else {
            string msg = "Aborting local resource managers failed for transaction:" + participatedTxnId;
            log:printError(msg);
            ret = {message:msg};
        }
    }
    return ret;
}
