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

import ballerina/cache;
import ballerina/log;
import ballerina/http;
import ballerina/system;
import ballerina/task;
import ballerina/time;

# ID of the local participant used when registering with the initiator.
string localParticipantId = system:uuid();

# This map is used for caching transaction that are initiated.
map<TwoPhaseCommitTransaction> initiatedTransactions = {};

# This map is used for caching transaction that are this Ballerina instance participates in.
map<TwoPhaseCommitTransaction> participatedTransactions = {};

# This cache is used for caching HTTP connectors against the URL, since creating connectors is expensive.
cache:Cache httpClientCache = new;

final boolean scheduleInit = scheduleTimer(1000, 60000);

function scheduleTimer(int delay, int interval) returns boolean {
    (function() returns error?) onTriggerFunction = cleanupTransactions;
    task:Timer timer = new(onTriggerFunction, (), interval, delay = delay);
    timer.start();
    return true;
}

function cleanupTransactions() returns error? {
    worker w1 {
        foreach var (key, twopcTxn) in participatedTransactions {
            string participatedTxnId = getParticipatedTransactionId(twopcTxn.transactionId,
                twopcTxn.transactionBlockId);
            if (time:currentTime().time - twopcTxn.createdTime >= 120000) {
                if (twopcTxn.state != TXN_STATE_ABORTED && twopcTxn.state != TXN_STATE_COMMITTED) {
                    if (twopcTxn.state != TXN_STATE_PREPARED) {
                        boolean prepareSuccessful =
                            prepareResourceManagers(twopcTxn.transactionId, twopcTxn.transactionBlockId);
                        if (prepareSuccessful) {
                            twopcTxn.state = TXN_STATE_PREPARED;
                            log:printInfo("Auto-prepared participated  transaction: " + participatedTxnId);
                        } else {
                            log:printError("Auto-prepare of participated transaction: " + participatedTxnId +
                                    " failed");
                        }
                    }
                    if (twopcTxn.state == TXN_STATE_PREPARED) {
                        boolean commitSuccessful = commitResourceManagers(twopcTxn.transactionId,
                            twopcTxn.transactionBlockId);
                        if (commitSuccessful) {
                            twopcTxn.state = TXN_STATE_COMMITTED;
                            log:printInfo("Auto-committed participated  transaction: " + participatedTxnId);
                            removeParticipatedTransaction(participatedTxnId);
                        } else {
                            log:printError("Auto-commit of participated transaction: " + participatedTxnId + " failed");
                        }
                    }
                }
            }
            if (time:currentTime().time - twopcTxn.createdTime >= 600000) {
                // We don't want dead transactions hanging around
                removeParticipatedTransaction(participatedTxnId);
            }
        }
    }
    worker w2 returns () {
        foreach var (key, twopcTxn) in initiatedTransactions {
            if (time:currentTime().time - twopcTxn.createdTime >= 120000) {
                if (twopcTxn.state != TXN_STATE_ABORTED) {
                    // Commit the transaction since prepare hasn't been received
                    var result = twopcTxn.twoPhaseCommit();
                    if (result is string) {
                        log:printInfo("Auto-committed initiated transaction: " + twopcTxn.transactionId +
                                ". Result: " + result);
                        removeInitiatedTransaction(twopcTxn.transactionId);
                    } else {
                        log:printError("Auto-commit of participated transaction: " +
                        twopcTxn.transactionId + " failed", err = result);
                    }
                }
            }
            if (time:currentTime().time - twopcTxn.createdTime >= 600000) {
                // We don't want dead transactions hanging around
                removeInitiatedTransaction(twopcTxn.transactionId);
            }
        }
        return ();
    }
    var value = wait w2;
    return value;
}


function isRegisteredParticipant(string participantId, map<Participant> participants) returns boolean {
    return participants.hasKey(participantId);
}

function isValidCoordinationType(string coordinationType) returns boolean {
    foreach var coordType in coordinationTypes {
        if (coordinationType == coordType) {
            return true;
        }
    }
    return false;
}

function protoName(UProtocol p) returns string {
    if (p is LocalProtocol) {
        return p.name;
    } else {
        return <string> p.name;
    }
}

function protocolCompatible(string coordinationType, UProtocol?[] participantProtocols) returns boolean {
    boolean participantProtocolIsValid = false;
    string[] validProtocols = coordinationTypeToProtocolsMap[coordinationType] ?: [];
    foreach var p in participantProtocols {
        UProtocol participantProtocol = p;
        foreach var validProtocol in validProtocols {
            if (protoName(participantProtocol) == validProtocol) {
                participantProtocolIsValid = true;
                break;
            } else {
                participantProtocolIsValid = false;
            }
        }
        if (!participantProtocolIsValid) {
            break;
        }
    }
    return participantProtocolIsValid;
}

