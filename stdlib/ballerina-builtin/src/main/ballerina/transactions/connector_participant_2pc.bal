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

public struct Participant2pcClientConfig {
    string participantURL;
    int endpointTimeout;
    struct {
        int count;
        int interval;
    } retryConfig;
}

public struct Participant2pcClientEP {
    http:ClientEndpoint httpClient;
    Participant2pcClientConfig conf;
}

public function <Participant2pcClientEP ep> init (Participant2pcClientConfig conf) {
    endpoint http:ClientEndpoint httpEP {targets:[{url:conf.participantURL}],
        endpointTimeout:conf.endpointTimeout,
        retry:{count:conf.retryConfig.count,
                  interval:conf.retryConfig.interval}};
    ep.httpClient = httpEP;
    ep.conf = conf;
}

public function <Participant2pcClientEP ep> getClient () returns Participant2pcClient {
    return {clientEP:ep};
}

public struct Participant2pcClient {
    Participant2pcClientEP clientEP;
}

public function <Participant2pcClient client> prepare (string transactionId) returns string|error {
    endpoint http:ClientEndpoint httpClient = client.clientEP.httpClient;
    http:Request req = {};
    PrepareRequest prepareReq = {transactionId:transactionId};
    json j =? <json>prepareReq;
    req.setJsonPayload(j);
    http:Response res =? httpClient -> post("/prepare", req);
    int statusCode = res.statusCode;
    if (statusCode == http:NOT_FOUND_404) {
        error err = {message:"Transaction-Unknown"};
        return err;
    } else if (statusCode == http:OK_200) {
        json payload =? res.getJsonPayload();
        PrepareResponse prepareRes = <PrepareResponse>payload; //TODO: Change this this to use the safe assignment operator
        return prepareRes.message;
    } else {
        error err = {message:"Prepare failed. Transaction: " + transactionId + ", Participant: " +
                             client.clientEP.conf.participantURL};
        return err;
    }
}

public function <Participant2pcClient client> notify (string transactionId, string message) returns string|error {
    endpoint http:ClientEndpoint httpClient = client.clientEP.httpClient;
    http:Request req = {};
    NotifyRequest notifyReq = {transactionId:transactionId, message:message};
    json j =? <json>notifyReq;
    req.setJsonPayload(j);
    http:Response res =? httpClient -> post("/notify", req);
    json payload =? res.getJsonPayload();
    NotifyResponse notifyRes = <NotifyResponse>payload;  //TODO: Change this this to use the safe assignment operator
    string msg = notifyRes.message;
    int statusCode = res.statusCode;
    if (statusCode == http:OK_200) {
        return msg;
    } else if ((statusCode == http:BAD_REQUEST_400 && msg == "Not-Prepared") ||
               (statusCode == http:NOT_FOUND_404 && msg == "Transaction-Unknown") ||
               (statusCode == http:INTERNAL_SERVER_ERROR_500 && msg == "Failed-EOT")) {
        error participantErr = {message:msg};
        return participantErr;
    } else {
        error participantErr = {message:"Notify failed. Transaction: " + transactionId + ", Participant: " +
                                        client.clientEP.conf.participantURL};
        return participantErr;
    }
}
