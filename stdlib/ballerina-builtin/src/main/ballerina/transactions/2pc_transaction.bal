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

package ballerina.transactions;

import ballerina/io;
import ballerina/log;

struct TwoPhaseCommitTransaction {
    string transactionId;
    int transactionBlockId;
    string coordinationType = "2pc";
    boolean isInitiated; // Indicates whether this is a transaction that was initiated or is participated in
    map<Participant> participants;
    Protocol[] coordinatorProtocols;
    int createdTime;
    TransactionState state;
    boolean possibleMixedOutcome;
}

// This function will be called by the initiator
function<TwoPhaseCommitTransaction txn> twoPhaseCommit () returns string|error {
    string transactionId = txn.transactionId;
    int transactionBlockId = txn.transactionBlockId;
    log:printInfo(io:sprintf("Running 2-phase commit for transaction: %s:%d", [transactionId, transactionBlockId]));
    string|error ret = "";

    // Prepare local resource managers
    boolean localPrepareSuccessful = prepareResourceManagers(transactionId, transactionBlockId);
    if (!localPrepareSuccessful) {
        ret = {message:"Local prepare failed"};
        return ret;
    }

    // Prepare phase & commit phase
    // First call prepare on all volatile participants
    boolean prepareVolatilesSuccessful = txn.prepareParticipants(PROTOCOL_VOLATILE);
    if (prepareVolatilesSuccessful) {
        // if all volatile participants voted YES, Next call prepare on all durable participants
        boolean prepareDurablesSuccessful = txn.prepareParticipants(PROTOCOL_DURABLE);
        if (prepareDurablesSuccessful) {
            // If all durable participants voted YES (PREPARED or READONLY), next call notify(commit) on all
            // (durable & volatile) participants and return committed to the initiator
            boolean notifyCommitSuccessful = txn.notify(COMMAND_COMMIT);
            if (notifyCommitSuccessful) {
                ret = OUTCOME_COMMITTED;
            } else {
                // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                ret = {message: OUTCOME_HAZARD};
            }
            boolean localCommitSuccessful = commitResourceManagers(transactionId, transactionBlockId);
            if (!localCommitSuccessful) {
                ret = {message:"Local commit failed"};
            }
        } else {
            // If some durable participants voted NO, next call notify(abort) on all durable participants
            // and return aborted to the initiator
            boolean notifyAbortSuccessful = txn.notify(COMMAND_ABORT);
            if (notifyAbortSuccessful) {
                if (txn.possibleMixedOutcome) {
                    ret = OUTCOME_MIXED;
                } else {
                    ret = OUTCOME_ABORTED;
                }
            } else {
                // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                ret = {message: OUTCOME_HAZARD};
            }
            boolean localAbortSuccessful = abortResourceManagers(transactionId, transactionBlockId);
            if (!localAbortSuccessful) {
                ret = {message: OUTCOME_FAILED_EOT};
            }
        }
    } else {
        boolean notifySuccessful = txn.notifyAbortToVolatileParticipants();
        if (notifySuccessful) {
            if (txn.possibleMixedOutcome) {
                ret = OUTCOME_MIXED;
            } else {
                ret = OUTCOME_ABORTED;
            }
        } else {
            // return Hazard outcome if a participant cannot successfully end its branch of the transaction
            ret = {message: OUTCOME_HAZARD};
        }
        boolean localAbortSuccessful = abortResourceManagers(transactionId, transactionBlockId);
        if (!localAbortSuccessful) {
            ret = {message: OUTCOME_FAILED_EOT};
        }
    }
    return ret;
}

documentation {
    When an abort statement is executed, this function gets called. The transaction in concern will be marked for
    abortion.
}
function<TwoPhaseCommitTransaction txn> markForAbortion () returns string|error {
    string transactionId = txn.transactionId;
    int transactionBlockId = txn.transactionBlockId;
    if(txn.isInitiated) {
        txn.state = TransactionState.ABORTED;
        log:printInfo("Marked initiated transaction for abortion");
    } else { // participant
       boolean successful = abortResourceManagers(transactionId, transactionBlockId);
       string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
       if(successful) {
           txn.state = TransactionState.ABORTED;
           log:printInfo("Marked participated transaction for abort. Transaction:" + participatedTxnId);
       } else {
           string msg = "Aborting local resource managers failed for participated transaction:" + participatedTxnId;
           log:printError(msg);
           error err = {message:msg};
           return err;
       }
    }
    return ""; //TODO: check what will happen if nothing is returned
}

