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

import ballerina/io;
import ballerina/log;
import ballerina/time;

class TwoPhaseCommitTransaction {

    string transactionId;
    string transactionBlockId;
    string coordinationType;
    boolean isInitiated = false; // Indicates whether this is a transaction that was initiated or is participated in
    map<Participant> participants = {};
    UProtocol?[] coordinatorProtocols = [];
    int createdTime = time:currentTime().time;
    TransactionState state = TXN_STATE_ACTIVE;
    private boolean possibleMixedOutcome = false;

    function init(string transactionId, string transactionBlockId, string coordinationType = "2pc") {
        self.transactionId = transactionId;
        self.transactionBlockId = transactionBlockId;
        self.coordinationType = coordinationType;
    }

    // This function will be called by the initiator
    function twoPhaseCommit() returns string|error {
        log:printInfo(io:sprintf("Running 2-phase commit for transaction: %s:%s", self.transactionId,
                self.transactionBlockId));
        string|error ret = "";

        // Prepare local resource managers
        boolean localPrepareSuccessful = prepareResourceManagers(self.transactionId, self.transactionBlockId);
        if (!localPrepareSuccessful) {
            log:printInfo("Local prepare failed, aborting..");
            var result = self.notifyParticipants(COMMAND_ABORT, ());
            if (result is error) {
                return "hazard";
            } else {
                match result {
                    "committed" => { return "committed"; }
                    "aborted" => { return "aborted"; }
                }
            }
            return "aborted";
        }

        // Prepare phase & commit phase
        // First call prepare on all volatile participants
        PrepareDecision prepareVolatilesDecision = self.prepareParticipants(PROTOCOL_VOLATILE);
        if (localPrepareSuccessful && prepareVolatilesDecision == PREPARE_DECISION_COMMIT) {
            // if all volatile participants voted YES, Next call prepare on all durable participants
            PrepareDecision prepareDurablesDecision = self.prepareParticipants(PROTOCOL_DURABLE);
            if (prepareDurablesDecision == PREPARE_DECISION_COMMIT) {
                // If all durable participants voted YES (PREPARED or READONLY), next call notify(commit) on all
                // (durable & volatile) participants and return committed to the initiator
                var result = self.notifyParticipants(COMMAND_COMMIT, ());
                if (result is error) {
                    // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                    ret = TransactionError(OUTCOME_HAZARD);
                } else {
                    boolean localCommitSuccessful = commitResourceManagers(self.transactionId, self.transactionBlockId);
                    if (!localCommitSuccessful) {
                        // "Local commit failed"
                        ret = TransactionError(OUTCOME_HAZARD);
                    } else {
                        ret = OUTCOME_COMMITTED;
                    }
                }
            } else {
                // If some durable participants voted NO, next call notify(abort) on all participants
                // and return aborted to the initiator
                var result = self.notifyParticipants(COMMAND_ABORT, ());
                if (result is error) {
                    // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                    ret = TransactionError(OUTCOME_HAZARD);
                } else {
                    boolean localAbortSuccessful = abortResourceManagers(self.transactionId, self.transactionBlockId);
                    if (!localAbortSuccessful) {
                        ret = TransactionError(OUTCOME_HAZARD);
                    } else {
                        if (self.possibleMixedOutcome) {
                            ret = OUTCOME_MIXED;
                        } else {
                            ret = OUTCOME_ABORTED;
                        }
                    }
                }
            }
        } else {
            // If some volatile participants voted NO, next call notify(abort) on all volatile articipants
            // and return aborted to the initiator
            var result = self.notifyParticipants(COMMAND_ABORT, PROTOCOL_VOLATILE);
            if (result is error) {
                // return Hazard outcome if a participant cannot successfully end its branch of the transaction
                ret = TransactionError(OUTCOME_HAZARD);
            } else {
                boolean localAbortSuccessful = abortResourceManagers(self.transactionId, self.transactionBlockId);
                if (!localAbortSuccessful) {
                    ret = TransactionError(OUTCOME_HAZARD);
                } else {
                    if (self.possibleMixedOutcome) {
                        ret = OUTCOME_MIXED;
                    } else {
                        ret = OUTCOME_ABORTED;
                    }
                }
            }
        }
        return ret;
    }

    # When an abort statement is executed, this function gets called. The transaction in concern will be marked for
    # abortion.
    #
    # + return - error or nil retured when marking transaction for abortion is unsuccessful or successful
    #            respectively
    function markForAbortion() returns error? {
        if (self.isInitiated) {
            self.state = TXN_STATE_ABORTED;
            log:printInfo("Marked initiated transaction for abortion");
        } else { // participant
            boolean successful = abortResourceManagers(self.transactionId, self.transactionBlockId);
            string participatedTxnId = getParticipatedTransactionId(self.transactionId, self.transactionBlockId);
            if (successful) {
                self.state = TXN_STATE_ABORTED;
                log:printInfo("Marked participated transaction for abort. Transaction:" + participatedTxnId);
            } else {
                string msg = "Aborting local resource managers failed for participated transaction:" +
                    participatedTxnId;
                log:printError(msg);
                return TransactionError(msg);
            }
        }
        return ();
    }

