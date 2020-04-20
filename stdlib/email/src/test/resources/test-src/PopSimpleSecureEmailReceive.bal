// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/email;

email:PopConfig popConfig = {
     port: 3995,
     enableSsl: true
};

function testReceiveSimpleEmail(string host, string username, string password) returns email:Email|email:Error? {
    email:PopClient|email:Error popClient = new (host, username, password, popConfig);
    if (popClient is email:PopClient) {
        return popClient->read();
    } else {
        return popClient;
    }
}
