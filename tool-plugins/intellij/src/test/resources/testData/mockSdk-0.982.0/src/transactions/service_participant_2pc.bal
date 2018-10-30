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
import ballerina/http;

@http:ServiceConfig {
    basePath:participant2pcCoordinatorBasePath
}
// # Service on the participant which handles protocol messages related to the 2-phase commit (2PC) coordination type.
service Participant2pcService bind coordinatorListener {

    # When the initiator sends "prepare" this resource on the participant will get called.
    # This participant will in turn call prepare on all its resource managers registered with the respective
    # transaction.
    #
    # + transactionBlockId - transaction block ID on the participant. This is sent during registration by the
    #                        participant as part of the participant protocol endpoint. The initiator isn't aware
    #                        of this `transactionBlockId` and will simply send it back as part of the URL it calls.
    @http:ResourceConfig {
        methods:["POST"],
        path:"{transactionBlockId}/prepare",
        body:"prepareReq",
        consumes:["application/json"]
    }
    prepare(endpoint conn, http:Request req, int transactionBlockId, PrepareRequest prepareReq) {
        http:Response res = new;
        string transactionId = prepareReq.transactionId;
        string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
        log:printInfo("Prepare received for transaction: " + participatedTxnId);
        PrepareResponse prepareRes = {};

        match (participatedTransactions[participatedTxnId]) {
            () => {
                res.statusCode = http:NOT_FOUND_404;
                prepareRes.message = TRANSACTION_UNKNOWN;
            }
            TwoPhaseCommitTransaction participatedTxn => {
                if (participatedTxn.state == TXN_STATE_ABORTED) {
                    res.statusCode = http:OK_200;
                    prepareRes.message = PREPARE_RESULT_ABORTED_STR;
                    removeParticipatedTransaction(participatedTxnId);
                } else {
                    // Call prepare on the local resource manager
                    boolean prepareSuccessful = prepareResourceManagers(transactionId, transactionBlockId);
                    if (prepareSuccessful) {
                        res.statusCode = http:OK_200;
                        participatedTxn.state = TXN_STATE_PREPARED;
                        prepareRes.message = PREPARE_RESULT_PREPARED_STR;
                        log:printInfo("Prepared transaction: " + transactionId);
                    } else {
                        res.statusCode = http:OK_200;
                        prepareRes.message = PREPARE_RESULT_ABORTED_STR;
                        participatedTxn.state = TXN_STATE_ABORTED;
                        removeParticipatedTransaction(participatedTxnId);
                        log:printInfo("Aborted transaction: " + transactionId);
                    }
                }
            }
        }
        json j = check <json>prepareRes;
        res.setJsonPayload(j);
        var resResult = conn->respond(res);
        match resResult {
            error err => log:printError("Sending response for prepare request for transaction " +
                    transactionId + " failed", err = err);
            () => {}
        }
    }

    # When the initiator sends "notify(commit | abort)" this resource on the participant will get called.
    # This participant will in turn call "commit" or "abort" on all the resource managers registered with the
    # respective transaction.
    #
    # + transactionBlockId - transaction block ID on the participant. This is sent during registration by the
    #                        participant as part of the participant protocol endpoint. The initiator isn't aware
    #                        of this `transactionBlockId` and will simply send it back as part of the URL it calls.
    @http:ResourceConfig {
        methods:["POST"],
        path:"{transactionBlockId}/notify",
        body:"notifyReq",
        consumes:["application/json"]
    }
    notify(endpoint conn, http:Request req, int transactionBlockId, NotifyRequest notifyReq) {
        http:Response res = new;
        string transactionId = notifyReq.transactionId;
        string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
        log:printInfo("Notify(" + notifyReq.message + ") received for transaction: " + participatedTxnId);
        NotifyResponse notifyRes = {};
        match (participatedTransactions[participatedTxnId]) {
            () => {
                res.statusCode = http:NOT_FOUND_404;
                notifyRes.message = TRANSACTION_UNKNOWN;
            }
            TwoPhaseCommitTransaction txn => {
                if (notifyReq.message == COMMAND_COMMIT) {
                    if (txn.state != TXN_STATE_PREPARED) {
                        res.statusCode = http:BAD_REQUEST_400;
                        notifyRes.message = NOTIFY_RESULT_NOT_PREPARED_STR;
                    } else {
                        // Notify commit to the resource manager
                        boolean commitSuccessful = commitResourceManagers(transactionId, transactionBlockId);
                        if (commitSuccessful) {
                            res.statusCode = http:OK_200;
                            notifyRes.message = PREPARE_RESULT_COMMITTED_STR;
                            txn.state = TXN_STATE_COMMITTED;
                        } else {
                            res.statusCode = http:INTERNAL_SERVER_ERROR_500;
                            log:printError("Committing resource managers failed. Transaction:" + participatedTxnId);
                            notifyRes.message = PREPARE_RESULT_FAILED_STR;
                        }
                    }
                } else if (notifyReq.message == COMMAND_ABORT) {
                    // Notify abort to the resource manager
                    boolean abortSuccessful = abortResourceManagers(transactionId, transactionBlockId);
                    if (abortSuccessful) {
                        res.statusCode = http:OK_200;
                        notifyRes.message = PREPARE_RESULT_ABORTED_STR;
                        txn.state = TXN_STATE_ABORTED;
                    } else {
                        res.statusCode = http:INTERNAL_SERVER_ERROR_500;
                        log:printError("Aborting resource managers failed. Transaction:" + participatedTxnId);
                        notifyRes.message = PREPARE_RESULT_FAILED_STR;
                    }
                }
                removeParticipatedTransaction(participatedTxnId);
            }
        }
        json j = check <json>notifyRes;
        res.setJsonPayload(j);
        var resResult = conn->respond(res);
        match resResult {
            error err => log:printError("Sending response for notify request for transaction " + transactionId +
                    " failed", err = err);
            () => {}
        }
    }
}
