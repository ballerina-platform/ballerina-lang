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
import ballerina/net.http;

@http:ServiceConfig {
    basePath:participant2pcCoordinatorBasePath
}
documentation {
    Service on the participant which handles protocol messages related to the 2-phase commit (2PC) coordination type.
}
service<http:Service> Participant2pcService bind coordinatorServerEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"{transactionBlockId}/prepare",
        body:"prepareReq",
        consumes:["application/json"]
    }
    documentation {
        When the initiator sends "prepare" this resource on the participant will get called.
        This participant will in turn call prepare on all its resource managers registered with the respective
        transaction.

        P{{transactionBlockId}} - transaction block ID on the participant. This is sent during registration by the
                                  participant as part of the participant protocol endpoint. The initiator isn't aware
                                  of this `transactionBlockId` and will simply send it back as part of the URL it calls.
    }
    prepare (endpoint conn, http:Request req, int transactionBlockId, PrepareRequest prepareReq) {
        http:Response res = {};
        string transactionId = prepareReq.transactionId;
        string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
        log:printInfo("Prepare received for transaction: " + participatedTxnId);
        PrepareResponse prepareRes = {};

        if (!participatedTransactions.hasKey(participatedTxnId)) {
            res.statusCode = 404;
            prepareRes.message = "Transaction-Unknown";
        } else {
            TwoPhaseCommitTransaction txn =? <TwoPhaseCommitTransaction>participatedTransactions[participatedTxnId];
            if (txn.state == TransactionState.ABORTED) {
                res.statusCode = 200;
                prepareRes.message = "aborted";
                removeParticipatedTransaction(participatedTxnId);
            } else {
                // Call prepare on the local resource manager
                boolean prepareSuccessful = prepareResourceManagers(transactionId, transactionBlockId);
                if (prepareSuccessful) {
                    res.statusCode = 200;
                    txn.state = TransactionState.PREPARED;
                    //PrepareResponse prepareRes = {message:"read-only"};
                    prepareRes.message = "prepared";
                    log:printInfo("Prepared transaction: " + transactionId);
                } else {
                    res.statusCode = 200;
                    prepareRes.message = "aborted";
                    txn.state = TransactionState.ABORTED;
                    removeParticipatedTransaction(participatedTxnId);
                    log:printInfo("Aborted transaction: " + transactionId);
                }
            }
        }
        json j =? <json>prepareRes;
        res.setJsonPayload(j);
        http:HttpConnectorError connErr = conn -> respond(res);
        match connErr {
            error err => log:printErrorCause("Sending response for prepare request for transaction " + transactionId +
                                             " failed", err);
        }
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"{transactionBlockId}/notify",
        body:"notifyReq",
        consumes:["application/json"]
    }
    documentation {
        When the initiator sends "notify(commit | abort)" this resource on the participant will get called.
        This participant will in turn call "commit" or "abort" on all the resource managers registered with the
        respective transaction.

        P{{transactionBlockId}} - transaction block ID on the participant. This is sent during registration by the
                                  participant as part of the participant protocol endpoint. The initiator isn't aware
                                  of this `transactionBlockId` and will simply send it back as part of the URL it calls.
    }
    notify (endpoint conn, http:Request req, int transactionBlockId, NotifyRequest notifyReq) {
        http:Response res = {};
        string transactionId = notifyReq.transactionId;
        string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
        log:printInfo("Notify(" + notifyReq.message + ") received for transaction: " + participatedTxnId);
        NotifyResponse notifyRes = {};
        if (!participatedTransactions.hasKey(participatedTxnId)) {
            res.statusCode = 404;
            notifyRes.message = "Transaction-Unknown";
        } else {
            TwoPhaseCommitTransaction txn =? <TwoPhaseCommitTransaction>participatedTransactions[participatedTxnId];
            if (notifyReq.message == "commit") {
                if (txn.state != TransactionState.PREPARED) {
                    res.statusCode = 400;
                    notifyRes.message = "Not-Prepared";
                } else {
                    // Notify commit to the resource manager
                    boolean commitSuccessful = commitResourceManagers(transactionId, transactionBlockId);
                    if (commitSuccessful) {
                        res.statusCode = 200;
                        notifyRes.message = "Committed";
                        txn.state = TransactionState.COMMITTED;
                    } else {
                        res.statusCode = 500;
                        log:printError("Committing resource managers failed. Transaction:" + participatedTxnId);
                        notifyRes.message = "Failed-EOT";
                    }
                }
            } else if (notifyReq.message == "abort") {
                // Notify abort to the resource manager
                boolean abortSuccessful = abortResourceManagers(transactionId, transactionBlockId);
                if (abortSuccessful) {
                    res.statusCode = 200;
                    notifyRes.message = "Aborted";
                    txn.state = TransactionState.ABORTED;
                } else {
                    res.statusCode = 500;
                    log:printError("Aborting resource managers failed. Transaction:" + participatedTxnId);
                    notifyRes.message = "Failed-EOT";
                }
            }
            removeParticipatedTransaction(participatedTxnId);
        }
        json j =? <json>notifyRes;
        res.setJsonPayload(j);
        var connErr = conn -> respond(res);
        match connErr {
            error err => log:printErrorCause("Sending response for notify request for transaction " + transactionId +
                                             " failed", err);
        }
    }
}
