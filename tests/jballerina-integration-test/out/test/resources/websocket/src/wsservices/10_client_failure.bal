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
import ballerina/io;

http:WebSocketCaller? globalServerCaller = ();

@http:WebSocketServiceConfig {
    path: "/client/failure"
}
service clientFailure on new http:Listener(21010) {

    resource function onOpen(http:WebSocketCaller wsEp) {
        http:WebSocketClient wsClientEp;
        globalServerCaller = <@untainted> wsEp;
        wsClientEp = new("ws://localhost:21018/websocketxyz", {callbackService: errorHandlingService,
                            readyOnConnect: false });
        var err = wsClientEp->ready();
        if (err is http:WebSocketError) {
          io:println(err.detail()["message"]);
        }
    }
}
service errorHandlingService = @http:WebSocketServiceConfig {} service {
    resource function onError(http:WebSocketClient caller, error err) {
        http:WebSocketCaller? serverCaller = globalServerCaller;
        if (serverCaller is http:WebSocketCaller) {
            var closeErr = serverCaller->close(1011, <string>err.detail()["message"], 0);
            io:println("Failed during closing the connection: ", closeErr);
        } else {
            io:println("serverCaller has not been set");
        }
    }
};
