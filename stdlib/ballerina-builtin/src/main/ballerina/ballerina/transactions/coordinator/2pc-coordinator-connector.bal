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

connector TwoPhaseCommitCoordinatorClient () {

    action commitTransaction (CommitRequest commitReq) returns (json jsonRes, error err) {
        endpoint<http:HttpClient> coordinatorEP {
            create http:HttpClient("http://localhost:9999/2pc/commit", {endpointTimeout:15000});
        }
        var j, _ = <json>commitReq;
        http:OutRequest req = {};
        req.setJsonPayload(j);
        var res, e = coordinatorEP.post("", req);
        if (e == null) {
            jsonRes = res.getJsonPayload();
        } else {
            err = (error)e;
        }
        return;
    }

    action abortTransaction (AbortRequest abortReq) returns (json jsonRes, error err) {
        endpoint<http:HttpClient> coordinatorEP {
            create http:HttpClient("http://localhost:9999/2pc/abort", {});
        }
        var j, _ = <json>abortReq;
        http:OutRequest req = {};
        req.setJsonPayload(j);
        var res, e = coordinatorEP.post("", req);
        if (e == null) {
            jsonRes = res.getJsonPayload();
        } else {
            err = (error)e;
        }
        return;
    }
}
