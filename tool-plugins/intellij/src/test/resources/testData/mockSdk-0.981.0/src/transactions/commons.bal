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

documentation {
    ID of the local participant used when registering with the initiator.
}
string localParticipantId = system:uuid();

documentation {
    This map is used for caching transaction that are initiated.
}
map<TwoPhaseCommitTransaction> initiatedTransactions;

documentation {
    This map is used for caching transaction that are this Ballerina instance participates in.
}
map<TwoPhaseCommitTransaction> participatedTransactions;

documentation {
    This cache is used for caching HTTP connectors against the URL, since creating connectors is expensive.
}
cache:Cache httpClientCache = new;

@final boolean scheduleInit = scheduleTimer(1000, 60000);

function scheduleTimer(int delay, int interval) returns boolean {
    (function() returns error?) onTriggerFunction = cleanupTransactions;
    task:Timer timer = new(onTriggerFunction, (), interval, delay = delay);
    timer.start();
    return true;
}

function cleanupTransactions() returns error? {
    worker w1 {
        foreach _, twopcTxn in participatedTransactions {
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
    worker w2 {
        foreach _, twopcTxn in initiatedTransactions {
            if (time:currentTime().time - twopcTxn.createdTime >= 120000) {
                if (twopcTxn.state != TXN_STATE_ABORTED) {
                    // Commit the transaction since prepare hasn't been received
                    var result = twopcTxn.twoPhaseCommit();
                    match result {
                        string str => log:printInfo("Auto-committed initiated transaction: " + twopcTxn.transactionId +
                                ". Result: " + str);
                        error err => log:printError("Auto-commit of participated transaction: " +
                                twopcTxn.transactionId + " failed", err = err);
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
}


function isRegisteredParticipant(string participantId, map<Participant> participants) returns boolean {
    return participants.hasKey(participantId);
}

function isValidCoordinationType(string coordinationType) returns boolean {
    foreach coordType in coordinationTypes {
        if (coordinationType == coordType) {
            return true;
        }
    }
    return false;
}

function protocolCompatible(string coordinationType, Protocol[] participantProtocols) returns boolean {
    boolean participantProtocolIsValid = false;
    string[] validProtocols = coordinationTypeToProtocolsMap[coordinationType] but { () => [] };
    foreach participantProtocol in participantProtocols {
        foreach validProtocol in validProtocols {
            if (participantProtocol.name == validProtocol) {
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

function respondToBadRequest(http:Listener conn, string msg) {
    endpoint http:Listener ep = conn;
    log:printError(msg);
    http:Response res = new;  res.statusCode = http:BAD_REQUEST_400;
    RequestError err = {errorMessage:msg};
    json resPayload = check <json>err;
    res.setJsonPayload(untaint resPayload);
    var resResult = ep->respond(res);
    match resResult {
        error respondErr => {
            log:printError("Could not send Bad Request error response to caller", err = respondErr);
        }
        () => return;
    }
}

function getCoordinatorProtocolAt(string protocolName, int transactionBlockId) returns string {
    //TODO: protocolName is unused for the moment
    return "http://" + coordinatorHost + ":" + coordinatorPort + initiator2pcCoordinatorBasePath + "/" +
        transactionBlockId;
}

function getParticipantProtocolAt(string protocolName, int transactionBlockId) returns string {
    //TODO: protocolName is unused for the moment
    return "http://" + coordinatorHost + ":" + coordinatorPort + participant2pcCoordinatorBasePath + "/" +
        transactionBlockId;
}

documentation {
    The initiator will create a new transaction context by calling this function. At this point, a transaction object
    corresponding to the coordinationType will also be created and stored as an initiated transaction.
}
function createTransactionContext(string coordinationType, int transactionBlockId) returns TransactionContext|error {
    if (!isValidCoordinationType(coordinationType)) {
        string msg = "Invalid-Coordination-Type:" + coordinationType;
        log:printError(msg);
        error err = {message:msg};
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

documentation {
    Register a local participant, which corresponds to a nested transaction of the initiated transaction, with the
    initiator. Such participants and the initiator don't have to communicate over the network, so we are special casing
    such participants.
}
function registerLocalParticipantWithInitiator(string transactionId, int transactionBlockId, string registerAtURL)
    returns TransactionContext|error {

    string participantId = getParticipantId(transactionBlockId);
    //TODO: Protocol name should be passed down from the transaction statement
    LocalProtocol participantProtocol = {name:PROTOCOL_DURABLE};
    match (initiatedTransactions[transactionId]) {
        () => {
            error err = {message:"Transaction-Unknown. Invalid TID:" + transactionId};
            return err;
        }
        TwoPhaseCommitTransaction initiatedTxn => {
            if (isRegisteredParticipant(participantId, initiatedTxn.participants)) { // Already-Registered
                error err = {message:"Already-Registered. TID:" + transactionId + ",participant ID:" + participantId};
                return err;
            } else if (!protocolCompatible(initiatedTxn.coordinationType, [participantProtocol])) { // Invalid-Protocol
                error err = {message:"Invalid-Protocol in local participant. TID:" + transactionId + ",participant ID:" +
                    participantId};
                return err;
            } else {
    
                //Set initiator protocols
                TwoPhaseCommitTransaction participatedTxn = new(transactionId, transactionBlockId);
                //Protocol initiatorProto = {name: PROTOCOL_DURABLE, transactionBlockId:transactionBlockId};
                //participatedTxn.coordinatorProtocols = [initiatorProto];
    
                LocalParticipant participant = new(participantId, participatedTxn, [participantProtocol]);
                initiatedTxn.participants[participantId] = <Participant>participant;
    
                string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
                participatedTransactions[participatedTxnId] = participatedTxn;
                TransactionContext txnCtx = {transactionId:transactionId, transactionBlockId:transactionBlockId,
                    coordinationType:TWO_PHASE_COMMIT, registerAtURL:registerAtURL};
                log:printInfo("Registered local participant: " + participantId + " for transaction:" + transactionId);
                return txnCtx;
            }
        }
    }
}

function removeParticipatedTransaction(string participatedTxnId) {
    boolean removed = participatedTransactions.remove(participatedTxnId);
    if (!removed) {
        error err = {message:"Removing participated transaction: " + participatedTxnId + " failed"};
        throw err;
    }
}

function removeInitiatedTransaction(string transactionId) {
    boolean removed = initiatedTransactions.remove(transactionId);
    if (!removed) {
        error err = {message:"Removing initiated transaction: " + transactionId + " failed"};
        throw err;
    }
}

function getInitiatorClient(string registerAtURL) returns InitiatorClientEP {
    if (httpClientCache.hasKey(registerAtURL)) {
        InitiatorClientEP initiatorEP = check <InitiatorClientEP>httpClientCache.get(registerAtURL);
        return initiatorEP;
    } else {
        lock {
            if (httpClientCache.hasKey(registerAtURL)) {
                InitiatorClientEP initiatorEP = check <InitiatorClientEP>httpClientCache.get(registerAtURL);
                return initiatorEP;
            }
            InitiatorClientEP initiatorEP = new;
            InitiatorClientConfig config = { registerAtURL: registerAtURL,
                timeoutMillis: 15000, retryConfig: { count: 2, interval: 5000 }
            };
            initiatorEP.init(config);
            httpClientCache.put(registerAtURL, initiatorEP);
            return initiatorEP;
        }
    }
}

function getParticipant2pcClient(string participantURL) returns Participant2pcClientEP {
    if (httpClientCache.hasKey(participantURL)) {
        Participant2pcClientEP participantEP = check <Participant2pcClientEP>httpClientCache.get(participantURL);
        return participantEP;
    } else {
        lock {
            if (httpClientCache.hasKey(participantURL)) {
                Participant2pcClientEP participantEP = check <
                Participant2pcClientEP>httpClientCache.get(participantURL);
                return participantEP;
            }
            Participant2pcClientEP participantEP = new;
            Participant2pcClientConfig config = { participantURL: participantURL,
                timeoutMillis: 15000, retryConfig: { count: 2, interval: 5000 }
            };
            participantEP.init(config);
            httpClientCache.put(participantURL, participantEP);
            return participantEP;
        }
    }
}

documentation {
    Registers a participant with the initiator's coordinator. This function will be called by the participant

    P{{transactionId}} - ID of the transaction to which this participant is registering with
    P{{transactionBlockId}} - The local ID of the transaction block on the participant
    P{{registerAtURL}} - The URL of the initiator to which this participant will register with
}
public function registerParticipantWithRemoteInitiator(string transactionId, int transactionBlockId,
                                                       string registerAtURL, RemoteProtocol[] participantProtocols)
    returns TransactionContext|error {

    endpoint InitiatorClientEP initiatorEP;
    initiatorEP = getInitiatorClient(registerAtURL);
    string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);

    // Register with the coordinator only if the participant has not already done so
    if (participatedTransactions.hasKey(participatedTxnId)) {
        string msg = "Already registered with initiator for transaction:" + participatedTxnId;
        log:printError(msg);
        error err = {message:msg};
        return err;
    }
    log:printInfo("Registering for transaction: " + participatedTxnId + " with coordinator: " + registerAtURL);

    var result = initiatorEP->register(transactionId, transactionBlockId, participantProtocols);
    match result {
        error e => {
            string msg = "Cannot register with coordinator for transaction: " + transactionId;
            log:printError(msg, err = e);
            error err = {message:msg, cause:e};
            return err;
        }
        RegistrationResponse regRes => {
            RemoteProtocol[] coordinatorProtocols = regRes.coordinatorProtocols;
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
}

function getParticipatedTransactionId(string transactionId, int transactionBlockId) returns string {
    string id = transactionId + ":" + transactionBlockId;
    return id;
}

function getParticipantId(int transactionBlockId) returns string {
    string participantId = localParticipantId + ":" + transactionBlockId;
    return participantId;
}

extern function getAvailablePort() returns int;

extern function getHostAddress() returns string;