function<TwoPhaseCommitTransaction txn> prepareParticipants (string protocol) returns boolean {
    boolean successful = true;
    foreach _, participant in txn.participants {
        Protocol[] protocols = participant.participantProtocols;
        foreach proto in protocols {
            if (proto.name == protocol) {
                match proto.protocolFn {
                    (function (string, int, string) returns boolean) protocolFn => {
                        // if the participant is a local participant, i.e. protoFn is set, then call that fn
                        log:printInfo("Preparing local participant: " + participant.participantId);
                        if (!protocolFn(txn.transactionId, proto.transactionBlockId, COMMAND_PREPARE)) {
                            // Don't send notify to participants who have failed or rejected prepare
                            boolean removed = txn.participants.remove(participant.participantId);
                            if(!removed){
                                log:printError("Removing participant from transaction failed");
                            }
                            successful = false;
                        }
                    }
                    null => {
                        if (!txn.prepareRemoteParticipant(participant, proto.url)) {
                            successful = false;
                        }
                    }
                }
            }
        }
    }
    return successful;
}

function<TwoPhaseCommitTransaction txn> notifyAbortToVolatileParticipants () returns boolean {
    map<Participant> participants = txn.participants;
    foreach _, participant in participants {
        Protocol[] protocols = participant.participantProtocols;
        foreach proto in protocols {
            if (proto.name == PROTOCOL_VOLATILE) {
                var ret = txn.notifyRemoteParticipant(participant, COMMAND_ABORT);
                match ret {
                    error err => return false;
                    string s => return true;
                }
            }
        }
    }
    return true;
}

function<TwoPhaseCommitTransaction txn> notifyAbort () returns string|error {
    map<Participant> participants = txn.participants;
    string|error ret = OUTCOME_ABORTED;
    boolean isHazardOutcome = false;
    foreach _, participant in participants {
        Protocol[] protocols = participant.participantProtocols;
        foreach proto in protocols {
            match proto.protocolFn {
                (function (string, int, string) returns boolean) protocolFn => {
                // if the participant is a local participant, i.e. protoFn is set, then call that fn
                    log:printInfo("Notify(abort) local participant: " + participant.participantId);
                    boolean successful = protocolFn(txn.transactionId, proto.transactionBlockId, COMMAND_ABORT);
                    if (!successful) {
                        error e = {message: OUTCOME_HAZARD};
                        ret = e;
                        isHazardOutcome = true;
                    }
                }
                null => {
                    var result = txn.notifyRemoteParticipant(participant, COMMAND_ABORT);
                    match result {
                        string status => {
                            if (!isHazardOutcome && status == OUTCOME_COMMITTED) {
                                txn.possibleMixedOutcome = true;
                                ret = OUTCOME_MIXED;
                            }
                        }
                        error err => {
                            error e = {message: OUTCOME_HAZARD};
                            ret = e;
                            isHazardOutcome = true;
                        }
                    }
                }
            }
        }
    }
    return ret;
}

