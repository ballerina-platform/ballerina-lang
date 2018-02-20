// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina.net.http;
import ballerina.log;

@http:configuration {
    basePath:"/2pc",
    host:coordinatorHost,
    port:coordinatorPort
}
service<http> twoPcCoordinator {

    @http:resourceConfig {
        path:"/commit"
    }
    resource commitTransaction (http:Connection conn, http:InRequest req) {
        //The following command is used to end a micro-transaction successfully,
        // i.e. committing all modifications of all participants. As a result, the coordinator
        // will initiate a prepare() (see section 2.2.5) for each participant.
        //
        //                                                         commit(in: Micro-Transaction-Identifier,
        //                                                         out: ( Committed | Aborted | Mixed )?,
        //                                                         fault: ( Micro-Transaction-Unknown |
        //                                                         Hazard-Outcome ? )
        //
        // The input parameter Micro-Transaction-Identifier is the globally unique identifier of the
        // micro-transaction the participant requests to commit. If the joint outcome is “commit” the output will be
        // Committed. If the joint outcome is “abort”, the output will be Aborted. In case at least one participant
        // performed its commit processing before it had been asked to vote on the joint outcome (e.g. because it was
        // blocking too long) but another participant voted “abort”, no joint outcome can be achieved and Mixed will be
        // the output.

        // If the Micro-Transaction-Identifier is not known to the coordinator, the following fault will be returned.
        // Micro-Transaction-Unknown

        // If at least one of the participants could not end its branch of the micro-transaction as requested
        // (see section 2.2.6), the following fault will be returned:

        // Hazard-Outcome

        var commitReq, e = <CommitRequest>req.getJsonPayload();
        http:OutResponse res;
        if (e != null) {
            res = respondToBadRequest("Malformed request");
            var connError = conn.respond(res);
            if (connError != null) {
                log:printErrorCause("Sending response for malformed commit request failed", (error)connError);
            }
        } else {
            string txnId = commitReq.transactionId;
            var txn, _ = (TwoPhaseCommitTransaction)transactions[txnId];
            if (txn == null) {
                res = respondToBadRequest("Transaction-Unknown. Invalid TID:" + txnId);
                var connError = conn.respond(res);
                if (connError != null) {
                    log:printErrorCause("Sending response to commit request with null transaction ID failed",
                                        (error)connError);
                }
            } else {
                log:printInfo("Committing transaction: " + txnId);
                // return response to the initiator. ( Committed | Aborted | Mixed )
                var msg, err = twoPhaseCommit(txn);
                if (err == null) {
                    CommitResponse commitRes = {message:msg};
                    var resPayload, _ = <json>commitRes;
                    res = {statusCode:200};
                    res.setJsonPayload(resPayload);
                } else {
                    res = {statusCode:500}; //TODO: Not sure about this status code
                    var resPayload, _ = <json>err;
                    res.setJsonPayload(resPayload);
                }
                transactions.remove(txnId);
                var connError = conn.respond(res);
                if (connError != null) {
                    log:printErrorCause("Sending response for commit request for transaction " + txnId + " failed",
                                        (error)connError);
                }
            }
        }

    }

    @http:resourceConfig {
        path:"/abort"
    }
    resource abortTransaction (http:Connection conn, http:InRequest req) {
        var abortReq, e = <AbortRequest>req.getJsonPayload();
        http:OutResponse res;
        if (e != null) {
            res = {statusCode:400};
            RequestError err = {errorMessage:"Bad Request"};
            var resPayload, _ = <json>err;
            res.setJsonPayload(resPayload);
            var connError = conn.respond(res);
            if (connError != null) {
                log:printErrorCause("Sending response for abort request with malformed transaction ID request failed",
                                    (error)connError);
            }
        } else {
            string txnId = abortReq.transactionId;
            var txn, _ = (TwoPhaseCommitTransaction)transactions[txnId];
            if (txn == null) {
                res = respondToBadRequest("Transaction-Unknown. Invalid TID:" + txnId);
                var connError = conn.respond(res);
                if (connError != null) {
                    log:printErrorCause("Sending response for abort request with null transaction ID failed",
                                        (error)connError);
                }
            } else {
                log:printInfo("Aborting transaction: " + txnId);
                // return response to the initiator. ( Aborted | Mixed )
                var msg, err = notifyAbort(txn);
                if (err == null) {
                    AbortResponse abortRes = {message:msg};
                    var resPayload, _ = <json>abortRes;
                    res = {statusCode:200};
                    res.setJsonPayload(resPayload);
                } else {
                    res = {statusCode:500}; //TODO: Not sure about this status code
                    var resPayload, _ = <json>err;
                    res.setJsonPayload(resPayload);
                }
                transactions.remove(txnId);
                var connError = conn.respond(res);
                if (connError != null) {
                    log:printErrorCause("Sending response for abort request for transaction " + txn.transactionId +
                                        " failed",
                                        (error)connError);
                }
            }
        }
    }

    @http:resourceConfig {
        path:"/replay"
    }
    resource replay (http:Connection conn, http:InRequest req) {

    }
}