function respondToBadRequest(http:Caller ep, string msg) {
    log:printError(msg);
    http:Response res = new;  res.statusCode = http:BAD_REQUEST_400;
    RequestError requestError = {errorMessage:msg};
    var resPayload = json.convert(requestError);
    if (resPayload is json) {
        res.setJsonPayload(untaint resPayload);
        var resResult = ep->respond(res);
        if (resResult is error) {
            log:printError("Could not send Bad Request error response to caller", err = resResult);
        } else {
            return;
        }
    } else {
        panic resPayload;
    }
}

function getCoordinatorProtocolAt(string protocolName, string transactionBlockId) returns string {
    //TODO: protocolName is unused for the moment
    return "http://" + coordinatorHost + ":" + coordinatorPort + initiator2pcCoordinatorBasePath + "/" +
        transactionBlockId;
}

function getParticipantProtocolAt(string protocolName, string transactionBlockId) returns string {
    //TODO: protocolName is unused for the moment
    return "http://" + coordinatorHost + ":" + coordinatorPort + participant2pcCoordinatorBasePath + "/" +
        transactionBlockId;
}

# The initiator will create a new transaction context by calling this function. At this point, a transaction object
# corresponding to the coordinationType will also be created and stored as an initiated transaction.
#
# + coordinationType - The type of the coordination relevant to the transaction block for which this TransactionContext
#                      is being created for.
# + transactionBlockId - The ID of the transaction block.
# + return - TransactionContext if the coordination type is valid or an error in case of an invalid coordination type.
function createTransactionContext(string coordinationType, string transactionBlockId) returns TransactionContext|error {
    if (!isValidCoordinationType(coordinationType)) {
        string msg = "Invalid-Coordination-Type:" + coordinationType;
        log:printError(msg);
        error err = error(msg);
        return err;
    } else {
        TwoPhaseCommitTransaction txn = new(system:uuid(), transactionBlockId, coordinationType = coordinationType);
        string txnId = txn.transactionId;
        txn.isInitiated = true;
        initiatedTransactions[txnId] = txn;
        TransactionContext txnContext = {
            transactionId:txnId,
            transactionBlockId:transactionBlockId,
            coordinationType:coordinationType,
            registerAtURL:"http://" + coordinatorHost + ":" + coordinatorPort + initiatorCoordinatorBasePath + "/" +
                transactionBlockId + registrationPath
        };
        log:printInfo("Created transaction: " + txnId);
        return txnContext;
    }
}

# Register a local participant, which corresponds to a nested transaction of the initiated transaction, with the
# initiator. Such participants and the initiator don't have to communicate over the network, so we are special casing
# such participants.
#
# + transactionId - Globally unique transaction ID
# + transactionBlockId - ID of the transaction block. Each transaction block in a process has a unique ID.
# + registerAtURL - The URL of the initiator
# + return - TransactionContext if the registration is successul or an error in case of a failure.
function registerLocalParticipantWithInitiator(string transactionId, string transactionBlockId, string registerAtURL)
    returns TransactionContext|error {

    string participantId = getParticipantId(transactionBlockId);
    //TODO: Protocol name should be passed down from the transaction statement
    LocalProtocol participantProtocol = {name:PROTOCOL_DURABLE};
    var initiatedTxn = initiatedTransactions[transactionId];
    if (initiatedTxn is ()) {
        error err = error("Transaction-Unknown. Invalid TID:" + transactionId);
        return err;
    } else {
        if (isRegisteredParticipant(participantId, initiatedTxn.participants)) { // Already-Registered
            error err = error("Already-Registered. TID:" + transactionId + ",participant ID:" + participantId);
            return err;
        } else if (!protocolCompatible(initiatedTxn.coordinationType, [participantProtocol])) { // Invalid-Protocol
            error err = error("Invalid-Protocol in local participant. TID:" + transactionId + ",participant ID:" +
            participantId);
            return err;
        } else {
            //Set initiator protocols
            TwoPhaseCommitTransaction participatedTxn = new(transactionId, transactionBlockId);
            //Protocol initiatorProto = {name: PROTOCOL_DURABLE, transactionBlockId:transactionBlockId};
            //participatedTxn.coordinatorProtocols = [initiatorProto];

            LocalParticipant participant = new(participantId, participatedTxn, [participantProtocol]);
            initiatedTxn.participants[participantId] = participant;

            string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
            participatedTransactions[participatedTxnId] = participatedTxn;
            TransactionContext txnCtx = {transactionId:transactionId, transactionBlockId:transactionBlockId,
            coordinationType:TWO_PHASE_COMMIT, registerAtURL:registerAtURL};
            log:printInfo("Registered local participant: " + participantId + " for transaction:" + transactionId);
            return txnCtx;
        }
    }
}

