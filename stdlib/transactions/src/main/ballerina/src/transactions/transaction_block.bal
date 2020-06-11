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

import ballerina/log;
import ballerina/java;

# Handles the transaction initiator block.
# Transaction initiator block will be desugared to following method.
#
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + rMax - Max retry count.
# + trxFunc - Transaction main block function.
# + retryFunc - On retry block function.
# + committedFunc - Committed function.
# + abortedFunc - Abort function.
function beginTransactionInitiator(string transactionBlockId, int rMax, function () returns int trxFunc,
                                   function () retryFunc, function () committedFunc, function () abortedFunc) {
    boolean isTrxSuccess = false;
    int rCnt = 1;
    if (rMax < 0) {
        panic TransactionError("invalid retry count");
    }
    // If global tx enabled, it is managed via transaction coordinator.Otherwise it is managed locally
    // without any interaction with the transaction coordinator.
    if (isNestedTransaction()) {
        // Starting a transaction within already infected transaction.
        panic TransactionError("dynamically nested transactions are not allowed");
    }
    TransactionContext|error txnContext;
    string transactionId = "";
    int|error trxResult = -1;
    error? abortResult = ();
    error? committedResult = ();
    error? retryResult = ();
    error? rollbackResult = ();
    
    while (true) {
        txnContext =  beginTransaction((), transactionBlockId, "", TWO_PHASE_COMMIT);
        if (txnContext is error) {
            panic txnContext;
        } else {
            transactionId = txnContext.transactionId;
            setTransactionContext(txnContext);
        }
        trxResult = trap trxFunc();
        if (trxResult is int) {
            // If transaction result == 0, means it is successful.
            if (trxResult == 0) { 
                // We need to check any failures in transaction context. This will handle cases where transaction
                // code will not panic still we need to fail the transaction. ex sql transactions
                boolean isFailed = getAndClearFailure();
                if (!isFailed) {
                    var endSuccess = trap endTransaction(transactionId, transactionBlockId);
                    if (endSuccess is string) {
                        if (endSuccess == OUTCOME_COMMITTED) {
                            isTrxSuccess = true;
                            break;
                        }
                    }
                }
            }
            // If transaction result == -1, means it was aborted.
            if (trxResult == -1) { // abort
                abortResult = trap abortTransaction(transactionId, transactionBlockId);
                break;
            }
        } else {
            log:printDebug(trxResult.message());
        }
        rCnt = rCnt + 1;
        // retry
        if (rCnt <= rMax) {
            rollbackResult = trap rollbackTransaction(transactionBlockId);
            if (rollbackResult is error) {
                log:printDebug(rollbackResult.message());
            }
            retryResult = trap retryFunc();
            if (retryResult is error) {
                log:printDebug(retryResult.message());
            }
        } else {
            break;
        }
    }
    if (isTrxSuccess) {
        committedResult = trap committedFunc();
        if (committedResult is error) {
            log:printDebug(committedResult.message());
        }
    } else {
        abortResult = trap handleAbortTransaction(transactionId, transactionBlockId, abortedFunc);
        if (abortResult is error) {
            log:printDebug(abortResult.message());
        }
    }
    cleanupTransactionContext(transactionBlockId);
    // Rethrowing the  panic error captured in transaction block.
    if (trxResult is error) {
        panic trxResult;
    }
}

# Handles the trahsaction local participant function.
# Transaction local participant function will be desugared to following method.
#
# + transactionBlockId - ID of the transaction block.
# + trxFunc - Participant logic.
# + committedFunc - Committed function.
# + abortedFunc - Abort function.
# + return - Return value of the participant.
function beginLocalParticipant(string transactionBlockId, function () returns any|error trxFunc,
                               function (string trxId) committedFunc, function (string trxId) abortedFunc)
                               returns any|error|() {
    TransactionContext? txnContext = registerLocalParticipant(transactionBlockId, committedFunc, abortedFunc);
    if (txnContext is ()) {
        return <any|error|()>trxFunc();
    } else {
        TransactionContext|error returnContext = beginTransaction(txnContext.transactionId, transactionBlockId,
            txnContext.registerAtURL, txnContext.coordinationType);
        if (returnContext is error) {
            notifyLocalParticipantOnFailure();
            panic returnContext;
        } else {
            log:printInfo("participant registered: " + returnContext.transactionId);
        }
        var result = trap transactionParticipantWrapper(trxFunc);
        if (result is error) {
            notifyLocalParticipantOnFailure();
            panic result;
        } else {
            return result.data;
        }
    }
}

