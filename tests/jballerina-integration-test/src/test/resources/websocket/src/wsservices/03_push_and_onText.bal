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

service onTextString on new http:Listener(21003) {

    resource function onText(http:WebSocketCaller caller, string data, boolean finalFrame) {
        var returnVal = caller->pushText(data);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }
}

service onTextJSON on new http:Listener(21023) {

resource function onText(http:WebSocketCaller caller, json data) {
        var returnVal = caller->pushText(data);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }
}

service onTextXML on new http:Listener(21024) {

    resource function onText(http:WebSocketCaller caller, xml data) {
        var returnVal = caller->pushText(data);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }
}

type Person record {|
    int id;
    string name;
|};
service onTextRecord on new http:Listener(21025) {

    resource function onText(http:WebSocketCaller caller, Person data) {
        var personData = data.cloneWithType(json);
        if (personData is error) {
            panic personData;
        } else {
             var returnVal = caller->pushText(personData);
             if (returnVal is http:WebSocketError) {
                panic <error> returnVal;
             }
        }
    }
}

service onTextByteArray on new http:Listener(21026){

    resource function onText(http:WebSocketCaller caller, byte[] data) {
        var returnVal = caller->pushText(data);
        if (returnVal is http:WebSocketError) {
            panic <error> returnVal;
        }
    }
}
