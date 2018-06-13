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

import ballerina/io;
import ballerina/http;

@final string PATH1 = "PATH1";
@final string PATH2 = "PATH2";
@final string QUERY1 = "QUERY1";
@final string QUERY2 = "QUERY2";

service<http:Service> simple bind { port: 9090 } {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/{path1}/{path2}",
            upgradeService: simpleProxy
        }
    }
    websocketProxy(endpoint httpEp, http:Request req, string path1, string path2) {
        endpoint http:WebSocketListener wsServiceEp;
        wsServiceEp = httpEp->acceptWebSocketUpgrade({ "X-some-header": "some-header-value" });
        wsServiceEp.attributes[PATH1] = path1;
        wsServiceEp.attributes[PATH2] = path2;
        wsServiceEp.attributes[QUERY1] = req.getQueryParams()["q1"];
        wsServiceEp.attributes[QUERY2] = req.getQueryParams()["q2"];
    }
}

service<http:WebSocketService> simpleProxy {

    onText(endpoint wsEp, string text) {
        if (text == "send") {
            string path1 = <string>wsEp.attributes[PATH1];
            string path2 = <string>wsEp.attributes[PATH2];
            string query1 = <string>wsEp.attributes[QUERY1];
            string query2 = <string>wsEp.attributes[QUERY2];

            string msg = string `path-params: {{path1}}, {{path2}}; query-params: {{query1}}, {{query2}}`;
            wsEp->pushText(msg) but {
                error e => io:println("Error sending message. " + e.message)
            };
        }
    }
}
