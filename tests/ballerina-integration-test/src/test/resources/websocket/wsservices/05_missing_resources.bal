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

@http:WebSocketServiceConfig {
    idleTimeoutInSeconds: 10
}
service<http:WebSocketService> onlyOnBinary bind { port: 9086 } {
    onBinary(endpoint caller, byte[] data){
        var returnVal = caller->pushBinary(data);
        if (returnVal is error) {
           panic returnVal;
        }
    }
}

service<http:WebSocketService> onlyOnText bind { port: 9087 } {
    onText(endpoint caller, string data) {
        var returnVal = caller->pushText(data);
        if (returnVal is error) {
             panic returnVal;
        }
    }
}
