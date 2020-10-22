// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/lang.'value as value;
import ballerina/java;

const string TWO_PHASE_COMMIT = "2pc";
const string PROTOCOL_COMPLETION = "completion";
const string PROTOCOL_VOLATILE = "volatile";
const string PROTOCOL_DURABLE = "durable";

type TransactionContext record {
    string contextVersion = "1.0";
    string transactionId = "";
    string transactionBlockId = "";
    string coordinationType = "";
    string registerAtURL = "";
};

# ID of the local participant used when registering with the initiator.
string localParticipantId = "85674653634523";

# This map is used for caching transaction that are this Ballerina instance participates in.
@tainted map<TwoPhaseCommitTransaction> participatedTransactions = {};

# This represents the protocol associated with the coordination type.
#
# + name - protocol name
type LocalProtocol record {
    string name = "";
};

# This represents the protocol associated with the coordination type.

# + name - protocol name
# + url - protocol URL. This URL will have a value only if the participant is remote. If the participant is local,
#         the `protocolFn` will be called
type RemoteProtocol record {
    string name = "";
    string url = "";
};

string[] coordinationTypes = [TWO_PHASE_COMMIT];

type TransactionState TXN_STATE_ACTIVE|TXN_STATE_PREPARED|TXN_STATE_COMMITTED|TXN_STATE_ABORTED;
const TXN_STATE_ACTIVE = "active";
const TXN_STATE_PREPARED = "prepared";
const TXN_STATE_COMMITTED = "committed";
const TXN_STATE_ABORTED = "aborted";

type PrepareResult PREPARE_RESULT_PREPARED|PREPARE_RESULT_ABORTED|PREPARE_RESULT_COMMITTED|PREPARE_RESULT_READ_ONLY;
const PREPARE_RESULT_PREPARED = "prepared";
const PREPARE_RESULT_ABORTED = "aborted";
const PREPARE_RESULT_COMMITTED = "committed";
const PREPARE_RESULT_READ_ONLY = "read-only";

type NotifyResult NOTIFY_RESULT_COMMITTED|NOTIFY_RESULT_ABORTED;
const NOTIFY_RESULT_COMMITTED = "committed";
const NOTIFY_RESULT_ABORTED = "aborted";

type PrepareDecision PREPARE_DECISION_COMMIT|PREPARE_DECISION_ABORT;
const PREPARE_DECISION_COMMIT = "commit";
const PREPARE_DECISION_ABORT = "abort";

type UProtocol LocalProtocol|RemoteProtocol;

type Participant object {

    string participantId;

    function prepare(string protocol) returns [(PrepareResult|error)?, Participant];

    function notify(string action, string? protocolName) returns (NotifyResult|error)?;
};

class LocalParticipant {

    string participantId;
    private TwoPhaseCommitTransaction participatedTxn;
    private LocalProtocol[] participantProtocols;

    function init(string participantId, TwoPhaseCommitTransaction participatedTxn, LocalProtocol[]
        participantProtocols) {
        self.participantId = participantId;
        self.participatedTxn = participatedTxn;
        self.participantProtocols = participantProtocols;
    }

    function prepare(string protocol) returns [(PrepareResult|error)?, Participant] {
        foreach var localProto in self.participantProtocols {
            if (localProto.name == protocol) {
                return [self.prepareMe(self.participatedTxn.transactionId, self.participatedTxn.transactionBlockId),
                self];
            }
        }
        return [(), self];
    }

    function prepareMe(string transactionId, string transactionBlockId) returns PrepareResult|error {
        string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
        if (!participatedTransactions.hasKey(participatedTxnId)) {
            error err = error(TRANSACTION_UNKNOWN);
            return err;
        }
        if (self.participatedTxn.state == TXN_STATE_ABORTED) {
            removeParticipatedTransaction(participatedTxnId);
            return PREPARE_RESULT_ABORTED;
        } else if (self.participatedTxn.state == TXN_STATE_COMMITTED) {
            removeParticipatedTransaction(participatedTxnId);
            return PREPARE_RESULT_COMMITTED;
        } else {
            boolean successful = prepareResourceManagers(transactionId, transactionBlockId);
            if (successful) {
                self.participatedTxn.state = TXN_STATE_PREPARED;
                return PREPARE_RESULT_PREPARED;
            } else {
                return PREPARE_RESULT_ABORTED;
            }
        }
    }

