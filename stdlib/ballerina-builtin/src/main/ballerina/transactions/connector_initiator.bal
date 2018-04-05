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

type InitiatorClientConfig {
    string registerAtURL;
    int endpointTimeout;
    struct {
        int count;
        int interval;
    } retryConfig;
}

type InitiatorClientEP object {
    private {
        http:ClientEndpoint httpClient;
    }

    function init (InitiatorClientConfig conf) {
        endpoint http:ClientEndpoint httpEP {targets:[{url:conf.registerAtURL}],
                                            endpointTimeout:conf.endpointTimeout,
                                            retry:{count:conf.retryConfig.count,
                                                      interval:conf.retryConfig.interval}};
        self.httpClient = httpEP;
    }

    function getClient () returns InitiatorClient {
        InitiatorClient client = new;
        client.clientEP = self;
        return client;
    }
}

type InitiatorClient object {
    private {
        InitiatorClientEP clientEP;
    }

    new() {}


    function register(string transactionId, int transactionBlockId,
                        Protocol[] participantProtocols) returns RegistrationResponse|error {
        endpoint http:ClientEndpoint httpClient = self.clientEP.httpClient;
        string participantId = getParticipantId(transactionBlockId);
        RegistrationRequest regReq = {transactionId:transactionId, participantId:participantId};
        regReq.participantProtocols = participantProtocols;

        json reqPayload = regRequestToJson(regReq);
        http:Request req = {};
        req.setJsonPayload(reqPayload);
        http:Response res = httpClient -> post("", req) but {error};
        int statusCode = res.statusCode;
        if (statusCode != http:OK_200) {
            error err = {message:"Registration for transaction: " + transactionId + " failed"};
            return err;
        }
        json resPayload = check res.getJsonPayload();
        return jsonToRegResponse(resPayload);
    }
}
