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

import ballerina.log;
import ballerina.net.http;

@http:configuration {
    basePath:"/2pc",
    host:coordinatorHost,
    port:coordinatorPort
}
service<http> TwoPcParticipantCoordinator {

    // This resource is on the participant's coordinator. When the initiator's coordinator sends "prepare"
    // this resource on the participant will get called. This participant's coordinator will in turn call
    // prepare on all the resource managers registered with the respective transaction.
    resource prepare (http:Connection conn, http:InRequest req) {
        http:OutResponse res;
        var prepareReq, _ = <PrepareRequest>req.getJsonPayload();
        string transactionId = prepareReq.transactionId;
        log:printInfo("Prepare received for transaction: " + transactionId);
        var txn, _ = (TwoPhaseCommitTransaction)transactions[transactionId];
        if (txn == null) {
            res = {statusCode:404};
            PrepareResponse prepareRes = {message:"Transaction-Unknown"};
            var j, _ = <json>prepareRes;
            res.setJsonPayload(j);
        } else {
            // TODO: call prepare on the local resource manager, if the transaction manager returns OK, then return
            // "Prepared" else return "Aborted"

            res = {statusCode:200};
            txn.state = TransactionState.PREPARED;
            //PrepareResponse prepareRes = {message:"read-only"};
            PrepareResponse prepareRes = {message:"prepared"};
            log:printInfo("Prepared");
            var j, _ = <json>prepareRes;
            res.setJsonPayload(j);
        }

        _ = conn.respond(res);
    }

    // This resource is on the participant's coordinator. When the initiator's coordinator sends
    // "notify(commit | abort)" this resource on the participant will get called.
    // This participant's coordinator will in turn call "commit" or "abort" on
    // all the resource managers registered with the respective transaction.
    resource notify (http:Connection conn, http:InRequest req) {
        var notifyReq, _ = <NotifyRequest>req.getJsonPayload();
        string transactionId = notifyReq.transactionId;
        log:printInfo("Notify(" + notifyReq.message + ") received for transaction: " + transactionId);
        http:OutResponse res;

        NotifyResponse notifyRes;
        var txn, _ = (TwoPhaseCommitTransaction)transactions[transactionId];
        if (txn == null) {
            res = {statusCode:404};
            notifyRes = {message:"Transaction-Unknown"};
        } else {
            if (notifyReq.message == "commit") {
                if (txn.state != TransactionState.PREPARED) {
                    res = {statusCode:400};
                    notifyRes = {message:"Not-Prepared"};
                } else {
                    // TODO: Notify commit to the resource manager
                    res = {statusCode:200};
                    notifyRes = {message:"committed"};

                }
            } else if (notifyReq.message == "abort") {
                // TODO: Notify abort to the resource manager
                res = {statusCode:200};
                notifyRes = {message:"aborted"};
            }
            transactions.remove(transactionId);
        }
        var j, _ = <json>notifyRes;
        res.setJsonPayload(j);
        _ = conn.respond(res);
    }
}
