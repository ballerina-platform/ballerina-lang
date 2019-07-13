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

public type Participant2pcClientConfig record {
    string participantURL = "";
    int timeoutMillis = 0;
    record {
        int count = 0;
        int interval = 0;
    } retryConfig = {};
};

public type Participant2pcClientEP client object {

    http:Client httpClient;
    Participant2pcClientConfig conf = {};

    public function __init(Participant2pcClientConfig c) {
        http:Client httpEP = new(c.participantURL, {
            timeoutMillis: c.timeoutMillis,
            retryConfig:{
                count: c.retryConfig.count, interval: c.retryConfig.interval
            }
        });
        self.httpClient = httpEP;
        self.conf = c;
    }

    public remote function prepare(string transactionId) returns string|error {
        http:Client httpClient = self.httpClient;
        http:Request req = new;
        PrepareRequest prepareReq = {transactionId:transactionId};
        json j = check typedesc<json>.constructFrom(prepareReq);
        req.setJsonPayload(j);
        var result = httpClient->post("/prepare", req);
        http:Response res = check result;
        int statusCode = res.statusCode;
        if (statusCode == http:NOT_FOUND_404) {
            error err = error(TRANSACTION_UNKNOWN);
            return err;
        } else if (statusCode == http:OK_200) {
            json payload = check res.getJsonPayload();
            PrepareResponse prepareRes = check typedesc<PrepareResponse>.constructFrom(payload);
            return <@untainted> prepareRes.message;
        } else {
            error err = error("Prepare failed. Transaction: " + transactionId + ", Participant: " +
                self.conf.participantURL);
            return <@untainted> err;
        }
    }

    public remote function notify(string transactionId, string message) returns string|error {
        http:Client httpClient = self.httpClient;
        http:Request req = new;
        NotifyRequest notifyReq = {transactionId:transactionId, message:message};
        json j = check typedesc<json>.constructFrom(notifyReq);
        req.setJsonPayload(j);
        var result = httpClient->post("/notify", req);
        http:Response res = check result;
        json payload = check res.getJsonPayload();
        NotifyResponse notifyRes = check typedesc<NotifyResponse>.constructFrom(payload);
        string msg = notifyRes.message;
        int statusCode = res.statusCode;
        if (statusCode == http:OK_200) {
            return <@untainted string> msg;
        } else if ((statusCode == http:BAD_REQUEST_400 && msg == NOTIFY_RESULT_NOT_PREPARED_STR) ||
            (statusCode == http:NOT_FOUND_404 && msg == TRANSACTION_UNKNOWN) ||
            (statusCode == http:INTERNAL_SERVER_ERROR_500 && msg == NOTIFY_RESULT_FAILED_EOT_STR)) {
            error participantErr = error(msg);
            return <@untainted error> participantErr;
        } else { // Some other error state
            error participantErr = error("Notify failed. Transaction: " + transactionId + ", Participant: " +
                self.conf.participantURL);
            return <@untainted error> participantErr;
        }
    }
};
