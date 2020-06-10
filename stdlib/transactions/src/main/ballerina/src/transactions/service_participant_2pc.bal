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
    basePath:"/balcoordinator/participant/2pc"
}
// # Service on the participant which handles protocol messages related to the 2-phase commit (2PC) coordination type.
service Participant2pcService on coordinatorListener {

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
    resource function prepare(http:Caller conn, http:Request req, string transactionBlockId, PrepareRequest prepareReq) {
        http:Response res = new;
        string transactionId = prepareReq.transactionId;
        string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
        log:printInfo("Prepare received for transaction: " + participatedTxnId);
        PrepareResponse prepareRes = {};

        var participatedTxn = participatedTransactions[participatedTxnId];
        if (participatedTxn is ()) {
            res.statusCode = http:STATUS_NOT_FOUND;
            prepareRes.message = TRANSACTION_UNKNOWN;
        } else {
            if (participatedTxn.state == TXN_STATE_ABORTED) {
                res.statusCode = http:STATUS_OK;
                prepareRes.message = PREPARE_RESULT_ABORTED_STR;
                removeParticipatedTransaction(participatedTxnId);
            } else {
                // Call prepare on the local resource manager
                boolean prepareSuccessful = prepareResourceManagers(transactionId, transactionBlockId);
                if (prepareSuccessful) {
                    res.statusCode = http:STATUS_OK;
                    participatedTxn.state = TXN_STATE_PREPARED;
                    prepareRes.message = PREPARE_RESULT_PREPARED_STR;
                    log:printInfo("Prepared transaction: " + transactionId);
                } else {
                    res.statusCode = http:STATUS_OK;
                    prepareRes.message = PREPARE_RESULT_ABORTED_STR;
                    participatedTxn.state = TXN_STATE_ABORTED;
                    removeParticipatedTransaction(participatedTxnId);
                    log:printInfo("Aborted transaction: " + transactionId);
                }
            }
        }

        var jsonResponse = prepareRes.cloneWithType(typedesc<json>);
        if (jsonResponse is json) {
            res.setJsonPayload(jsonResponse);
            var resResult = conn->respond(res);
            if (resResult is error) {
                log:printError("Sending response for prepare request for transaction " +
                transactionId + " failed", resResult);
            }
        } else {
            panic jsonResponse;
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
    resource function notify(http:Caller conn, http:Request req, string transactionBlockId, NotifyRequest notifyReq) {
        http:Response res = new;
        string transactionId = notifyReq.transactionId;
        string participatedTxnId = getParticipatedTransactionId(transactionId, transactionBlockId);
        log:printInfo("Notify(" + notifyReq.message + ") received for transaction: " + participatedTxnId);
        NotifyResponse notifyRes = {};
        var txn = participatedTransactions[participatedTxnId];
        if (txn is ()) {
            res.statusCode = http:STATUS_NOT_FOUND;
            notifyRes.message = TRANSACTION_UNKNOWN;
        } else {
            if (notifyReq.message == COMMAND_COMMIT) {
                if (txn.state != TXN_STATE_PREPARED) {
                    res.statusCode = http:STATUS_BAD_REQUEST;
                    notifyRes.message = NOTIFY_RESULT_NOT_PREPARED_STR;
                } else {
                    // Notify commit to the resource manager
                    boolean commitSuccessful = commitResourceManagers(transactionId, transactionBlockId);
                    if (commitSuccessful) {
                        res.statusCode = http:STATUS_OK;
                        notifyRes.message = PREPARE_RESULT_COMMITTED_STR;
                        txn.state = TXN_STATE_COMMITTED;
                    } else {
                        res.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
                        log:printError("Committing resource managers failed. Transaction:" + participatedTxnId);
                        notifyRes.message = PREPARE_RESULT_FAILED_STR;
                    }
                }
            } else if (notifyReq.message == COMMAND_ABORT) {
                // Notify abort to the resource manager
                boolean abortSuccessful = abortResourceManagers(transactionId, transactionBlockId);
                if (abortSuccessful) {
                    res.statusCode = http:STATUS_OK;
                    notifyRes.message = PREPARE_RESULT_ABORTED_STR;
                    txn.state = TXN_STATE_ABORTED;
                } else {
                    res.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
                    log:printError("Aborting resource managers failed. Transaction:" + participatedTxnId);
                    notifyRes.message = PREPARE_RESULT_FAILED_STR;
                }
            }
            removeParticipatedTransaction(participatedTxnId);
        }

        var jsonResponse = notifyRes.cloneWithType(typedesc<json>);
        if (jsonResponse is json) {
            res.setJsonPayload(jsonResponse);
            var resResult = conn->respond(res);
            if (resResult is http:ListenerError) {
                log:printError("Sending response for notify request for transaction " + transactionId +
                        " failed", resResult);
            }
        } else {
            panic jsonResponse;
        }
    }
}
