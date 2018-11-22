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
import ballerina/log;

service<http:WebSocketService> onTextString bind { port: 9080 } {

    onText(endpoint caller, string data, boolean final) {
        var returnVal = caller->pushText(data);
        if (returnVal is error) {
             panic returnVal;
        }
    }
}

service<http:WebSocketService> onTextJSON bind { port: 9081 } {

    onText(endpoint caller, json data) {
        var returnVal = caller->pushText(data);
        if (returnVal is error) {
             panic returnVal;
        }
    }
}

service<http:WebSocketService> onTextXML bind { port: 9082 } {

    onText(endpoint caller, xml data) {
        var returnVal = caller->pushText(data);
        if (returnVal is error) {
             panic returnVal;
        }
    }
}

type Person record {
    int id;
    string name;
    !...
};
service<http:WebSocketService> onTextRecord bind { port: 9083 } {

    onText(endpoint caller, Person data) {
        var personData = <json>data;
        if (personData is error) {
             panic personData;
        } else {
             var returnVal = caller->pushText(personData);
             if (returnVal is error) {
                  panic returnVal;
             }
        }
    }
}

service<http:WebSocketService> onTextByteArray bind { port: 9084 } {

    onText(endpoint caller, byte[] data) {
        var returnVal = caller->pushText(data);
        if (returnVal is error) {
             panic returnVal;
        }
    }
}
