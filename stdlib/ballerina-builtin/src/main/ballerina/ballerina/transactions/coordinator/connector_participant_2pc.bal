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

struct Participant2pcClientConfig {
    string participantURL;
    int endpointTimeout;
    struct {
        int count;
        int interval;
    } retryConfig;
}

struct Participant2pcClientEP {
    http:ClientEndpoint httpClient;
    Participant2pcClientConfig conf;
}

function <Participant2pcClientEP ep> init(Participant2pcClientConfig conf){
    endpoint http:ClientEndpoint httpEP {targets:[{uri:conf.participantURL}],
                                            endpointTimeout:conf.endpointTimeout,
                                            retryConfig:{count:conf.retryConfig.count,
                                                            interval:conf.retryConfig.interval}};
    ep.httpClient = httpEP;
    ep.conf = conf;
}

function <Participant2pcClientEP ep> getClient() returns (Participant2pcClient) {
    return {clientEP: ep};
}

struct Participant2pcClient {
    Participant2pcClientEP clientEP;
}

function<Participant2pcClient client> prepare (string transactionId) returns (string status, error err) {
    endpoint http:ClientEndpoint httpClient = client.clientEP.httpClient;
    http:Request req = {};
    PrepareRequest prepareReq = {transactionId:transactionId};
    var j, _ = <json>prepareReq;
    req.setJsonPayload(j);
    var res, communicationErr = httpClient -> post("/prepare", req);
    if (communicationErr == null) {
        var payload, payloadError = res.getJsonPayload();
        if (payloadError == null) {
            var prepareRes, transformErr = <PrepareResponse>payload;
            if (transformErr == null) {
                int statusCode = res.statusCode;
                string msg = prepareRes.message;
                if (statusCode == 200) {
                    status = msg;
                } else if (statusCode == 404 && msg == "Transaction-Unknown") {
                    err = {message:msg};
                } else {
                    err = {message:"Prepare failed. Transaction: " + transactionId + ", Participant: " +
                                   client.clientEP.conf.participantURL};
                }
            } else {
                err = (error)transformErr;
            }
        } else {
            err = (error)payloadError;
        }
    } else {
        err = (error)communicationErr;
    }
    return;
}

function<Participant2pcClient client> notify (string transactionId, string message) returns (string status,
                                                                                             error participantErr,
                                                                                             error communicationErr) {
    endpoint http:ClientEndpoint httpClient = client.clientEP.httpClient;
    http:Request req = {};
    NotifyRequest notifyReq = {transactionId:transactionId, message:message};
    var j, _ = <json>notifyReq;
    req.setJsonPayload(j);
    var res, commErr = httpClient -> post("/notify", req);
    if (commErr == null) {
        var payload, payloadError = res.getJsonPayload();
        if (payloadError == null) {
            var notifyRes, transformErr = <NotifyResponse>payload;
            if (transformErr == null) {
                int statusCode = res.statusCode;
                string msg = notifyRes.message;
                if (statusCode == 200) {
                    if (transformErr == null) {
                        status = msg;
                    } else {
                        participantErr = (error)transformErr;
                    }
                } else if ((statusCode == 400 && msg == "Not-Prepared") ||
                           (statusCode == 404 && msg == "Transaction-Unknown") ||
                           (statusCode == 500 && msg == "Failed-EOT")) {
                    participantErr = {message:msg};
                } else {
                    participantErr = {message:"Notify failed. Transaction: " + transactionId + ", Participant: " +
                                                                               client.clientEP.conf.participantURL};
                }
            } else {
                communicationErr = (error)transformErr;
            }
        } else {
            communicationErr = (error)payloadError;
        }
    } else {
        communicationErr = (error)commErr;
    }
    return;
}
