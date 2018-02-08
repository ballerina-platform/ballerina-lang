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

package ballerina.builtin.transactions.coordinator;

import ballerina.net.http;
import ballerina.config;

public connector CoordinatorClient () {

    action register (string transactionId, string participantId, string registerAtURL) returns
                                                                                       (json jsonRes, error err) {
        endpoint<http:HttpClient> coordinatorEP {
            create http:HttpClient(registerAtURL, {});
        }
        RegistrationRequest regReq = {transactionId:transactionId, participantId:participantId};
        Protocol[] protocols = [{name:"volatile", url:"http://" + participantHost + ":" + participantPort + "/"}];
        regReq.participantProtocols = protocols;

        var j, _ = <json>regReq;
        http:OutRequest req = {};
        req.setJsonPayload(j);
        var res, e = coordinatorEP.post("", req);
        if (e == null) {
            int statusCode = res.getStatusCode();
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
