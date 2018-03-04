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
    basePath:participant2pcCoordinatorBasePath,
    host:coordinatorHost,
    port:coordinatorPort
}
service<http> Participant2pcService {

    // This resource is on the participant's coordinator. When the initiator's coordinator sends "prepare"
    // this resource on the participant will get called. This participant's coordinator will in turn call
    // prepare on all the resource managers registered with the respective transaction.
    @http:resourceConfig {
        methods:["POST"]
    }
    resource prepare (http:Connection conn, http:InRequest req) {
        http:OutResponse res;
        var prepareReq, _ = <PrepareRequest>req.getJsonPayload();
        string transactionId = prepareReq.transactionId;
        log:printInfo("Prepare received for transaction: " + transactionId);
        PrepareResponse prepareRes;
        var txn, _ = (TwoPhaseCommitTransaction)participatedTransactions[transactionId];
        if (txn == null) {
            res = {statusCode:404};
            prepareRes = {message:"Transaction-Unknown"};
        } else {
            // Call prepare on the local resource manager
            boolean prepareSuccessful = prepareResourceManagers(transactionId);
            if (prepareSuccessful) {
                res = {statusCode:200};
                txn.state = TransactionState.PREPARED;
                //PrepareResponse prepareRes = {message:"read-only"};
                prepareRes = {message:"prepared"};
                log:printInfo("Prepared transaction: " + transactionId);
            } else {
                res = {statusCode:500};
                prepareRes = {message:"aborted"};
                participatedTransactions.remove(transactionId);
                log:printInfo("Aborted transaction: " + transactionId);
            }
        }
        var j, _ = <json>prepareRes;
        res.setJsonPayload(j);
        var connError = conn.respond(res);
        if (connError != null) {
            log:printErrorCause("Sending response for prepare request for transaction " + transactionId +
                                " failed", (error)connError);
        }
    }

    // This resource is on the participant's coordinator. When the initiator's coordinator sends
    // "notify(commit | abort)" this resource on the participant will get called.
    // This participant's coordinator will in turn call "commit" or "abort" on
    // all the resource managers registered with the respective transaction.
    @http:resourceConfig {
        methods:["POST"]
    }
    resource notify (http:Connection conn, http:InRequest req) {
        var notifyReq, _ = <NotifyRequest>req.getJsonPayload();
        string transactionId = notifyReq.transactionId;
        log:printInfo("Notify(" + notifyReq.message + ") received for transaction: " + transactionId);
        http:OutResponse res;

        NotifyResponse notifyRes;
        var txn, _ = (TwoPhaseCommitTransaction)participatedTransactions[transactionId];
        if (txn == null) {
            res = {statusCode:404};
            notifyRes = {message:"Transaction-Unknown"};
        } else {
            if (notifyReq.message == "commit") {
                if (txn.state != TransactionState.PREPARED) {
                    res = {statusCode:400};
                    notifyRes = {message:"Not-Prepared"};
                } else {
                    // Notify commit to the resource manager
                    boolean commitSuccessful = commitResourceManagers(transactionId);

                    if (commitSuccessful) {
                        res = {statusCode:200};
                        notifyRes = {message:"Committed"};
                    } else {
                        res = {statusCode:500};
                        notifyRes = {message:"Failed-EOT"};
                    }
                }
            } else if (notifyReq.message == "abort") {
                // Notify abort to the resource manager
                boolean abortSuccessful = abortResourceManagers(transactionId);
                if (abortSuccessful) {
                    res = {statusCode:200};
                    notifyRes = {message:"Aborted"};
                } else {
                    res = {statusCode:500};
                    notifyRes = {message:"Failed-EOT"};
                }
            }
            participatedTransactions.remove(transactionId);
        }
        var j, _ = <json>notifyRes;
        res.setJsonPayload(j);
        var connError = conn.respond(res);
        if (connError != null) {
            log:printErrorCause("Sending response for notify request for transaction " + transactionId +
                                " failed", (error)connError);
        }
    }
}
