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

listener http:Listener attachDetachEp = new (21029);
@http:WebSocketServiceConfig {
    path: "/attach/detach"
}
service attachDetach on attachDetachEp {
    resource function onText(http:WebSocketCaller caller, string data, boolean finalFrame) returns error?{
        if (data == "attach") {
            var err = attachDetachEp.__attach(wsNoPath);
            handleError(err, caller);
            err = attachDetachEp.__attach(wsWithPath);
        } else if (data == "detach") {
            var err = attachDetachEp.__detach(wsNoPath);
            handleError(err, caller);
            err = attachDetachEp.__detach(wsWithPath);
        } else if (data == "client_attach") {
            var err = attachDetachEp.__attach(wsClientService);
            handleError(err, caller);
        }
    }
}

service wsWithPath = @http:WebSocketServiceConfig {path: "/hello"} service {


    resource function onText(http:WebSocketCaller conn, string text, boolean finalFrame) returns error? {
       check conn->pushText(text);
    }
};

service wsNoPath = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketCaller conn, string text, boolean finalFrame) returns error? {
        check conn->pushText(text);
    }
};

service wsClientService = @http:WebSocketServiceConfig {} service {

    resource function onText(http:WebSocketClient conn, string text, boolean finalFrame) returns error? {
        check conn->pushText(text);
    }
};

function handleError(error? err, http:WebSocketCaller caller) {
    if (err is http:WebSocketError) {
        checkpanic caller->pushText(err.detail()["message"]);
    }
}
