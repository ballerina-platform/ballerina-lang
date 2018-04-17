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

package ballerina.transactions;

import ballerina/http;

public type Participant2pcClientConfig {
    string participantURL;
    int timeoutMillis;
    {
        int count;
        int interval;
    } retryConfig;
};

public type Participant2pcClientEP object {

    private {
        http:Client httpClient;
        Participant2pcClientConfig conf;
    }

    public function init(Participant2pcClientConfig conf) {
        endpoint http:Client httpEP {targets:[{url:conf.participantURL}],
                                            timeoutMillis:conf.timeoutMillis,
                                            retry:{count:conf.retryConfig.count,
                                                      interval:conf.retryConfig.interval}};
        self.httpClient = httpEP;
        self.conf = conf;
    }

    public function getClient() returns Participant2pcClient {
        Participant2pcClient client = new;
        client.clientEP = self;
        return client;
    }
};

public type Participant2pcClient object {

    private {
        Participant2pcClientEP clientEP;
    }

    public function prepare(string transactionId) returns string|error {
        endpoint http:Client httpClient = self.clientEP.httpClient;
        http:Request req = new;
        PrepareRequest prepareReq = {transactionId:transactionId};
        json j = check <json>prepareReq;
        req.setJsonPayload(j);
        var  result = httpClient -> post("/prepare", req);
        http:Response res = check result;
        int statusCode = res.statusCode;
        if (statusCode == http:NOT_FOUND_404) {
            error err = {message:TRANSACTION_UNKNOWN};
            return err;
        } else if (statusCode == http:OK_200) {
            json payload = check res.getJsonPayload();
            PrepareResponse prepareRes = <PrepareResponse>payload; //TODO: Change this this to use the safe assignment operator
            return prepareRes.message;
        } else {
            error err = {message:"Prepare failed. Transaction: " + transactionId + ", Participant: " + self.clientEP.conf.participantURL};
            return err;
        }
    }

    public function notify(string transactionId, string message) returns string|error {
        endpoint http:Client httpClient = self.clientEP.httpClient;
        http:Request req = new;
        NotifyRequest notifyReq = {transactionId:transactionId, message:message};
        json j = check <json>notifyReq;
        req.setJsonPayload(j);
        var result = httpClient -> post("/notify", req);
        http:Response res = check result;
        json payload = check res.getJsonPayload();
        NotifyResponse notifyRes = <NotifyResponse>payload;  //TODO: Change this this to use the safe assignment operator
        string msg = notifyRes.message;
        int statusCode = res.statusCode;
        if (statusCode == http:OK_200) {
            return msg;
        } else if ((statusCode == http:BAD_REQUEST_400 && msg == NOTIFY_RESULT_NOT_PREPARED_STR) ||
                    (statusCode == http:NOT_FOUND_404 && msg == TRANSACTION_UNKNOWN) ||
                    (statusCode == http:INTERNAL_SERVER_ERROR_500 && msg == NOTIFY_RESULT_FAILED_EOT_STR)) {
            error participantErr = {message:msg};
            return participantErr;
        } else { // Some other error state
            error participantErr = {message:"Notify failed. Transaction: " + transactionId + ", Participant: " +
                                    self.clientEP.conf.participantURL};
            return participantErr;
        }
    }
};
