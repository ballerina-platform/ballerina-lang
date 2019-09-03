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

final string PATH1 = "PATH1";
final string PATH2 = "PATH2";
final string QUERY1 = "QUERY1";
final string QUERY2 = "QUERY2";

service simple6 on new http:Listener(21015) {

    @http:ResourceConfig {
        webSocketUpgrade: {
            upgradePath: "/{path1}/{path2}",
            upgradeService: simpleProxy6
        }
    }
    resource function websocketProxy(http:Caller httpEp, http:Request req, string path1, string path2) {
        http:WebSocketCaller|http:WebSocketError wsServiceEp =
        httpEp->acceptWebSocketUpgrade({ "X-some-header": "some-header-value" });
        if (wsServiceEp is http:WebSocketCaller) {
            wsServiceEp.setAttribute(PATH1, path1);
            wsServiceEp.setAttribute(PATH2, path2);
            wsServiceEp.setAttribute(QUERY1, req.getQueryParamValue("q1"));
            wsServiceEp.setAttribute(QUERY2, req.getQueryParamValue("q2"));
        } else {
            panic wsServiceEp;
        }
    }
}

service simpleProxy6 = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketCaller wsEp, string text) {
        if (text == "send") {
            string path1 = <string>wsEp.getAttribute(PATH1);
            string path2 = <string>wsEp.getAttribute(PATH2);
            string query1 = <string>wsEp.getAttribute(QUERY1);
            string query2 = <string>wsEp.getAttribute(QUERY2);

            string msg = string `path-params: ${path1}, ${path2}; query-params: ${query1}, ${query2}`;
            var returnVal = wsEp->pushText(msg);
            if(returnVal is http:WebSocketError) {
                panic <error> returnVal;
            }
        }
    }
};
