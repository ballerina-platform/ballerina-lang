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

documentation {
    When a transaction block in Ballerina code begins, it will call this function to begin a transaction.
    If this is a new transaction (transactionId == () ), then this instance will become the initiator and will
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
function beginTransaction (string? transactionId, int transactionBlockId, string registerAtUrl,
                           string coordinationType) returns TransactionContext|error {
    match transactionId {
        string txnId => {
            io:println(typeof txnId);
            if (initiatedTransactions.hasKey(txnId)) { // if participant & initiator are in the same process
                // we don't need to do a network call and can simply do a local function call
                return registerParticipantWithLocalInitiator(txnId, transactionBlockId, registerAtUrl);
            } else {
                //TODO: set the proper protocol
                ProtocolName protocolName = PROTOCOL_DURABLE;
                Protocol[] protocols = [{name:protocolName, url:getParticipantProtocolAt(protocolName, transactionBlockId)}];
                return registerParticipantWithRemoteInitiator(txnId, transactionBlockId, registerAtUrl, protocols);
            }
        }

        () => {
            return createTransactionContext(coordinationType, transactionBlockId);
        }
    }
}

documentation {
    When an abort statement is executed, this function gets called.

    P{{transactionId}} - Globally unique transaction ID.
    P{{transactionBlockId}} - ID of the transaction block. Each transaction block in a process has a unique ID.
}
function abortTransaction (string transactionId, int transactionBlockId) returns string|error {
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
    if (participatedTransactions.hasKey(participatedTxnId)) {
        TwoPhaseCommitTransaction txn = participatedTransactions[participatedTxnId];
        return txn.markForAbortion();
    } else if (initiatedTransactions.hasKey(transactionId)) {
        TwoPhaseCommitTransaction txn = initiatedTransactions[transactionId];
        return txn.markForAbortion();
    } else {
        error err = {message:"Unknown transaction"};
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
    if(!initiatedTransactions.hasKey(transactionId) && !participatedTransactions.hasKey(participatedTxnId)) {
        error err = {message:"Transaction: " + participatedTxnId + " not found"};
        throw err;
    }

    // Only the initiator can end the transaction. Here we check whether the entity trying to end the transaction is
    // an initiator or just a local participant
    if (initiatedTransactions.hasKey(transactionId) && !participatedTransactions.hasKey(participatedTxnId)) {
        TwoPhaseCommitTransaction txn = initiatedTransactions[transactionId];
        if (txn.state == TXN_STATE_ABORTED) {
            return txn.abortInitiatorTransaction();
        } else {
            string|error ret = txn.twoPhaseCommit();
            removeInitiatedTransaction(transactionId);
            return ret;
        }
    } else {
        return "";  // Nothing to do on endTransaction if you are a participant
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


documentation {
    Get the current transaction id.
}
native function GetCurrentTransactionId () returns string;
