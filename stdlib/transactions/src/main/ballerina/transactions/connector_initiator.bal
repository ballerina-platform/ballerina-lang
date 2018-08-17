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
    string registerAtURL;
    int timeoutMillis;
    record {
        int count;
        int interval;
    } retryConfig;
};

type InitiatorClientEP object {
    http:Client httpClient;

    function init(InitiatorClientConfig conf) {
        endpoint http:Client httpEP {
            url:conf.registerAtURL,
            timeoutMillis:conf.timeoutMillis,
            retryConfig:{
                count:conf.retryConfig.count, interval:conf.retryConfig.interval
            }
        };
        self.httpClient = httpEP;
    }

    function getCallerActions() returns InitiatorClient {
        InitiatorClient client = new;
        client.clientEP = self;
        return client;
    }
};

type InitiatorClient object {
    InitiatorClientEP clientEP;

    new() {

    }

    function register(string transactionId, int transactionBlockId, RemoteProtocol[] participantProtocols)
        returns RegistrationResponse|error {

        endpoint http:Client httpClient = self.clientEP.httpClient;
        string participantId = getParticipantId(transactionBlockId);
        RegistrationRequest regReq = {
            transactionId:transactionId, participantId:participantId, participantProtocols:participantProtocols
        };

        json reqPayload = check <json>regReq;
        http:Request req = new;
        req.setJsonPayload(reqPayload);
        var result = httpClient->post("", req);
        http:Response res = check result;
        int statusCode = res.statusCode;
        if (statusCode != http:OK_200) {
            error err = {message:"Registration for transaction: " + transactionId + " failed"};
            return err;
        }
        json resPayload = check res.getJsonPayload();
        return <RegistrationResponse>resPayload;
    }
};
