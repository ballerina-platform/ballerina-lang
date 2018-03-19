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

import ballerina.config;
import ballerina.net.http;

public struct InitiatorClientConfig {
    string registerAtURL;
    int endpointTimeout;
    struct {
        int count;
        int interval;
    } retryConfig;
}

public struct InitiatorClientEP {
    http:ClientEndpoint httpClient;
}

public function <InitiatorClientEP ep> init(InitiatorClientConfig conf){
    endpoint http:ClientEndpoint httpEP {targets:[{uri:conf.registerAtURL}],
                                            endpointTimeout:conf.endpointTimeout,
                                            retryConfig:{count:conf.retryConfig.count,
                                                            interval:conf.retryConfig.interval}};
    ep.httpClient = httpEP;
}

public function <InitiatorClientEP ep> getClient() returns (InitiatorClient) {
    return {clientEP: ep};
}

public struct InitiatorClient {
    InitiatorClientEP clientEP;
}

public function<InitiatorClient client> register (string transactionId,
                                                  int transactionBlockId,
                                                  Protocol[] participantProtocols) returns (RegistrationResponse registrationRes,
                                                                            error err) {
    endpoint http:ClientEndpoint httpClient = client.clientEP.httpClient;
    string participantId = getParticipantId(transactionBlockId);
    RegistrationRequest regReq = {transactionId:transactionId, participantId:participantId};

    regReq.participantProtocols = participantProtocols;

    json j = <json, regRequestToJson()>regReq;
    http:Request req = {};
    req.setJsonPayload(j);
    var res, e = httpClient -> post("", req);
    if (e == null) {
        int statusCode = res.statusCode;
        var payload, payloadError = res.getJsonPayload();
        if (payloadError == null) {
            if (statusCode == 200) {
                registrationRes = <RegistrationResponse, jsonToRegResponse()>(payload);
            } else {
                if (payload == null) {
                    var stringPayload, _ = res.getStringPayload();
                    err = {message:stringPayload};
                } else {
                    var errMsg, _ = (string)payload.errorMessage;
                    err = {message:errMsg};
                }
            }
        } else {
            err = (error)payloadError;
        }
    } else {
        err = (error)e;
    }
    return;
}
