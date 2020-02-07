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

byte[] content = [];
service onBinaryContinuation on new http:Listener(21007) {
    resource function onBinary(http:WebSocketCaller caller, byte[] data, boolean finalFrame) {
        if (finalFrame) {
            appendToArray(<@untainted> data, content);
            var returnVal = caller->pushBinary(content);
            if (returnVal is http:WebSocketError) {
                panic <error> returnVal;
            }
        } else {
            appendToArray(<@untainted> data, content);
        }
    }
}

function appendToArray(byte[] src, byte[] dest) {
    int i = 0;
    int l = src.length();
    while (i < l) {
        dest[dest.length()] = src[i];
        i = i + 1;
    }
}
