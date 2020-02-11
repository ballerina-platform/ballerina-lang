// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/io;

http:WebSocketCaller? serverCaller = ();

@http:WebSocketServiceConfig {
    path: "/client/errors"
}
service clientError on new http:Listener(21027) {

    resource function onText(http:WebSocketCaller wsEp, string text) {
        http:WebSocketClientConfiguration config = {callbackService: errorResourceService, subProtocols: ["xml"]};
        serverCaller = <@untainted>wsEp;
        if (text == "invalid-connection") {
            http:WebSocketClient wsClientEp = new ("ws://lmnop.ls", config);
        } else if (text == "ssl") {
            http:WebSocketClient wsClientEp = new ("wss://localhost:15000/websocket", config);
        } else if (text == "long-frame") {
            string ping = "pingpingpingpingpingpingpingpingpingpingpingpingpingpingpingpingpingpingpingpingpingpingping"
            + "pingpingpingpingpingpingpingpingpingpingpingpingpingping";
            byte[] pingData = ping.toBytes();
            http:WebSocketClient wsClientEp = new (REMOTE_BACKEND_URL, config);
            var err = wsClientEp->ping(pingData);
            if (err is error) {
                checkpanic wsEp->pushText(err.toString());
            }
        } else if (text == "connection-closed") {
            http:WebSocketClient wsClientEp = new (REMOTE_BACKEND_URL, config);
            checkpanic wsClientEp->close();
            var err = wsClientEp->pushText("some");
            if (err is error) {
                io:println(err);
                checkpanic wsEp->pushText(err.toString());
            }
        } else if (text == "handshake") {
            http:WebSocketClient wsClientEp = new (REMOTE_BACKEND_URL, {callbackService: errorResourceService,
             subProtocols: ["abc"]});
        } else if (text == "ready") {
            http:WebSocketClient wsClientEp = new (REMOTE_BACKEND_URL, {callbackService: errorResourceService});
            var returnVal = wsClientEp->ready();
            if (returnVal is error) {
                checkpanic wsEp->pushText(returnVal.toString());
            }
        } else {
            checkpanic wsEp->pushText(text);
        }
    }
}
service errorResourceService = @http:WebSocketServiceConfig {} service {
    resource function onError(http:WebSocketClient clientCaller, error err) {
        http:WebSocketCaller? caller = serverCaller;
        io:println("client error ", err);
        if (caller is http:WebSocketCaller) {
            checkpanic caller->pushText(err.toString());
        } else {
            io:println("serverCaller has not been set");
        }
    }
};