    function notify(string action, string? protocolName) returns (NotifyResult|error)? {
        if (protocolName is string) {
            foreach var localProto in self.participantProtocols {
                if (protocolName == localProto.name) {
                    return self.notifyMe(action, self.participatedTxn.transactionBlockId);
                }
            }
        } else {
            NotifyResult|error notifyResult = (action == COMMAND_COMMIT) ? NOTIFY_RESULT_COMMITTED
                                                                         : NOTIFY_RESULT_ABORTED;
            foreach var localProto in self.participantProtocols {
                var result = self.notifyMe(action, self.participatedTxn.transactionBlockId);
                if (result is error) {
                    notifyResult = result;
                }
                // Else, nothing to do since we have set the notifyResult already
            }
            return notifyResult;
        }
        return (); // No matching protocol
    }

    function notifyMe(string action, string participatedTxnBlockId) returns NotifyResult|error {
        string participatedTxnId = getParticipatedTransactionId(self.participatedTxn.transactionId, participatedTxnBlockId);
        if (action == COMMAND_COMMIT) {
            if (self.participatedTxn.state == TXN_STATE_PREPARED) {
                boolean successful = commitResourceManagers(self.participatedTxn.transactionId, participatedTxnBlockId);
                removeParticipatedTransaction(participatedTxnId);
                if (successful) {
                    return NOTIFY_RESULT_COMMITTED;
                } else {
                    error err = error(NOTIFY_RESULT_FAILED_EOT_STR);
                    return err;
                }
            } else {
                error err = error(NOTIFY_RESULT_NOT_PREPARED_STR);
                return err;
            }
        } else if (action == COMMAND_ABORT) {
            boolean successful = abortResourceManagers(self.participatedTxn.transactionId, participatedTxnBlockId);
            removeParticipatedTransaction(participatedTxnId);
            if (successful) {
                return NOTIFY_RESULT_ABORTED;
            } else {
                error err = error(NOTIFY_RESULT_FAILED_EOT_STR);
                return err;
            }
        } else {
            error err = error("Invalid protocol action:" + action);
            panic err;
        }
    }
}

function getParticipatedTransactionId(string transactionId, string transactionBlockId) returns string {
    string id = transactionId + ":" + transactionBlockId;
    return id;
}

function removeParticipatedTransaction(string participatedTxnId) {
    var removed = trap participatedTransactions.remove(participatedTxnId);
    if (removed is error) {
        error err = error("Removing participated transaction: " + participatedTxnId + " failed");
        panic err;
    }
}

function removeInitiatedTransaction(string transactionId) {
    var removed = trap initiatedTransactions.remove(transactionId);
    if (removed is error) {
        error err = error("Removing initiated transaction: " + transactionId + " failed");
        panic err;
    }
}

function getParticipantId(string transactionBlockId) returns string {
    string participantId = localParticipantId + ":" + transactionBlockId;
    return participantId;
}

function isValidCoordinationType(string coordinationType) returns boolean {
    foreach var coordType in coordinationTypes {
        if (coordinationType == coordType) {
            return true;
        }
    }
    return false;
}

class TwoPhaseCommitTransaction {

    string transactionId;
    string transactionBlockId;
    string coordinationType;
    boolean isInitiated = false; // Indicates whether this is a transaction that was initiated or is participated in
    map<Participant> participants = {};
    UProtocol?[] coordinatorProtocols = [];
    //TODO: get the current time
    int createdTime = timeNow();
    TransactionState state = TXN_STATE_ACTIVE;
    private boolean possibleMixedOutcome = false;

    function init(string transactionId, string transactionBlockId, string coordinationType = "2pc") {
        self.transactionId = transactionId;
        self.transactionBlockId = transactionBlockId;
        self.coordinationType = coordinationType;
    }

