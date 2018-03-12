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

const string participantHost = getParticipantHost();
const int participantPort = getParticipantPort();

function getParticipantHost () returns (string host) {
    host = config:getInstanceValue("http", "participant.host");
    if (host == "") {
        host = "localhost";
    }
    return;
}

function getParticipantPort () returns (int port) {
    var p, e = <int>config:getInstanceValue("http", "participant.port");
    if (e != null) {
        port = 8081;
    } else {
        port = p;
    }
    return;
}

connector InitiatorClient (string registerAtURL) {
    endpoint<http:HttpClient> initiatorEP {
        create http:HttpClient(registerAtURL, {});
    }

    action register (string transactionId, int transactionBlockId) returns (RegistrationResponse registrationRes,
                                                                            error err) {
        string participantId = getParticipantId(transactionBlockId);
        RegistrationRequest regReq = {transactionId:transactionId, participantId:participantId};

        //TODO: set the proper protocol
        string protocol = "durable";
        Protocol[] protocols = [{name:protocol, url:getParticipantProtocolAt(protocol, transactionBlockId)}];
        regReq.participantProtocols = protocols;

        json j = <json, regRequestToJson()>regReq;
        http:Request req = {};
        req.setJsonPayload(j);
        var res, e = initiatorEP.post("", req);
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
}
