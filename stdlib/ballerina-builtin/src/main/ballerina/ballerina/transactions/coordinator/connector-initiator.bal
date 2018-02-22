// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina.config;

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

connector InitiatorCoordinatorClient () {

    action register (string transactionId, string participantId, string registerAtURL) returns
                                                                                       (json jsonRes, error err) {
        endpoint<http:HttpClient> coordinatorEP {
            create http:HttpClient(registerAtURL, {});
        }
        RegistrationRequest regReq = {transactionId:transactionId, participantId:localParticipantId};

        //TODO: set the proper host and port
        Protocol[] protocols = [{name:"volatile", url:"http://" + participantHost + ":" + participantPort + "/"}];
        regReq.participantProtocols = protocols;

        var j, _ = <json>regReq;
        http:OutRequest req = {};
        req.setJsonPayload(j);
        var res, e = coordinatorEP.post("", req);
        if (e == null) {
            int statusCode = res.statusCode;
            if (statusCode == 200) {
                jsonRes = res.getJsonPayload();
            } else {
                var errMsg, _ = (string)res.getJsonPayload().errorMessage;
                err = {msg:errMsg};
            }
        } else {
            err = (error)e;
        }
        return;
    }
}
