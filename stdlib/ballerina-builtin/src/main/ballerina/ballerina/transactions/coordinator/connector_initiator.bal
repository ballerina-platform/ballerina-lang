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
    endpoint<http:HttpClient> coordinatorEP {
        create http:HttpClient(registerAtURL, {});
    }

    action register (string transactionId) returns (RegistrationResponse registrationRes,
                                                    error err) {
        RegistrationRequest regReq = {transactionId:transactionId, participantId:localParticipantId};

        //TODO: set the proper protocol
        string protocol = "durable";
        Protocol[] protocols = [{name:"volatile", url:getParticipantProtocolAt(protocol)}];
        regReq.participantProtocols = protocols;

        var j, _ = <json>regReq;
        http:OutRequest req = {};
        req.setJsonPayload(j);
        var res, e = coordinatorEP.post("", req);
        if (e == null) {
            int statusCode = res.statusCode;
            if (statusCode == 200) {
                var regRes, transformErr = <RegistrationResponse>res.getJsonPayload();
                registrationRes = regRes;
                err = (error)transformErr;
            } else {
                json payload = res.getJsonPayload();
                if (payload == null) {
                    err = {message:res.getStringPayload()};
                } else {
                    var errMsg, _ = (string)payload.errorMessage;
                    err = {message:errMsg};
                }
            }
        } else {
            err = (error)e;
        }
        return;
    }
}
