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

import ballerina/net.http;

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
    endpoint http:ClientEndpoint httpEP {targets:[{uri:conf.participantURL}],
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
    var j =? <json>prepareReq;
    req.setJsonPayload(j);
    var res =? httpClient -> post("/prepare", req);
    int statusCode = res.statusCode;
    if (statusCode == 404) {
        error err = {message:"Transaction-Unknown"};
        return err;
    } else if (statusCode == 200) {
        var payload =? res.getJsonPayload();
        var prepareRes =? <PrepareResponse>payload;
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
    var j =? <json>notifyReq;
    req.setJsonPayload(j);
    var res =? httpClient -> post("/notify", req);
    var payload =? res.getJsonPayload();
    var notifyRes =? <NotifyResponse>payload;
    string msg = notifyRes.message;
    int statusCode = res.statusCode;
    if (statusCode == 200) {
        return msg;
    } else if ((statusCode == 400 && msg == "Not-Prepared") || (statusCode == 404 && msg == "Transaction-Unknown") ||
               (statusCode == 500 && msg == "Failed-EOT")) {
        error participantErr = {message:msg};
        return participantErr;
    } else {
        error participantErr = {message:"Notify failed. Transaction: " + transactionId + ", Participant: " +
                                        client.clientEP.conf.participantURL};
        return participantErr;
    }
}