    // This function will be called by the initiator
    function twoPhaseCommit() returns string|Error {
        string|Error ret = "";

        // Prepare local resource managers
        boolean localPrepareSuccessful = prepareResourceManagers(self.transactionId, self.transactionBlockId);
        if (!localPrepareSuccessful) {
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
                    ret = prepareError(OUTCOME_HAZARD);
                } else {
                    boolean localCommitSuccessful = commitResourceManagers(self.transactionId, self.transactionBlockId);
                    if (!localCommitSuccessful) {
                        // "Local commit failed"
                        ret = prepareError(OUTCOME_HAZARD);
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
                    ret = prepareError(OUTCOME_HAZARD);
                } else {
                    boolean localAbortSuccessful = abortResourceManagers(self.transactionId, self.transactionBlockId);
                    if (!localAbortSuccessful) {
                        ret = prepareError(OUTCOME_HAZARD);
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
                ret = prepareError(OUTCOME_HAZARD);
            } else {
                boolean localAbortSuccessful = abortResourceManagers(self.transactionId, self.transactionBlockId);
                if (!localAbortSuccessful) {
                    ret = prepareError(OUTCOME_HAZARD);
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
        } else { // participant
            boolean successful = abortResourceManagers(self.transactionId, self.transactionBlockId);
            string participatedTxnId = getParticipatedTransactionId(self.transactionId, self.transactionBlockId);
            if (successful) {
                self.state = TXN_STATE_ABORTED;
            } else {
                string msg = "Aborting local resource managers failed for participated transaction:" +
                    participatedTxnId;
                error err = error(msg);
                return err;
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
                error err = error("Unexpected nil found");
                panic err;
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
                error err = error("Unexpected nil found");
                panic err;
            }

            (NotifyResult|error)? result = wait f;
            if (result is error) {
                notifyResult = result;
            }
        }
        return notifyResult;
    }

    // This function will be called by the initiator
    function abortInitiatorTransaction() returns string|Error {
        string|Error ret = "";
        // return response to the initiator. ( Aborted | Mixed )
        var result = self.notifyParticipants(COMMAND_ABORT, ());
        if (result is error) {
            // return Hazard outcome if a participant cannot successfully end its branch of the transaction
            ret = prepareError(OUTCOME_HAZARD);
        } else {
            boolean localAbortSuccessful = abortResourceManagers(self.transactionId, self.transactionBlockId);
            if (!localAbortSuccessful) {
                ret = prepareError(OUTCOME_HAZARD);
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
        } else {
            string msg = "Aborting local resource managers failed for transaction:" + participatedTxnId;
            error err = error(msg);
            ret = err;
        }
        return ret;
    }

    function removeParticipant(string participantId, string failedMessage) {
        var removed = trap self.participants.remove(participantId);
        if (removed is error) {
            //TODO: do what?
        }
    }
}

# This map is used for caching transaction that are initiated.
map<TwoPhaseCommitTransaction> initiatedTransactions = {};

function startTransaction(string transactionBlockId, Info? prevAttempt = ()) returns string {
    string transactionId = "";
    TransactionContext|error txnContext = createTransactionContext(TWO_PHASE_COMMIT, transactionBlockId);
    if (txnContext is error) {
        panic txnContext;
    } else {

        transactionId = txnContext.transactionId;
        setTransactionContext(txnContext, prevAttempt);
    }
    return transactionId;
}

# When a transaction block in Ballerina code ends, it will call this function to end a transaction.
# Ending a transaction by a participant has no effect because it is the initiator who can decide whether to
# commit or abort a transaction.
# Depending on the state of the transaction, the initiator decides to commit or abort the transaction.
#
# + transactionId - Globally unique transaction ID.
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + return - A string or an error representing the transaction end succcess status or failure respectively.
transactional function endTransaction(string transactionId, string transactionBlockId)
        returns @tainted string|Error? {
    if (getRollbackOnly()) {
        return getRollbackOnlyError();
    }

    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
    if (!initiatedTransactions.hasKey(transactionId) && !participatedTransactions.hasKey(participatedTxnId)) {
        error err = error("Transaction: " + participatedTxnId + " not found");
        panic err;
    }

    setContextAsNonTransactional();

    // Only the initiator can end the transaction. Here we check whether the entity trying to end the transaction is
    // an initiator or just a local participant
    if (!participatedTransactions.hasKey(participatedTxnId)) {
        var initiatedTxn = initiatedTransactions[transactionId];
        if (initiatedTxn is ()) {
            return "";
        } else {
            if (initiatedTxn.state == TXN_STATE_ABORTED) {
                return initiatedTxn.abortInitiatorTransaction();
            } else {
                string|Error ret = initiatedTxn.twoPhaseCommit();
                removeInitiatedTransaction(transactionId);
                return ret;
            }
        }
    } else {
        return "";  // Nothing to do on endTransaction if you are a participant
    }
}

# A new transaction context is created by calling this function. At this point, a transaction object
# corresponding to the coordinationType will also be created and stored as an initiated transaction.
#
# + coordinationType - The type of the coordination relevant to the transaction block for which this TransactionContext
#                      is being created for.
# + transactionBlockId - The ID of the transaction block.
# + return - TransactionContext if the coordination type is valid or an error in case of an invalid coordination type.
function createTransactionContext(string coordinationType, string transactionBlockId) returns TransactionContext|error {
    if (!isValidCoordinationType(coordinationType)) {
        string msg = "Invalid-Coordination-Type:" + coordinationType;
        error err = error(msg);
        return err;
    } else {
        TwoPhaseCommitTransaction txn = new(uuid(), transactionBlockId, coordinationType = coordinationType);
        string txnId = txn.transactionId;
        txn.isInitiated = true;
        initiatedTransactions[txnId] = txn;
        TransactionContext txnContext = {
            transactionId:txnId,
            transactionBlockId:transactionBlockId,
            coordinationType:coordinationType,
            registerAtURL:"http://" + value:toString(getHostAddress()) + ":" + value:toString(getAvailablePort()) +
                initiatorCoordinatorBasePath + "/" + transactionBlockId + registrationPath
        };
        return txnContext;
    }
}

# Commit local resource managers.
#
# + transactionId - Globally unique transaction ID.
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + return - true or false representing whether the commit is successful or not.
function commitResourceManagers(string transactionId, string transactionBlockId) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.CommitResourceManagers",
    name: "commitResourceManagers"
} external;

# Prepare local resource managers.
#
# + transactionId - Globally unique transaction ID.
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + return - true or false representing whether the resource manager preparation is successful or not.
function prepareResourceManagers(string transactionId, string transactionBlockId) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.PrepareResourceManagers",
    name: "prepareResourceManagers"
} external;

# Abort local resource managers.
#
# + transactionId - Globally unique transaction ID.
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + return - true or false representing whether the resource manager abortion is successful or not.
function abortResourceManagers(string transactionId, string transactionBlockId) returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.AbortResourceManagers",
    name: "abortResourceManagers"
} external;

# Set the transactionContext.
#
# + transactionContext - Transaction context.
# + prevAttempt - Information related to previous attempt.
function setTransactionContext(TransactionContext transactionContext, Info? prevAttempt = ()) = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.SetTransactionContext",
    name: "setTransactionContext"
} external;