# Handles the trahsaction remote participant function.
# Transaction remote participant function will be desugared to following method.
#
# + transactionBlockId - ID of the transaction block.
# + trxFunc - Participant logic.
# + committedFunc - Committed function.
# + abortedFunc - Abort function.
# + return - Return value of the participant.
function beginRemoteParticipant(string transactionBlockId, function () returns any|error trxFunc,
                                function (string trxId) committedFunc, function (string trxId) abortedFunc)
                                returns any|error|() {
    TransactionContext? txnContext = registerRemoteParticipant(transactionBlockId, committedFunc, abortedFunc);
    if (txnContext is ()) {
        return trxFunc();
    } else {
        TransactionContext|error returnContext = beginTransaction(txnContext.transactionId, transactionBlockId,
            txnContext.registerAtURL, txnContext.coordinationType);
        if (returnContext is error) {
            notifyRemoteParticipantOnFailure();
            panic returnContext;
        } else {
            log:printInfo("participant registered: " + returnContext.transactionId);
        }
        var result = trap transactionParticipantWrapper(trxFunc);
        if (result is error) {
            notifyRemoteParticipantOnFailure();
            panic result;
        } else {
            return result.data;
        }
    }
}

function handleAbortTransaction(string transactionId, string transactionBlockId, function () abortedFunc) {
    var result = trap abortTransaction(transactionId, transactionBlockId);
    notifyResourceManagerOnAbort(transactionBlockId);
    var abortResult = trap abortedFunc();
    if (result is error) {
        panic result;
    }
    if (abortResult is error) {
        panic abortResult;
    }
}

# When a transaction block in Ballerina code begins, it will call this function to begin a transaction.
# If this is a new transaction (transactionId == () ), then this instance will become the initiator and will
# create a new transaction context.
# If the participant and initiator are in the same process, this transaction block will register with the local
# initiator via a local function call.
# If the participant and initiator are in different processes, this transaction block will register with the remote
# initiator via a network call.
#
# + transactionId - Globally unique transaction ID. If this is a new transaction which is initiated, then this
#                   will be null.
#                   If this is a participant in an existing transaction, then it will have a value.
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + registerAtUrl - The URL of the initiator
# + coordinationType - Coordination type of this transaction
# + return - Newly created/existing TransactionContext for this transaction.
function beginTransaction(string? transactionId, string transactionBlockId, string registerAtUrl,
                          string coordinationType) returns TransactionContext|error {
    if (transactionId is string) {
        if (initiatedTransactions.hasKey(transactionId)) { // if participant & initiator are in the same process
            // we don't need to do a network call and can simply do a local function call
            return registerLocalParticipantWithInitiator(transactionId, transactionBlockId, registerAtUrl);
        } else {
            //TODO: set the proper protocol
            string protocolName = PROTOCOL_DURABLE;
            RemoteProtocol[] protocols = [{
            name:protocolName, url:getParticipantProtocolAt(protocolName, <@untainted> transactionBlockId)
            }];
            return registerParticipantWithRemoteInitiator(transactionId, transactionBlockId, registerAtUrl, protocols);
        }
    } else {
        return createTransactionContext(coordinationType, transactionBlockId);
    }
}

# When an abort statement is executed, this function gets called.
#
# + transactionId - Globally unique transaction ID.
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + return - nil or error when transaction abortion is successful or not respectively.
function abortTransaction(string transactionId, string transactionBlockId) returns @tainted error? {
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
    var txn = participatedTransactions[participatedTxnId];
    if (txn is TwoPhaseCommitTransaction) {
        return txn.markForAbortion();
    } else {
        var initiatedTxn = initiatedTransactions[transactionId];
        if (initiatedTxn is TwoPhaseCommitTransaction) {
            return initiatedTxn.markForAbortion();
        } else {
            panic TransactionError("Unknown transaction");
        }
    }
}

# When a transaction block in Ballerina code ends, it will call this function to end a transaction.
# Ending a transaction by a participant has no effect because it is the initiator who can decide whether to
# commit or abort a transaction.
# Depending on the state of the transaction, the initiator decides to commit or abort the transaction.
#
# + transactionId - Globally unique transaction ID.
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + return - A string or an error representing the transaction end succcess status or failure respectively.
function endTransaction(string transactionId, string transactionBlockId) returns @tainted string|error {
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
    if (!initiatedTransactions.hasKey(transactionId) && !participatedTransactions.hasKey(participatedTxnId)) {
        panic TransactionError("Transaction: " + participatedTxnId + " not found");
    }

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
                string|error ret = initiatedTxn.twoPhaseCommit();
                removeInitiatedTransaction(transactionId);
                return ret;
            }
        }
    } else {
        return "";  // Nothing to do on endTransaction if you are a participant
    }
}

