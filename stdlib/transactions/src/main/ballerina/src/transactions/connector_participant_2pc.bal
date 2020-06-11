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

import ballerina/http;

type Participant2pcClientConfig record {
    string participantURL = "";
    int timeoutInMillis = 0;
    record {
        int count = 0;
        int intervalInMillis = 0;
    } retryConfig = {};
};

type Participant2pcClientEP client object {

    http:Client httpClient;
    Participant2pcClientConfig conf = {};

    function __init(Participant2pcClientConfig c) {
        http:Client httpEP = new(c.participantURL, {
            timeoutInMillis: c.timeoutInMillis,
            retryConfig:{
                count: c.retryConfig.count, intervalInMillis: c.retryConfig.intervalInMillis
            }
        });
        self.httpClient = httpEP;
        self.conf = c;
    }

    remote function prepare(string transactionId) returns @tainted string|error {
        http:Client httpClient = self.httpClient;
        http:Request req = new;
        PrepareRequest prepareReq = {transactionId:transactionId};
        json j = check prepareReq.cloneWithType(typedesc<json>);
        req.setJsonPayload(j);
        var result = httpClient->post("/prepare", req);
        http:Response res = check result;
        int statusCode = res.statusCode;
        if (statusCode == http:STATUS_NOT_FOUND) {
            return TransactionError(TRANSACTION_UNKNOWN);
        } else if (statusCode == http:STATUS_OK) {
            json payload = check res.getJsonPayload();
            PrepareResponse prepareRes = check payload.cloneWithType(typedesc<PrepareResponse>);
            return <@untainted> prepareRes.message;
        } else {
            return TransactionError("Prepare failed. Transaction: " + transactionId + ", Participant: " +
                self.conf.participantURL);
        }
    }

    remote function notify(string transactionId, string message) returns @tainted string|error {
        http:Client httpClient = self.httpClient;
        http:Request req = new;
        NotifyRequest notifyReq = {transactionId:transactionId, message:message};
        json j = check notifyReq.cloneWithType(typedesc<json>);
        req.setJsonPayload(j);
        var result = httpClient->post("/notify", req);
        http:Response res = check result;
        json payload = check res.getJsonPayload();
        NotifyResponse notifyRes = check payload.cloneWithType(typedesc<NotifyResponse>);
        string msg = notifyRes.message;
        int statusCode = res.statusCode;
        if (statusCode == http:STATUS_OK) {
            return <@untainted string> msg;
        } else if ((statusCode == http:STATUS_BAD_REQUEST && msg == NOTIFY_RESULT_NOT_PREPARED_STR) ||
            (statusCode == http:STATUS_NOT_FOUND && msg == TRANSACTION_UNKNOWN) ||
            (statusCode == http:STATUS_INTERNAL_SERVER_ERROR && msg == NOTIFY_RESULT_FAILED_EOT_STR)) {
            return TransactionError(msg);
        } else { // Some other error state
            return TransactionError("Notify failed. Transaction: " + transactionId + ", Participant: " +
                self.conf.participantURL);
        }
    }
};
