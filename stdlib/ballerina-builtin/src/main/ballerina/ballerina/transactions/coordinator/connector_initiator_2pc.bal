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

import ballerina.net.http;

public connector Initiator2pcClient (string coordinatorProtocolAt) {
    endpoint<http:HttpClient> initiatorEP {
        create http:HttpClient(coordinatorProtocolAt, {});
    }
    action abortTransaction (string transactionId, int transactionBlockId) returns (string msg, error err) {

        AbortRequest abortReq = {transactionId:transactionId, participantId:getParticipantId(transactionBlockId)};
        var j, _ = <json>abortReq;
        http:Request req = {};
        req.setJsonPayload(j);
        var res, commErr = initiatorEP.post("/abort", req);
        if (commErr == null) {
            var payload, payloadError = res.getJsonPayload();
            if (payloadError == null) {
                var abortRes, transformErr = <AbortResponse>payload;
                if (transformErr != null) {
                    msg = abortRes.message;
                } else {
                    err = (error)transformErr;
                }
            } else {
                err = (error)payloadError;
            }
        } else {
            err = (error)commErr;
        }
        return;
    }
}
