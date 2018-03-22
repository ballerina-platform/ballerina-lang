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
    R{{txnCtx}} - Transaction context corresponding to this transaction block
    R{{err}} - Error if something fails during beginning a transaction
}
function beginTransaction (string transactionId, int transactionBlockId, string registerAtUrl,
                           string coordinationType) returns (TransactionContext txnCtx, error err) {
    if (transactionId == null) {
        txnCtx, err = createTransactionContext(coordinationType, transactionBlockId);
    } else if (initiatedTransactions.hasKey(transactionId)) { // if participant & initiator are in the same process
        // we don't need to do a network call and can simply do a local function call
        txnCtx, err = registerParticipantWithLocalInitiator(transactionId, transactionBlockId, registerAtUrl);
    } else {
        //TODO: set the proper protocol
        string protocol = "durable";
        Protocol[] protocols = [{name:protocol, url:getParticipantProtocolAt(protocol, transactionBlockId)}];
        txnCtx, err = registerParticipantWithRemoteInitiator(transactionId, transactionBlockId, registerAtUrl, protocols);
    }
    return;
}

documentation {
    Mark a transaction for abortion.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
    R{{err}} - If the transaction was not found
}
function markForAbortion (string transactionId, int transactionBlockId) returns(error err) {
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
    if (participatedTransactions.hasKey(participatedTxnId)) {
        var txn, _ = (TwoPhaseCommitTransaction)participatedTransactions.get(transactionId);
        txn.state = TransactionState.ABORTED;
    } else if (initiatedTransactions.hasKey(transactionId)) {
        var txn, _ = (TwoPhaseCommitTransaction)initiatedTransactions.get(transactionId);
        txn.state = TransactionState.ABORTED;
    } else {
        err = {message: "Transaction: " + participatedTxnId + " not found"};
    }
    return;
}

documentation {
    When a transaction block in Ballerina code ends, it will call this function to end a transaction.
    Ending a transaction by a participant has no effect because it is the initiator who can decide whether to
    commit or abort a transaction.
    Depending on the state of the transaction, the initiator decides to commit or abort the transaction.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
    R{{msg}} - Outcome of the transaction
    R{{err}} - Error if something fails during beginning a transaction
}
function endTransaction (string transactionId, int transactionBlockId) returns (string msg, error err) {
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);

    // Only the initiator can end the transaction. Here we check whether the entity trying to end the transaction is
    // an initiator or just a local participant
    if (initiatedTransactions.hasKey(transactionId) && !participatedTransactions.hasKey(participatedTxnId)) {
        var txn, _ = (TwoPhaseCommitTransaction)initiatedTransactions.get(transactionId);
        if (txn.state == TransactionState.ABORTED) {
            msg, err = abortTransaction(transactionId, transactionBlockId);
        } else {
            msg, err = commitTransaction(transactionId, transactionBlockId);
            if (err == null) {
                txn.state = TransactionState.COMMITTED;
                initiatedTransactions.remove(transactionId);
            }
        }
    }
     // Nothing to do on endTransaction if you are a participant
    return;
}

documentation {
    When an abort statement is executed, this function gets called. Depending on whether the transaction block
    is an initiator or participant the flow will be different. An initiator will start the abort protocol. A participant
    will notify the initiator that it aborted, which will prompt the initiator to start the abort protocol.

    The initiator and participant being in the same process
    has also been handled as a special case in order to avoid a network call in that case.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
    R{{msg}} - Transaction context corresponding to this transaction block
    R{{err}} - Error if something fails during beginning a transaction
}
function abortTransaction (string transactionId, int transactionBlockId) returns (string msg, error err) {
    log:printInfo("########### abort called");

    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
    if (initiatedTransactions.hasKey(transactionId)) {
        if (participatedTransactions.hasKey(participatedTxnId)) {

            log:printInfo("########### aborting local participant transaction");

            // if I am a local participant, then I will remove myself because I don't want to be notified on abort,
            // and then call abort on the initiator
            var txn, _ = (TwoPhaseCommitTransaction)initiatedTransactions.get(transactionId);
            boolean successful = abortResourceManagers(transactionId, transactionBlockId);
            if (!successful) {
                err = {message:"Aborting local resource managers failed for transaction:" + participatedTxnId};
                return;
            }
            string participantId = getParticipantId(transactionBlockId);
            // do not remove the transaction since we may get a msg from the initiator
            _ = txn.participants.remove(participantId);
            msg, err = abortInitiatorTransaction(transactionId, transactionBlockId);
            if (err == null) {
                txn.state = TransactionState.ABORTED;
            }
        } else {
            msg, err = abortInitiatorTransaction(transactionId, transactionBlockId);
        }
    } else {
        //msg, err = abortLocalParticipantTransaction(transactionId, transactionBlockId); // TODO: we can move the core logic here from the function
        var txn, _ = (TwoPhaseCommitTransaction)participatedTransactions.get(participatedTxnId);
        boolean successful = abortResourceManagers(transactionId, transactionBlockId);
        if (!successful) {
            err = {message:"Aborting local resource managers failed for transaction:" + participatedTxnId};
            log:printErrorCause("Local participant transaction: " + participatedTxnId + " failed to abort", err);
            return;
        } else {
            txn.state = TransactionState.ABORTED;
            log:printInfo("Local participant aborted transaction: " + participatedTxnId);
        }
    }
    // do not remove the transaction since we may get a msg from the initiator
    return;
}

documentation {
    Checks whether this instance is an initiator. Returns true if initiator.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
}
function isInitiator (string transactionId, int transactionBlockId) returns (boolean) {
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
    R{{prepareSuccessful}} - Indicates whether the outcome was successful
}
native function prepareResourceManagers (string transactionId,
                                         int transactionBlockId) returns (boolean prepareSuccessful);

documentation {
    Commit local resource managers.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
    R{{commitSuccessful}} - Indicates whether the outcome was successful
}
native function commitResourceManagers (string transactionId,
                                        int transactionBlockId) returns (boolean commitSuccessful);

documentation {
    Abort local resource managers.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
    R{{abortSuccessful}} - Indicates whether the outcome was successful
}
native function abortResourceManagers (string transactionId,
                                       int transactionBlockId) returns (boolean abortSuccessful);

