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

import ballerina.log;
import ballerina.net.http;

@http:configuration {
    basePath:initiator2pcCoordinatorBasePath,
    host:coordinatorHost,
    port:coordinatorPort
}
documentation {
    Service on the initiator which handles protocol messages related to the 2-phase commit (2PC) coordination type.
}
service<http> Initiator2pcService {

    @http:resourceConfig {
        methods:["POST"],
        path:"{transactionBlockId}/abort"
    }
    documentation {
        When a participant wants to abort a transaction, it will make a call to this resource.
    }
    resource abortTransaction (http:Connection conn, http:Request req, string transactionBlockId) {
        http:Response res;
        var payload, payloadError = req.getJsonPayload();
        var txnBlockId, txnBlockIdConversionErr = <int>transactionBlockId;

        if (payloadError != null || txnBlockIdConversionErr != null) {
            res = {statusCode:400};
            RequestError err = {errorMessage:"Bad Request"};
            var resPayload, _ = <json>err;
            res.setJsonPayload(resPayload);
            var connError = conn.respond(res);
            if (connError != null) {
                log:printErrorCause("Sending response to Bad Request for abort transaction request failed", (error)connError);
            }
        } else {
            var abortReq, _ = <AbortRequest>payload;
            string transactionId = abortReq.transactionId;
            string participantId = abortReq.participantId;
            log:printInfo("Abort received for transaction: " + transactionId + " from participant:" + participantId);
            AbortResponse abortRes;
            var txn, _ = (TwoPhaseCommitTransaction)initiatedTransactions.get(transactionId);
            if (txn == null) {
                res = {statusCode:404};
                abortRes = {message:"Transaction-Unknown"};
            } else {
                // Remove the participant who sent the abort since we don't want to do a notify(Abort) to that
                // participant
                _ = txn.participants.remove(participantId);
                var msg, err = abortInitiatorTransaction(transactionId, txnBlockId);
                if (err == null) {
                    res = {statusCode:500};
                    abortRes = {message:"Abort-Failed"};
                } else {
                    res = {statusCode:200};
                    abortRes = {message:msg};
                }
            }
            var j, _ = <json>abortRes;
            res.setJsonPayload(j);
            error e = (error)conn.respond(res);
            if (e != null) {
                log:printErrorCause("Cannot respond to abort request for transaction: " + transactionId + " from participant:" +
                                    participantId, e);
            }
        }
    }
}
