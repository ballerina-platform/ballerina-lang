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

documentation {
    When a transaction block in Ballerina code begins, it will call this function to begin a transaction.
    If this is a new transaction (transactionId == null), then this instance will become the initiator and will
    create a new transaction context.
    If the participant and initiator are in the same process, this transaction block will register with the local
    initiator via a local function call.
    If the participant and initiator are in different processes, this transaction block will register with the remote
    initiator via a network call.

    P{{transactionId}} - Globally unique transaction ID. If this is a new transaction which is initiated, then this
                         will be null.
                         If this is a participant in an existing transaction, then it will have a value.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
    P{{registerAtUrl}} - The URL of the initiator
    P{{coordinationType}} - Coordination type of this transaction
}
function beginTransaction (string|null transactionId, int transactionBlockId, string registerAtUrl,
                           string coordinationType) returns TransactionContext|error {
    match transactionId {
        string txnId => {
            if (initiatedTransactions.hasKey(txnId)) { // if participant & initiator are in the same process
                // we don't need to do a network call and can simply do a local function call
                return registerParticipantWithLocalInitiator(txnId, transactionBlockId, registerAtUrl);
            } else {
                //TODO: set the proper protocol
                string protocol = "durable";
                Protocol[] protocols = [{name:protocol, url:getParticipantProtocolAt(protocol, transactionBlockId)}];
                return registerParticipantWithRemoteInitiator(txnId, transactionBlockId, registerAtUrl, protocols);
            }
        }

        any|null => {
            return createTransactionContext(coordinationType, transactionBlockId);
        }
    }
}

documentation {
    Mark a transaction for abortion.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
}
function markForAbortion (string transactionId, int transactionBlockId) {
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
    if (participatedTransactions.hasKey(participatedTxnId)) {
        var txn =? <TwoPhaseCommitTransaction>participatedTransactions[transactionId];
        txn.state = TransactionState.ABORTED;
    } else if (initiatedTransactions.hasKey(transactionId)) {
        var txn =? <TwoPhaseCommitTransaction>initiatedTransactions[transactionId];
        txn.state = TransactionState.ABORTED;
    } else {
        error err = {message:"Transaction: " + participatedTxnId + " not found"};
        throw err;
    }
}

documentation {
    When a transaction block in Ballerina code ends, it will call this function to end a transaction.
    Ending a transaction by a participant has no effect because it is the initiator who can decide whether to
    commit or abort a transaction.
    Depending on the state of the transaction, the initiator decides to commit or abort the transaction.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
}
function endTransaction (string transactionId, int transactionBlockId) returns string|error {
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);

    // Only the initiator can end the transaction. Here we check whether the entity trying to end the transaction is
    // an initiator or just a local participant
    if (initiatedTransactions.hasKey(transactionId) && !participatedTransactions.hasKey(participatedTxnId)) {
        var txn =? <TwoPhaseCommitTransaction>initiatedTransactions[transactionId];
        if (txn.state == TransactionState.ABORTED) {
            return abortTransaction(transactionId, transactionBlockId);
        } else {
            var ret =? commitTransaction(transactionId, transactionBlockId);
            removeInitiatedTransaction(transactionId);
            return ret;
        }
    } else {
        return "";  // Nothing to do on endTransaction if you are a participant
    }
}

documentation {
    When an abort statement is executed, this function gets called. Depending on whether the transaction block
    is an initiator or participant the flow will be different. An initiator will start the abort protocol. A participant
    will notify the initiator that it aborted, which will prompt the initiator to start the abort protocol.

    The initiator and participant being in the same process
    has also been handled as a special case in order to avoid a network call in that case.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
}
function abortTransaction (string transactionId, int transactionBlockId) returns string|error {
    log:printInfo("########### abort called");

    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
    if (initiatedTransactions.hasKey(transactionId)) {
        if (participatedTransactions.hasKey(participatedTxnId)) {

            log:printInfo("########### aborting local participant transaction");

            var txn =? <TwoPhaseCommitTransaction>initiatedTransactions[transactionId];
            boolean successful = abortResourceManagers(transactionId, transactionBlockId);
            if (!successful) {
                error err = {message:"Aborting local resource managers failed for transaction:" + participatedTxnId};
                return err;
            }
            string participantId = getParticipantId(transactionBlockId);
            // if I am a local participant, then I will remove myself because I don't want to be notified on abort,
            // and then call abort on the initiator
            boolean removed = txn.participants.remove(participantId);
            if (!removed) {
                error err = {message:"Participant: " + participantId + " removal failed"};
                throw err;
            }
            var ret =? abortInitiatorTransaction(transactionId, transactionBlockId);
            txn.state = TransactionState.ABORTED;
            return ret;
        } else {
            return abortInitiatorTransaction(transactionId, transactionBlockId);
        }
    } else {
        var txn =? <TwoPhaseCommitTransaction>participatedTransactions[participatedTxnId];
        boolean successful = abortResourceManagers(transactionId, transactionBlockId);
        if (!successful) {
            error err = {message:"Aborting local resource managers failed for transaction:" + participatedTxnId};
            log:printErrorCause("Local participant transaction: " + participatedTxnId + " failed to abort", err);
            return err;
        } else {
            txn.state = TransactionState.ABORTED;
            log:printInfo("Local participant aborted transaction: " + participatedTxnId);
            return ""; //TODO: check what will happen if nothing is returned
        }
    }
}

documentation {
    Checks whether this instance is an initiator. Returns true if initiator.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
}
function isInitiator (string transactionId, int transactionBlockId) returns boolean {
    if (initiatedTransactions.hasKey(transactionId)) {
        string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
        if (!participatedTransactions.hasKey(participatedTxnId)) {
            return true;
        }
    }
    return false;
}

documentation {
    Prepare local resource managers.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
}
native function prepareResourceManagers (string transactionId,
                                         int transactionBlockId) returns boolean;

documentation {
    Commit local resource managers.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
}
native function commitResourceManagers (string transactionId,
                                        int transactionBlockId) returns boolean;

documentation {
    Abort local resource managers.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
}
native function abortResourceManagers (string transactionId,
                                       int transactionBlockId) returns boolean;