# Checks whether this instance is an initiator. Returns true if initiator.
#
# + transactionId - Globally unique transaction ID.
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + return - true or false representing whether this instance is an intiator or not.
function isInitiator(string transactionId, string transactionBlockId) returns boolean {
    if (initiatedTransactions.hasKey(transactionId)) {
        string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
        if (!participatedTransactions.hasKey(participatedTxnId)) {
            return true;
        }
    }
    return false;
}

# Wrapper function used to seperate panic and return error after calling participant function.
#
# + trxFunc - Participant logic.
# + return - Return value of the participant.
function transactionParticipantWrapper(function () returns any|error trxFunc) returns ParticipantFunctionResult {
    any|error|() resultData = trxFunc();
    ParticipantFunctionResult result =  {data : resultData};
    return result;
}


# Prepare local resource managers.
#
# + transactionId - Globally unique transaction ID.
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + return - true or false representing whether the resource manager preparation is successful or not.
function prepareResourceManagers(string transactionId, string transactionBlockId) returns boolean = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "prepareResourceManagers"
} external;

# Commit local resource managers.
#
# + transactionId - Globally unique transaction ID.
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + return - true or false representing whether the commit is successful or not.
function commitResourceManagers(string transactionId, string transactionBlockId) returns boolean = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "commitResourceManagers"
} external;

# Abort local resource managers.
#
# + transactionId - Globally unique transaction ID.
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + return - true or false representing whether the resource manager abortion is successful or not.
function abortResourceManagers(string transactionId, string transactionBlockId) returns boolean = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "abortResourceManagers"
} external;

# Get the current transaction id. This function is useful for user code to save state against a transaction ID,
# so that when the `oncommit` or `onabort` functions registered for a transaction can retrieve that state using the
# transaction  that is passed in to those functions.
#
# + return - A string representing the ID of the current transaction.
public function getCurrentTransactionId() returns string = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "getCurrentTransactionId"
} external;

# Checks whether the transaction is nested.
#
# + return - true or false representing whether the transaction is nested.
function isNestedTransaction() returns boolean = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "isNestedTransaction"
} external;

# Set the transactionContext.
#
# + transactionContext - Transaction context.
function setTransactionContext(TransactionContext transactionContext) = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "setTransactionContext"
} external;
 
# Register local participant. Functions with participant annotations will be desugered to below functions.
#
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + committedFunc - Function pointer for commit function for participant.
# + abortedFunc -  Function pointer for abort function for participant.
# + return - Transaction context.
function registerLocalParticipant(string transactionBlockId, function (string trxId) committedFunc,
                                  function (string trxId) abortedFunc) returns TransactionContext? = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "registerLocalParticipant"
} external;

# Register remote participant. Functions with participant annotations will be desugered to below functions.
#
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + committedFunc - Function pointer for commit function for participant.
# + abortedFunc -  Function pointer for abort function for participant.
# + return - Transaction context.
function registerRemoteParticipant(string transactionBlockId, function (string trxId) committedFunc,
                                   function (string trxId) abortedFunc) returns  TransactionContext? = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "registerRemoteParticipant"
} external;

# Notify the transaction resource manager on local participant failture.
function notifyLocalParticipantOnFailure() = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "notifyLocalParticipantOnFailure"
} external;

# Notify the transaction resource manager on remote participant failture.
function notifyRemoteParticipantOnFailure() = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "notifyRemoteParticipantOnFailure"
} external;

# Notify the transaction resource manager on abort.
#
# + transactionBlockId - ID of the transaction block.
function notifyResourceManagerOnAbort(string transactionBlockId) = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "notifyResourceManagerOnAbort"
} external;

# Rollback the transaction.
#
# + transactionBlockId - ID of the transaction block.
function rollbackTransaction(string transactionBlockId) = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "rollbackTransaction"
} external;

# Cleanup the transaction context.
#
# + transactionBlockId - ID of the transaction block.
function cleanupTransactionContext(string transactionBlockId) = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "cleanupTransactionContext"
} external;

# Get and Cleanup the failure.
#
# + return - is failed.
function getAndClearFailure() returns boolean = @java:Method {
    class: "io.ballerina.transactions.Utils",
    name: "getAndClearFailure"
} external;