function removeParticipatedTransaction(string participatedTxnId) {
    boolean removed = participatedTransactions.remove(participatedTxnId);
    if (!removed) {
        error err = error("Removing participated transaction: " + participatedTxnId + " failed");
        panic err;
    }
}

function removeInitiatedTransaction(string transactionId) {
    boolean removed = initiatedTransactions.remove(transactionId);
    if (!removed) {
        error err = error("Removing initiated transaction: " + transactionId + " failed");
        panic err;
    }
}

function getInitiatorClient(string registerAtURL) returns InitiatorClientEP {
    InitiatorClientEP initiatorEP;
    if (httpClientCache.hasKey(registerAtURL)) {
        return <InitiatorClientEP>httpClientCache.get(registerAtURL);
    } else {
        lock {
            if (httpClientCache.hasKey(registerAtURL)) {
                return <InitiatorClientEP>httpClientCache.get(registerAtURL);
            }
            initiatorEP = new({ registerAtURL: registerAtURL, timeoutMillis: 15000,
                retryConfig: { count: 2, interval: 5000 }
            });
            httpClientCache.put(registerAtURL, initiatorEP);
            return initiatorEP;
        }
    }
}

function getParticipant2pcClient(string participantURL) returns Participant2pcClientEP {
    Participant2pcClientEP participantEP;
    if (httpClientCache.hasKey(participantURL)) {
        return <Participant2pcClientEP>httpClientCache.get(participantURL);
    } else {
        lock {
            if (httpClientCache.hasKey(participantURL)) {
                return <Participant2pcClientEP>httpClientCache.get(participantURL);
            }
            participantEP = new({ participantURL: participantURL,
                timeoutMillis: 15000, retryConfig: { count: 2, interval: 5000 }
            });
            httpClientCache.put(participantURL, participantEP);
            return participantEP;
        }
    }
}

# Registers a participant with the initiator's coordinator. This function will be called by the participant.
#
# + transactionId -  Global transaction ID to which this participant is registering with.
# + transactionBlockId - The local ID of the transaction block on the participant.
# + registerAtURL - The URL of the coordinator.
# + participantProtocols - The coordination protocals supported by the participant.
# + return - TransactionContext if the registration is successful or an error in case of a failure.
public function registerParticipantWithRemoteInitiator(string transactionId, string transactionBlockId,
                                                       string registerAtURL, RemoteProtocol[] participantProtocols)
    returns TransactionContext|error {

    InitiatorClientEP initiatorEP = getInitiatorClient(registerAtURL);
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);

    // Register with the coordinator only if the participant has not already done so
    if (participatedTransactions.hasKey(participatedTxnId)) {
        string msg = "Already registered with initiator for transaction:" + participatedTxnId;
        log:printError(msg);
        error err = error(msg);
        return err;
    }
    log:printInfo("Registering for transaction: " + participatedTxnId + " with coordinator: " + registerAtURL);

    var result = initiatorEP->register(transactionId, transactionBlockId, participantProtocols);
    if (result is error) {
        string msg = "Cannot register with coordinator for transaction: " + transactionId;
        log:printError(msg, err = result);
        // TODO : Fix me.
        //map data = { cause: err };
        error err = error(msg);
        return err;
    } else {
        RemoteProtocol[] coordinatorProtocols = result.coordinatorProtocols;
        TwoPhaseCommitTransaction twopcTxn = new(transactionId, transactionBlockId);
        twopcTxn.coordinatorProtocols = toProtocolArray(coordinatorProtocols);
        participatedTransactions[participatedTxnId] = twopcTxn;
        TransactionContext txnCtx = {
            transactionId:transactionId, transactionBlockId:transactionBlockId,
            coordinationType:TWO_PHASE_COMMIT, registerAtURL:registerAtURL
        };
        log:printInfo("Registered with coordinator for transaction: " + transactionId);
        return txnCtx;
    }
}

function getParticipatedTransactionId(string transactionId, string transactionBlockId) returns string {
    string id = transactionId + ":" + transactionBlockId;
    return id;
}

function getParticipantId(string transactionBlockId) returns string {
    string participantId = localParticipantId + ":" + transactionBlockId;
    return participantId;
}

extern function getAvailablePort() returns int;

extern function getHostAddress() returns string;