# Rollback the transaction.
#
# + transactionBlockId - ID of the transaction block.
# + err - The cause of the rollback.
function rollbackTransaction(string transactionBlockId, error? err = ()) = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.RollbackTransaction",
    name: "rollbackTransaction"
} external;

# Get and Cleanup the failure.
#
# + return - is failed.
function getAndClearFailure() returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.GetAndClearFailure",
    name: "getAndClearFailure"
} external;

# Cleanup the transaction context.
#
# + transactionBlockId - ID of the transaction block.
function cleanupTransactionContext(string transactionBlockId) = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.CleanUpTransactionContext",
    name: "cleanupTransactionContext"
} external;

function isTransactional() returns boolean = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.IsTransactional",
    name: "isTransactional"
} external;

function getAvailablePort() returns int = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.GetAvailablePort",
    name: "getAvailablePort"
} external;

function getHostAddress() returns string = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.GetHostAddress",
    name: "getHostAddress"
} external;

function uuid() returns string = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.UUID",
    name: "uuid"
} external;

function timeNow() returns int = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.CurrentTime",
    name: "timeNow"
} external;

function getRollbackOnlyError() returns Error? = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.GetRollbackOnlyError",
    name: "getRollbackOnlyError"
} external;

function setContextAsNonTransactional() = @java:Method {
    'class: "org.ballerinalang.langlib.transaction.SetContextAsNonTransactional",
    name: "setContextAsNonTransactional"
} external;