    // The result of this function is whether we can commit or abort
    function prepareParticipants(string protocol) returns PrepareDecision {
        PrepareDecision prepareDecision = PREPARE_DECISION_COMMIT;
        future<[(PrepareResult|error)?, Participant]>?[] results = [];
        foreach var participant in self.participants {
            string participantId = participant.participantId;
            future<[(PrepareResult|error)?, Participant]> f = @strand{thread:"any"} start participant.prepare(protocol);
            results[results.length()] = f;
        }
        foreach var res in results {
            future<[(PrepareResult|error)?, Participant]> f;
            if (res is future<[(PrepareResult|error)?, Participant]>) {
                f = res;
            } else {
                panic TransactionError("Unexpected nil found");
            }

            [(PrepareResult|error)?, Participant] r = wait f;
            var [result, participant] = r;
            string participantId = participant.participantId;
            if (result is PrepareResult) {
                if (result == PREPARE_RESULT_PREPARED) {
                // All set for a PREPARE_DECISION_COMMIT so we can proceed without doing anything
                } else if (result == PREPARE_RESULT_COMMITTED) {
                    // If one or more participants returns "committed" and the overall prepare fails, we have to
                    // report a mixed-outcome to the initiator
                    self.possibleMixedOutcome = true;
                    // Don't send notify to this participant because it is has already committed.
                    // We can forget about this participant.
                    self.removeParticipant(participantId,
                    "Could not remove committed participant: " + participantId + " from transaction: " +
                    self.transactionId);
                // All set for a PREPARE_DECISION_COMMIT so we can proceed without doing anything
                } else if (result == PREPARE_RESULT_READ_ONLY) {
                    // Don't send notify to this participant because it is read-only.
                    // We can forget about this participant.
                    self.removeParticipant(participantId,
                    "Could not remove read-only participant: " + participantId + " from transaction: " +
                    self.transactionId);
                // All set for a PREPARE_DECISION_COMMIT so we can proceed without doing anything
                } else if (result == PREPARE_RESULT_ABORTED) {
                    // Remove the participant who sent the abort since we don't want to do a notify(Abort) to that
                    // participant
                    self.removeParticipant(participantId, "Could not remove aborted participant: " + participantId +
                    " from transaction: " + self.transactionId);
                    prepareDecision = PREPARE_DECISION_ABORT;
                }
            } else if (result is error) {
                self.removeParticipant(participantId,
                "Could not remove prepare failed participant: " + participantId + " from transaction: " +
                self.transactionId);
                prepareDecision = PREPARE_DECISION_ABORT;
            }
        }
        return prepareDecision;
    }

    function notifyParticipants(string action, string? protocolName) returns NotifyResult|error {
        NotifyResult|error notifyResult = (action == COMMAND_COMMIT) ? NOTIFY_RESULT_COMMITTED : NOTIFY_RESULT_ABORTED;
        future<(NotifyResult|error)?>?[] results = [];
        foreach var participant in self.participants {
            future<(NotifyResult|error)?> f = @strand{thread:"any"} start participant.notify(action, protocolName);
            results[results.length()] = f;

        }
        foreach var r in results {
            future<(NotifyResult|error)?> f;
            if (r is future<(NotifyResult|error)?>) {
                f = r;
            } else {
                panic TransactionError("Unexpected nil found");
            }

            (NotifyResult|error)? result = wait f;
            if (result is error) {
                notifyResult = result;
            }
        }
        return notifyResult;
    }

    // This function will be called by the initiator
    function abortInitiatorTransaction() returns string|error {
        log:printInfo(io:sprintf("Aborting initiated transaction: %s:%s", self.transactionId, self.transactionBlockId));
        string|error ret = "";
        // return response to the initiator. ( Aborted | Mixed )
        var result = self.notifyParticipants(COMMAND_ABORT, ());
        if (result is error) {
            // return Hazard outcome if a participant cannot successfully end its branch of the transaction
            ret = TransactionError(OUTCOME_HAZARD);
        } else {
            boolean localAbortSuccessful = abortResourceManagers(self.transactionId, self.transactionBlockId);
            if (!localAbortSuccessful) {
                ret = TransactionError(OUTCOME_HAZARD);
            } else {
                if (self.possibleMixedOutcome) {
                    ret = OUTCOME_MIXED;
                } else {
                    ret = OUTCOME_ABORTED;
                }
            }
        }
        return ret;
    }

    # The participant should notify the initiator that it aborted. This function is called by the participant.
    # The initiator is remote.
    #
    # + return - An empty string or an error is returned when transaction abortion is successful or unsccuessful
    #            respectively
    function abortLocalParticipantTransaction() returns string|error {
        string|error ret = "";
        boolean successful = abortResourceManagers(self.transactionId, self.transactionBlockId);
        string participatedTxnId = getParticipatedTransactionId(self.transactionId, self.transactionBlockId);
        if (successful) {
            self.state = TXN_STATE_ABORTED;
            log:printInfo("Local participant aborted transaction: " + participatedTxnId);
        } else {
            string msg = "Aborting local resource managers failed for transaction:" + participatedTxnId;
            log:printError(msg);
            ret = TransactionError(msg);
        }
        return ret;
    }

    function removeParticipant(string participantId, string failedMessage) {
        var removed = trap self.participants.remove(participantId);
        if (removed is error) {
            log:printError(failedMessage);
        }
    }
}