function<TwoPhaseCommitTransaction txn> prepareRemoteParticipant (Participant participant,
                                                                  string protocolUrl) returns boolean {
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
            txn.removeParticipant(participantId, "Could not remove failed participant: " +
                                                    participantId + " from transaction: " + transactionId);
            successful = false;
        }
        string status => {
            if (status == OUTCOME_ABORTED) {
                log:printInfo("Remote participant: " + participantId + " aborted.");
                // Remove the participant who sent the abort since we don't want to do a notify(Abort) to that
                // participant
                txn.removeParticipant(participantId, "Could not remove aborted participant: " +
                                                      participantId + " from transaction: " + transactionId);
                successful = false;
            } else if (status == OUTCOME_COMMITTED) {
                log:printInfo("Remote participant: " + participantId + " committed");
                // If one or more participants returns "committed" and the overall prepare fails, we have to
                // report a mixed-outcome to the initiator
                txn.possibleMixedOutcome = true;
                // Don't send notify to this participant because it is has already committed. We can forget about this participant.
                txn.removeParticipant(participantId, "Could not remove committed participant: " +
                                                      participantId + " from transaction: " + transactionId);
            } else if (status == OUTCOME_READ_ONLY) {
                log:printInfo("Remote participant: " + participantId + " read-only");
                // Don't send notify to this participant because it is read-only. We can forget about this participant.
                txn.removeParticipant(participantId, "Could not remove read-only participant: " +
                                                      participantId + " from transaction: " + transactionId);
            } else if (status == OUTCOME_PREPARED) {
                log:printInfo("Remote participant: " + participantId + " prepared");
            } else {
                log:printInfo("Remote participant: " + participantId + ", status: " + status);
                successful = false;
            }
        }
    }
    return successful;
}

function<TwoPhaseCommitTransaction txn> notify (string message) returns boolean {
    boolean successful = true;
    foreach _, participant in txn.participants {
        Protocol[] protocols = participant.participantProtocols;
        foreach proto in protocols {
            match proto.protocolFn {
                (function (string, int, string) returns boolean) protocolFn => {
                // if the participant is a local participant, i.e. protoFn is set, then call that fn
                    log:printInfo("Notify(" + message + ") local participant: " + participant.participantId);
                    if (!protocolFn(txn.transactionId, proto.transactionBlockId, message)) {
                        successful = false;
                    }
                }
                null => {
                    var result = txn.notifyRemoteParticipant(participant, message);
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

function<TwoPhaseCommitTransaction txn> notifyRemoteParticipant (Participant participant,
                                                                 string message) returns string|error {
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
                if (notificationStatus == OUTCOME_ABORTED) {
                    log:printInfo("Remote participant: " + participantId + " aborted");
                } else if (notificationStatus == OUTCOME_COMMITTED) {
                    log:printInfo("Remote participant: " + participantId + " committed");
                }
                ret = notificationStatus;
            }
        }
    }
    return ret;
}

// This function will be called by the initiator
function<TwoPhaseCommitTransaction txn> abortInitiatorTransaction () returns string|error {
    string transactionId = txn.transactionId;
    int transactionBlockId = txn.transactionBlockId;
    log:printInfo(io:sprintf("Aborting initiated transaction: %s:%d", [transactionId, transactionBlockId]));
    // return response to the initiator. ( Aborted | Mixed )
    string|error ret = txn.notifyAbort();
    txn.state = TransactionState.ABORTED;
    boolean localAbortSuccessful = abortResourceManagers(transactionId, transactionBlockId);
    if (!localAbortSuccessful) {
        log:printError("Aborting local resource managers failed");
        error err = {message: OUTCOME_HAZARD};
        ret = err;
    }
    removeInitiatedTransaction(transactionId);
    return ret;
}

documentation {
    The participant should notify the initiator that it aborted. This function is called by the participant.
    The initiator is remote.
}
function<TwoPhaseCommitTransaction txn> abortLocalParticipantTransaction () returns string|error {
    string|error ret = "";
    boolean successful = abortResourceManagers(txn.transactionId, txn.transactionBlockId);
    string participatedTxnId = getParticipatedTransactionId(txn.transactionId, txn.transactionBlockId);
    if (successful) {
        txn.state = TransactionState.ABORTED;
        log:printInfo("Local participant aborted transaction: " + participatedTxnId);
    } else {
        string msg = "Aborting local resource managers failed for transaction:" + participatedTxnId;
        log:printError(msg);
        ret = {message:msg};
    }
    return ret;
}

function<TwoPhaseCommitTransaction txn> removeParticipant(string participantId, string failedMessage) {
    boolean participantRemoved = txn.participants.remove(participantId);
    if (!participantRemoved) {
        log:printError(failedMessage);
    }
}
