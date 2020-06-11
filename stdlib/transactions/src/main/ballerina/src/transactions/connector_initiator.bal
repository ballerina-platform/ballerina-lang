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

type InitiatorClientConfig record {
    string registerAtURL = "";
    int timeoutInMillis = 0;
    record {
        int count = 0;
        int intervalInMillis = 0;
    } retryConfig = {};
};

type InitiatorClientEP client object {
    http:Client httpClient;

    function __init(InitiatorClientConfig conf) {
        http:Client httpEP = new(conf.registerAtURL, {
                timeoutInMillis:conf.timeoutInMillis,
                retryConfig:{
                    count:conf.retryConfig.count,
                    intervalInMillis:conf.retryConfig.intervalInMillis
                }
            });
        self.httpClient = httpEP;
    }

    remote function register(string transactionId, string transactionBlockId, RemoteProtocol[] participantProtocols)
                 returns @tainted RegistrationResponse|error {
        http:Client httpClient = self.httpClient;
        string participantId = getParticipantId(transactionBlockId);
        RegistrationRequest regReq = {
            transactionId:transactionId, participantId:participantId, participantProtocols:participantProtocols
        };

        json reqPayload = check regReq.cloneWithType(typedesc<json>);
        http:Request req = new;
        req.setJsonPayload(reqPayload);
        var result = httpClient->post("", req);
        http:Response res = check result;
        int statusCode = res.statusCode;
        if (statusCode != http:STATUS_OK) {
            return TransactionError("Registration for transaction: " + transactionId + " failed response code: "
                + statusCode.toString());
        }
        json resPayload = check res.getJsonPayload();
        return <@untainted> resPayload.cloneWithType(typedesc<RegistrationResponse>);
    }
};
